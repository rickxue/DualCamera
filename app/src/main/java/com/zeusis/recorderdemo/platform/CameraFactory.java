package com.zeusis.recorderdemo.platform;


public class CameraFactory {
    /** return different camera on different platform. */
    private static PlatformCamera mPlatformCamera = null;
    public static PlatformCamera getPlatformCamera() {
        if(mPlatformCamera == null) {
            if (PlatformUtil.getCurrentPlatform() == PlatformUtil.Platform.QCOM) {
                mPlatformCamera =  new QcomCamera();
            }else{
                //default
                mPlatformCamera =  new QcomCamera();
            }
        }
        return mPlatformCamera;
    }
}
