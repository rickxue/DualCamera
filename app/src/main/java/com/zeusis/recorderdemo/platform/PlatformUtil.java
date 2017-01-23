package com.zeusis.recorderdemo.platform;

import android.os.Build;

public class PlatformUtil {
    private static Platform sPlatform = Platform.UNKNOWN;
    private static class Singleton {
        private static final PlatformUtil INSTANCE = new PlatformUtil();
    }

    public static PlatformUtil instance() {
        return Singleton.INSTANCE;
    }

    private PlatformUtil() {
        sPlatform = getCurrentPlatform();
    }

    public enum Platform {
        UNKNOWN, QCOM,
    }

    public static Platform getCurrentPlatform() {
        if (sPlatform != Platform.UNKNOWN) {
            return sPlatform;
        } else {
            if (Build.HARDWARE.startsWith("qcom") || Build.HARDWARE.startsWith("QCOM")
                    || Build.BOARD.startsWith("msm") || Build.BOARD.startsWith("MSM")) {
                sPlatform = Platform.QCOM;
            }
            return sPlatform;
        }
    }

    public static String getProductName() { return Build.PRODUCT; }

    public static String getDeviceName() { return Build.DEVICE; }
}
