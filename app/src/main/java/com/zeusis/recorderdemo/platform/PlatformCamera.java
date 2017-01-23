package com.zeusis.recorderdemo.platform;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/** used for set platform's special camera parameters or special API. */
public abstract class PlatformCamera {
    protected static final String ON = "on";
    protected static final String OFF = "off";
    // Parameter key suffix for supported values.
    protected static final String SUPPORTED_VALUES_SUFFIX = "-values";

    private final static String KEY_MODE_REMOVE_OBJECT = "zs_algo_move_obj_removal";
    private final static String KEY_MODE_MOTION_TRACK = "zs_algo_move_obj_track";
    private final static String KEY_MODE_MOTION_TRACK_INTERVAL = "zs_algo_move_obj_track_interval";

    public static final String KEY_AUTO_SCENE_DETECT = "auto_scene_detect";
    public static final String AUTO_SCENE_DETECT_AUTO = "scene_auto";
    public static final String AUTO_SCENE_DETECT_NIGHT = "scene_night";
    public static final String AUTO_SCENE_DETECT_HDR = "scene_hdr";
    public static final String AUTO_SCENE_DETECT_ANTISHAKING = "scene_antishaking";


    /** Qcom proprietary methods START */
    public List<String> getSupportedVideoHighFrameRateModes(Camera.Parameters parameters) {
        return null;
    }

    public List<Camera.Size> getSupportedHfrSizes(Camera.Parameters parameters) {
        return null;
    }

    public void setVideoHighFrameRate(Camera.Parameters parameters, String hfrRate) {
    }

    public void setVideoHighFrameRateOff(Camera.Parameters parameters) {
    }

    public boolean setLongshot(Camera camera, boolean enable) {
        return false;
    }

    public void setZSLMode(Camera.Parameters parameters, boolean enable) {
    }

    public void setCameraMode(Camera.Parameters parameters, int mode) {
    }

    public void setCameraPictureMirror(Camera.Parameters parameters, boolean enable, int rotation) {
    }
    /** Qcom proprietary methods END */

    public void setRemoveObjectMode(Camera.Parameters parameters, boolean enable) {
        parameters.set(KEY_MODE_REMOVE_OBJECT, enable ? ON : OFF);
    }

    public void setMotionTrackMode(Camera.Parameters parameters, boolean enable) {
        parameters.set(KEY_MODE_MOTION_TRACK, enable ? ON : OFF);
    }

    public void setMotionTrackInterval(Camera.Parameters parameters, int interval) {
        parameters.set(KEY_MODE_MOTION_TRACK_INTERVAL, interval);
    }

    public void setCameraEventListener(Camera camera, android.hardware.Camera.ZsAlgoEvtListener callback) {
        if (camera != null) {
            camera.setZsAlgoEvtListener(callback);
        }
    }

    /** Zeusis proprietary methods END */

    /** AOSP hide methods START */
    public void pauseMediaRecorder(MediaRecorder mediaRecorder) {
        ReflectionApi.pauseMediaRecorder(mediaRecorder);
    }

    /** AOSP hide methods END */

    /**
     * {@link android.hardware.Camera.Parameters#split(String str)}.
     * <p>
     * Splits a comma delimited string to an ArrayList of String. Return null if
     * the passing string is null or the size is 0.
     */
    protected ArrayList<String> split(String str) {
        if (str == null) return null;

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList<String>();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    /**
     * {@link android.hardware.Camera.Parameters#splitSize(String str)}.
     * <p>
     * Splits a comma delimited string to an ArrayList of Size. Return null if
     * the passing string is null or the size is 0.
     */
    protected ArrayList<Camera.Size> splitSize(String str) {
        if (str == null) return null;

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<Camera.Size> sizeList = new ArrayList<Camera.Size>();
        for (String s : splitter) {
            Camera.Size size = strToSize(s);
            if (size != null) sizeList.add(size);
        }
        if (sizeList.size() == 0) return null;
        return sizeList;
    }

    /**
     * {@link android.hardware.Camera.Parameters#strToSize(String str)}.
     * <p>
     * Splits a Parses a string (ex: "480x320") to Size object. Return null if
     * the passing string is null.
     */
    private Camera.Size strToSize(String str) {
        if (str == null) return null;

        int pos = str.indexOf('x');
        if (pos != -1) {
            String width = str.substring(0, pos);
            String height = str.substring(pos + 1);
            return ReflectionApi.Size(Integer.parseInt(width), Integer.parseInt(height));
        }
        return null;
    }
}
