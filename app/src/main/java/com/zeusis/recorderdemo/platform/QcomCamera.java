package com.zeusis.recorderdemo.platform;

import android.hardware.Camera;

import java.util.List;

public class QcomCamera extends PlatformCamera {
    private static final String KEY_QC_HFR_SIZE = "hfr-size";
    private static final String KEY_QC_VIDEO_HIGH_FRAME_RATE = "video-hfr";
    private static final String VALUE_QC_VIDEO_HIGH_FRAME_RATE_OFF = OFF;
    private static final String KEY_QC_SNAPSHOT_PICTURE_FLIP = "snapshot-picture-flip";

    private static final String FLIP_MODE_OFF = "off";
    private static final String FLIP_MODE_V = "flip-v";
    private static final String FLIP_MODE_H = "flip-h";
    private static final String FLIP_MODE_VH = "flip-vh";

    @Override
    public List<String> getSupportedVideoHighFrameRateModes(Camera.Parameters parameters) {
        String str = parameters.get(KEY_QC_VIDEO_HIGH_FRAME_RATE + SUPPORTED_VALUES_SUFFIX);
        return split(str);
    }

    @Override
    public List<Camera.Size> getSupportedHfrSizes(Camera.Parameters parameters) {
        String str = parameters.get(KEY_QC_HFR_SIZE + SUPPORTED_VALUES_SUFFIX);
        return splitSize(str);
    }

    @Override
    public void setVideoHighFrameRate(Camera.Parameters parameters, String hfrRate) {
        parameters.set(KEY_QC_VIDEO_HIGH_FRAME_RATE, hfrRate);
    }

    @Override
    public void setVideoHighFrameRateOff(Camera.Parameters parameters) {
        setVideoHighFrameRate(parameters, VALUE_QC_VIDEO_HIGH_FRAME_RATE_OFF);
    }

    @Override
    public boolean setLongshot(Camera camera, boolean enable) {
        return ReflectionApi.setLongshot(camera, enable);
    }

    @Override
    public void setZSLMode(Camera.Parameters parameters, boolean enable) {
        ReflectionApi.setZSLMode(parameters, enable ? ON : OFF);
    }

    @Override
    public void setCameraMode(Camera.Parameters parameters, int mode) {
        ReflectionApi.setCameraMode(parameters, mode);
    }

    @Override
    public void setCameraPictureMirror(Camera.Parameters parameters, boolean enable, int rotation) {
        String flipMode = FLIP_MODE_OFF;
        if (enable) {
            if (rotation == 90 || rotation == 270) {
                flipMode = FLIP_MODE_V;
            } else {
                flipMode = FLIP_MODE_H;
            }
        }
        parameters.set(KEY_QC_SNAPSHOT_PICTURE_FLIP, flipMode);
    }
}
