package com.zeusis.recorderdemo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xwf";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private boolean mHasCriticalPermissions;

    private Button mButtonOpen;
    private Button mButtonOpen2;
    private Button mButtonClose;
    private Button mButtonClose2;
    private Button mButtonStartRecorder;
    private Button mButtonStopRecorder;

    private SurfaceTexture mSurfaceTexture;
    private SurfaceTexture mSurfaceTexture2;

    private Camera mCamera;
    private Camera mCamera2;

    private static final int REQUEST_CODE = 1;
    static final int NO_DEVICE = -1;

    private Camera.CameraInfo[] cameraInfos;
    private int mCameraNumber;
    private int firstFront;
    private int firstBack;

    private String mPictureName;

    protected MediaRecorder mMediaRecorder;
    protected CamcorderProfile mProfile;
    protected boolean mPreferenceRead;

    protected String mCurrentVideoFilename;
    protected Uri mCurrentVideoUri;
    protected boolean mCurrentVideoUriFromMediaSaved;
    protected ContentValues mCurrentVideoValues;
    private String mVideoFilenameTMP;
    protected ParcelFileDescriptor mVideoFileDescriptor;
    private boolean mIsVideoRecording;
    protected long mRecordingStartTime;

    public static final long LOW_STORAGE_THRESHOLD_BYTES = 50000000;
    public static final long MAX_STORAGE_THRESHOLD_BYTES = 2000000000;

    private final View.OnClickListener mButtonClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(v == mButtonOpen){
                mCamera = openCamera(firstBack);
                startCameraOnePreview();
            }else if (v == mButtonClose){
                closeCamera(mCamera, firstBack);
            }else if (v == mButtonOpen2){
                mCamera2 = openCamera(firstFront);
                startCameraTwoPreview();
            }else if (v == mButtonClose2){
                closeCamera(mCamera2, firstFront);
            }
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "mSurfaceListener onSurfaceTextureAvailable");
            mSurfaceTexture = surface;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "mSurfaceListener onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG, "mSurfaceListener onSurfaceTextureDestroyed");
            if(mCamera!=null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            mSurfaceTexture = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceListener2 = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "mSurfaceListener2 onSurfaceTextureAvailable");
            mSurfaceTexture2 = surface;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "mSurfaceListener2 onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG, "mSurfaceListener2 onSurfaceTextureDestroyed");
            if(mCamera2!=null){
                mCamera2.stopPreview();
                mCamera2.release();
                mCamera2 = null;
            }
            mSurfaceTexture2 = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * Checks if any of the needed Android runtime permissions are missing.
     * If they are, then launch the permissions activity under one of the following conditions:
     * a) The permissions dialogs have not run yet. We will ask for permission only once.
     * b) If the missing permissions are critical to the app running, we will display a fatal error dialog.
     * Critical permissions are: camera, microphone and storage. The app cannot run without them.
     * Non-critical permission is location.
     */
    private void checkPermissions() {
        if (!ApiHelper.isMOrHigher()) {
            Log.v(TAG, "not running on M, skipping permission checks");
            mHasCriticalPermissions = true;
            return;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHasCriticalPermissions = true;
        } else {
            mHasCriticalPermissions = false;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !mHasCriticalPermissions) {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void requestMultiplePermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};
        requestPermissions(permissions, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestMultiplePermissions();
        checkPermissions();
        if (!mHasCriticalPermissions) {
            Log.v(TAG, "onCreate: Missing critical permissions.");
            finish();
            return;
        }

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        CameraUtil.initialize(this);
        getCameraDeviceInfo();

        TextureView textureView = (TextureView)findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(mSurfaceListener);

        TextureView textureView2 = (TextureView)findViewById(R.id.texture2);
        textureView2.setSurfaceTextureListener(mSurfaceListener2);

        mButtonOpen = (Button)findViewById(R.id.open_camera);
        mButtonOpen.setOnClickListener(mButtonClickListener);
        mButtonClose = (Button)findViewById(R.id.close_camera);
        mButtonClose.setOnClickListener(mButtonClickListener);

        Button capture = (Button)findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureByCameraOne();
            }
        });

        mButtonStartRecorder = (Button)findViewById(R.id.start_recorder);
        mButtonStartRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideoRecorder(firstBack);
            }
        });

        mButtonStopRecorder = (Button)findViewById(R.id.stop_recorder);
        mButtonStopRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVideoRecorder();
            }
        });

        mButtonOpen2 = (Button)findViewById(R.id.open_camera2);
        mButtonOpen2.setOnClickListener(mButtonClickListener);
        mButtonClose2 = (Button)findViewById(R.id.close_camera2);
        mButtonClose2.setOnClickListener(mButtonClickListener);

        mButtonStartRecorder = (Button)findViewById(R.id.start_recorder);
        mButtonStopRecorder = (Button)findViewById(R.id.stop_recorder);

    }

    private void getCameraDeviceInfo(){
        int numberOfCameras;
        try {
            numberOfCameras = Camera.getNumberOfCameras();
            mCameraNumber = numberOfCameras;
            cameraInfos = new Camera.CameraInfo[numberOfCameras];
            for (int i = 0; i < numberOfCameras; i++) {
                cameraInfos[i] = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfos[i]);
            }
        } catch (RuntimeException ex) {
            Log.e(TAG, "Exception while creating CameraDeviceInfo", ex);
            return;
        }

        firstFront = NO_DEVICE;
        firstBack = NO_DEVICE;
        // Get the first (smallest) back and first front camera id.
        for (int i = numberOfCameras - 1; i >= 0; i--) {
            if (cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                firstBack = i;
            } else {
                if (cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    firstFront = i;
                }
            }
        }
        Log.i(TAG,"getCameraDeviceInfo  mCameraNumber:"+mCameraNumber+
                ", firstBack:"+firstBack+", firstFront:"+firstFront);
    }

/*    public int getDisplayRotation() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private static final int LOG_THRESHOLD_MILLIS = 10;
    private static final boolean LOG_ALL_REQUESTS = false;
    public Object getSystemService(String service) {
        try {
            long start = System.currentTimeMillis();
            Object result = getSystemService(service);
            long duration = System.currentTimeMillis() - start;
            if (duration > LOG_THRESHOLD_MILLIS) {
                Log.w(TAG, "Warning: providing system service " + service + " took " +
                        duration + "ms");
            } else if (LOG_ALL_REQUESTS) {
                Log.v(TAG, "Provided system service " + service + " in " + duration + "ms");
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }*/


    private Camera openCamera(int id){
        if(mSurfaceTexture2 == null || mSurfaceTexture == null){
            Log.i(TAG,"mSurfaceTexture || mSurfaceTexture2 not prepared");
            return null;
        }
        Camera camera;
        camera = Camera.open(id);
        return camera;
    }

    private void closeCamera(Camera camera, int id){
        if(camera == null){
            Log.i(TAG,"closeCamera  camera == null  id:" + id);
            return;
        }
        Log.i(TAG,"closeCamera  id:" + id);
        camera.stopPreview();
        camera.release();
    }

    private void startCameraOnePreview(){
        if(mCamera == null){
            return;
        }
        //getDisplayRotation();
        mCamera.setDisplayOrientation(cameraInfos[firstBack].orientation);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateCameraParameter(mCamera);
        mCamera.startPreview();
    }

    private void updateCameraParameter(Camera camera){
        if(camera == null) return;

        Camera.Parameters param = camera.getParameters();
        if(param == null){
            Log.i(TAG, "updateCameraParameter  param is null !!!");
            return;
        }

        List<String> focusModes = param.getSupportedFocusModes();
        for(String focusMode :focusModes){
            if(focusMode.contains("auto")){
                Log.i(TAG,"setFocusMode :"+focusMode);
                param.setFocusMode(focusMode);
            }
        }
        if(param.getSupportedPreviewFormats().contains(ImageFormat.NV21)){
            param.setPreviewFormat(ImageFormat.NV21);
        }

        List<Camera.Size> previewList = param.getSupportedPictureSizes();
        for (Camera.Size size: previewList){
            if(size.width == CameraUtil.getsScreenHeight()
                    && size.height == CameraUtil.getScreenWidth()){
                Log.i(TAG,"setPreviewSize :" + size.width+"x" + size.height);
                param.setPreviewSize(size.width, size.height);
            }
        }

        List<Camera.Size> pictureList = param.getSupportedPictureSizes();
        Camera.Size pictureSize = null;
        for (Camera.Size picture: pictureList){
            if(pictureSize == null){
                pictureSize = picture;
            }
            if((picture.height*picture.width) >= (pictureSize.height*pictureSize.width)){
                pictureSize = picture;
            }
        }
        Log.i(TAG,"setPictureSize :" + pictureSize.width + "x" + pictureSize.height);
        param.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(param);
    }

    private void takePictureByCameraOne(){
        if(mCamera == null) return;

        Storage.generateStoragePath();
        mPictureName = Storage.createPictureName(System.currentTimeMillis());

        mCamera.takePicture(mJpegPictureCallback, mRawPictureCallback, mJpegPictureCallback);
    }

    private void startCameraTwoPreview(){
        if(mCamera2 == null){
            return;
        }
        //getDisplayRotation();
        mCamera2.setDisplayOrientation(cameraInfos[firstFront].orientation);
        try {
            mCamera2.setPreviewTexture(mSurfaceTexture2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateCameraParameter(mCamera2);
        mCamera2.startPreview();
    }

    protected void readVideoPreferences() {
        if(!mPreferenceRead) {
            Log.d(TAG, "readBackCameraVideoPreferences");

            int quality = CamcorderProfile.QUALITY_HIGH;

            // If quality is not supported, request QUALITY_HIGH which is always supported.
            if (CamcorderProfile.hasProfile(firstBack, quality) == false) {
                quality = CamcorderProfile.QUALITY_HIGH;
            }
            mProfile = CamcorderProfile.get(firstBack, quality);
            mPreferenceRead = true;
        }
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    private void generateVideoFilename(int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = Storage.createVideoName(dateTaken);
        // Used when emailing.
        String filename = title + convertOutputFormatToFileExt(outputFileFormat);
        String mime = convertOutputFormatToMimeType(outputFileFormat);
        String path = Storage.DIRECTORY + '/' + filename;
        String tmpPath = path + ".tmp";
        mCurrentVideoValues = new ContentValues(9);
        mCurrentVideoValues.put(MediaStore.Video.Media.TITLE, title);
        mCurrentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaStore.MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATA, path);
        mCurrentVideoValues.put(MediaStore.Video.Media.WIDTH, mProfile.videoFrameWidth);
        mCurrentVideoValues.put(MediaStore.Video.Media.HEIGHT, mProfile.videoFrameHeight);
        mCurrentVideoValues.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(mProfile.videoFrameWidth) + "x" +
                        Integer.toString(mProfile.videoFrameHeight));
/*        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mCurrentVideoValues.put(MediaStore.Video.Media.LATITUDE, loc.getLatitude());
            mCurrentVideoValues.put(MediaStore.Video.Media.LONGITUDE, loc.getLongitude());
        }*/
        mVideoFilenameTMP = tmpPath;
        Log.v(TAG, "New video filename: " + mVideoFilenameTMP);
    }

    // Prepares media recorder.
    private void initializeRecorder(Camera camera) {
        Log.i(TAG, "initializeRecorder: " + Thread.currentThread());
        // If the mCameraDevice is null, then this activity is going to finish
        if (camera == null) {
            Log.w(TAG, "null camera proxy, not recording");
            return;
        }
        readVideoPreferences();
        long requestedSizeLimit = 0;
        //closeVideoFileDescriptor();
        mCurrentVideoUriFromMediaSaved = false;

        mMediaRecorder = new MediaRecorder();

        camera.unlock();
        mMediaRecorder.setCamera(camera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(mProfile);
        mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        //mMediaRecorder.setMaxDuration(mMaxVideoDurationInMs);

        //setRecordLocation();

        // Set output file.
        // Try Uri in the intent first. If it doesn't exist, use our own
        // instead.

        generateVideoFilename(mProfile.fileFormat);
        mMediaRecorder.setOutputFile(mVideoFilenameTMP);

        // Set maximum file size.
        long maxFileSize = MAX_STORAGE_THRESHOLD_BYTES;//mActivity.getStorageSpaceBytes() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if (requestedSizeLimit > 0 && requestedSizeLimit < maxFileSize) {
            maxFileSize = requestedSizeLimit;
        }

        try {
            mMediaRecorder.setMaxFileSize(maxFileSize);
        } catch (RuntimeException exception) {
            // We are going to ignore failure of setMaxFileSize here, as
            // a) The composer selected may simply not support it, or
            // b) The underlying media framework may not handle 64-bit range
            // on the size restriction.
        }

        int sensorOrientation = cameraInfos[firstBack].orientation;
                //mActivity.getCameraProvider().getCharacteristics(mCameraId).getSensorOrientation();
        int deviceOrientation = 0;//
                //mAppController.getOrientationManager().getDeviceOrientation().getDegrees();
        int rotation = (sensorOrientation + deviceOrientation)%180;//CameraUtil.getImageRotation(
                //sensorOrientation, deviceOrientation, isCameraFrontFacing());
        mMediaRecorder.setOrientationHint(rotation);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFilenameTMP, e);
            releaseMediaRecorder();
            camera.lock();
            throw new RuntimeException(e);
        }

        mMediaRecorder.setOnErrorListener(mOnErrorMediaRecorder);
        mMediaRecorder.setOnInfoListener(mInfoMediaRecorder);
    }

    private void cleanupEmptyFile() {
        if (mVideoFilenameTMP != null) {
            File f = new File(mVideoFilenameTMP);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mVideoFilenameTMP);
                mVideoFilenameTMP = null;
            }
        }
    }

    private void releaseMediaRecorder() {
        Log.i(TAG, "releaseMediaRecorder");
        if (mMediaRecorder != null) {
            cleanupEmptyFile();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mVideoFilenameTMP = null;
    }

    private void startVideoRecorder(int id){
        Camera videoCamera;
        if(firstFront == id){
            videoCamera = mCamera2;
        }else {
            videoCamera = mCamera;
        }
        if(videoCamera == null){
            Log.i(TAG, "startVideoRecorder  videoCamera is null!!!");
            return;
        }

        initializeRecorder(videoCamera);
        try {
            mMediaRecorder.start(); // Recording is now started
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not start media recorder. ", e);
            releaseMediaRecorder();
            videoCamera.lock();
            return;
        }
        mRecordingStartTime = System.currentTimeMillis();
        mIsVideoRecording = true;
        Toast.makeText(this,"start video recording", Toast.LENGTH_SHORT).show();
    }

    private void deleteVideoFile(String fileName) {
        Log.v(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    private void stopVideoRecorder(){
        if(mIsVideoRecording){
            boolean shouldAddToMediaStoreNow = false;

            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.stop();
                shouldAddToMediaStoreNow = true;
                mCurrentVideoFilename = mVideoFilenameTMP;
                Log.v(TAG, "stopVideoRecording: current video filename: " + mCurrentVideoFilename);
            } catch (RuntimeException e) {
                Log.e(TAG, "stop fail",  e);
                if (mVideoFilenameTMP != null) {
                    deleteVideoFile(mVideoFilenameTMP);
                }
               // fail = true;
            }

            if (shouldAddToMediaStoreNow /*&& !fail*/) {
                saveVideoToFile();
            }
        }else {
            Log.i(TAG, "stopVideoRecorder  not started");
        }
        mIsVideoRecording = false;
        releaseMediaRecorder();
        if(mCamera != null) {
            mCamera.lock();
        }
    }

    private void saveVideoToFile(){
        if (mVideoFileDescriptor == null) {
            long duration = SystemClock.uptimeMillis() - mRecordingStartTime;
            if (duration > 0) {
                //
            } else {
                Log.w(TAG, "Video duration <= 0 : " + duration);
            }
            mCurrentVideoValues.put(MediaStore.Video.Media.SIZE, new File(mCurrentVideoFilename).length());
            mCurrentVideoValues.put(MediaStore.Video.Media.DURATION, duration);
        }
        Uri uri = null;
        try {
            Uri videoTable = Uri.parse(Storage.VIDEO_BASE_URI);
            uri = getContentResolver().insert(videoTable, mCurrentVideoValues);

            // Rename the video file to the final name. This avoids other
            // apps reading incomplete data.  We need to do it after we are
            // certain that the previous insert to MediaProvider is completed.
            String finalName = mCurrentVideoValues.getAsString(MediaStore.Video.Media.DATA);
            File finalFile = new File(finalName);
            if (new File(mCurrentVideoFilename).renameTo(finalFile)) {
                mCurrentVideoFilename = finalName;
            }
            getContentResolver().update(uri, mCurrentVideoValues, null, null);
            Toast.makeText(this,"save video :"+mCurrentVideoFilename, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // We failed to insert into the database. This can happen if
            // the SD card is unmounted.
            Log.e(TAG, "failed to add video to media store", e);
            uri = null;
        } finally {
            Log.v(TAG, "Current video URI: " + uri);
        }
        mCurrentVideoValues = null;
    }

    private final MediaRecorder.OnErrorListener mOnErrorMediaRecorder = new MediaRecorder.OnErrorListener(){

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e(TAG,"mOnErrorMediaRecorder  onError:"+ what);
        }
    };

    private final MediaRecorder.OnInfoListener mInfoMediaRecorder = new MediaRecorder.OnInfoListener(){

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.e(TAG,"mInfoMediaRecorder  onInfo:"+ what);
        }
    };

    protected final JpegPictureCallback mJpegPictureCallback = new JpegPictureCallback();
    protected final RawPictureCallback mRawPictureCallback = new RawPictureCallback();

    final class RawPictureCallback implements Camera.PictureCallback{

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG,"RawPictureCallback $$ onPictureTaken");
        }
    }

    final class JpegPictureCallback implements Camera.PictureCallback, Camera.ShutterCallback{

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG,"JpegPictureCallback $$ onPictureTaken");
            //final ExifInterface exif = Exif.getExif(data);
            String filePath = Storage.generateFilepath(Storage.DIRECTORY, mPictureName,
                    Storage.MIME_TYPE_JPEG);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int width = options.outWidth;
            int height = options.outHeight;
            Storage.saveDataToFile(filePath, data);
            Storage.addImageToMediaStore(MainActivity.this.getContentResolver(), mPictureName,
                    System.currentTimeMillis(), null, 90, data.length, filePath, width, height,
                    Storage.MIME_TYPE_JPEG);
            updateCameraParameter(camera);
            camera.startPreview();
            Toast.makeText(MainActivity.this, "save picture:"+filePath, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShutter() {
            Log.i(TAG,"JpegPictureCallback $$ onShutter");
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
