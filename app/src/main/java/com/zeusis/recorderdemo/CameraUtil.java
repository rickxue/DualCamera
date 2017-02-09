package com.zeusis.recorderdemo;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2016/12/17.
 */

public class CameraUtil {
    private static final String TAG = "xwf";
    private static int sScreenWidth = 0;
    private static int sScreenHeight = 0;
    private static float sScreenDensity = 0;
    private static int sDensityDpi = 0;

    public static void initialize(Context context){
        if (context == null)
            return;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sScreenWidth = metrics.widthPixels;
        sScreenHeight = metrics.heightPixels;
        sScreenDensity = metrics.density;      // 0.75 / 1.0 / 1.5 / 2.0 / 3.0
        sDensityDpi = metrics.densityDpi;  //120 160 240 320 480
        Log.d(TAG, "sScreenDensity = " + sScreenDensity + " sDensityDpi = " + sDensityDpi);
        Log.d(TAG, "sScreenWidth = " + sScreenWidth + " sScreenHeight = " + sScreenHeight);
    }

    public static int getScreenWidth(){
        return sScreenWidth;
    }
    public static int getsScreenHeight(){
        return sScreenHeight;
    }

    public static boolean isPreviewRatioEquals(double ratio, int width, int height){
        if(width == 0 || height == 0){
            return false;
        }
        final double ASPECT_TOLERANCE = 0.001f;
        double targetRatio = (double)height / (double)width;
        double temp = Math.abs(ratio - targetRatio);
        BigDecimal data1 = new BigDecimal(temp);
        BigDecimal data2 = new BigDecimal(ASPECT_TOLERANCE);

        if(data1.compareTo(data2) == -1){
            return true;
        }
        return false;

    }
}
