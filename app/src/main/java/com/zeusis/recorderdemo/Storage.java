package com.zeusis.recorderdemo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2016/12/17.
 */

public class Storage {

    private static final String TAG = "xwf";

    public static final String MIME_TYPE_JPEG = "image/jpeg";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String path = Environment.getExternalStorageDirectory().toString();
    public static final String DIRECTORY = path + "/CameraDemo";
    public static final File DIRECTORY_FILE = new File(DIRECTORY);
    public static final String JPEG_POSTFIX = ".jpg";
    public static final String GIF_POSTFIX = ".gif";

    public static final String VIDEO_BASE_URI = "content://media/external/video/media";

    public static void generateStoragePath(){
        File path = new File(DIRECTORY);
        if(!path.exists()){
            path.mkdir();
        }
    }

    public static String createPictureName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("'PIC'_yyyyMMdd_HHmmss");

        return dateFormat.format(date);
    }

    public static String createVideoName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss");

        return dateFormat.format(date);
    }

    public static String generateFilepath(String directory, String title, String mimeType) {
        String extension = null;
        if (MIME_TYPE_JPEG.equals(mimeType)) {
            extension = JPEG_POSTFIX;
        } else if (MIME_TYPE_GIF.equals(mimeType)) {
            extension = GIF_POSTFIX;
        } else {
            throw new IllegalArgumentException("Invalid mimeType: " + mimeType);
        }
        return (new File(directory, title + extension)).getAbsolutePath();
    }

    public static void saveDataToFile(String path, byte[] data){
        writeFile(path, data);
    }

    /**
     * Writes the data to a file.
     *
     * @param path The path to the target file.
     * @param data The data to save.
     *
     * @return The size of the file. -1 if failed.
     */
    private static long writeFile(String path, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
            return data.length;
        } catch (Exception e) {
            Log.e(TAG, "Failed to write data", e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close file after write", e);
            }
        }
        return -1;
    }

    // Get a ContentValues object for the given photo data
    public static ContentValues getContentValuesForData(String title,
                                long date, Location location, int orientation, long jpegLength,
                                String path, int width, int height, String mimeType) {

        File file = new File(path);
        long dateModifiedSeconds = TimeUnit.MILLISECONDS.toSeconds(file.lastModified());

        ContentValues values = new ContentValues(11);
        values.put(MediaStore.Images.ImageColumns.TITLE, title);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, title + JPEG_POSTFIX);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, date);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateModifiedSeconds);
        // Clockwise rotation in degrees. 0, 90, 180, or 270.
        values.put(MediaStore.Images.ImageColumns.ORIENTATION, orientation);
        values.put(MediaStore.Images.ImageColumns.DATA, path);
        values.put(MediaStore.Images.ImageColumns.SIZE, jpegLength);

        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            values.put(MediaStore.MediaColumns.WIDTH, width);
            values.put(MediaStore.MediaColumns.HEIGHT, height);
        }

        if (location != null) {
            values.put(MediaStore.Images.ImageColumns.LATITUDE, location.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, location.getLongitude());
        }
        return values;
    }

    /**
     * Add the entry for the media file to media store.
     *
     * @param resolver The The content resolver to use.
     * @param title The title of the media file.
     * @param date The date for the media file.
     * @param location The location of the media file.
     * @param orientation The orientation of the media file.
     * @param width The width of the media file after the orientation is
     *            applied.
     * @param height The height of the media file after the orientation is
     *            applied.
     * @param mimeType The MIME type of the data.
     * @return The content URI of the inserted media file or null, if the image
     *         could not be added.
     */
    public static Uri addImageToMediaStore(ContentResolver resolver, String title, long date,
                                           Location location, int orientation, long jpegLength, String path, int width, int height,
                                           String mimeType) {
        // Insert into MediaStore.
        ContentValues values =
                getContentValuesForData(title, date, location, orientation, jpegLength, path, width,
                        height, mimeType);

        Uri uri = null;
        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
            // This can happen when the external volume is already mounted, but
            // MediaScanner has not notify MediaProvider to add that volume.
            // The picture is still safe and MediaScanner will find it and
            // insert it into MediaProvider. The only problem is that the user
            // cannot click the thumbnail to review the picture.
            Log.e(TAG, "Failed to write MediaStore" + th);
        }
        return uri;
    }

}
