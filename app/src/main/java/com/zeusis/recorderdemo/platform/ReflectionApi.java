package com.zeusis.recorderdemo.platform;

import android.hardware.Camera;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.media.MediaRecorder;
import android.util.Log;

public class ReflectionApi {
    private static final String TAG = "ReflectionApi";

    private static Class<?> sCameraSizeClass = findClass("android.hardware.Camera$Size");
    private static Method sSetLongshotMethod;
    private static Method sSetZSLModeMethod;
    private static Method sSetCameraModeMethod;
    private static Method sMediaRecorderPause;
    private static Method sDisableShutterSoundMethod;

    static {
        /** Qcom proprietary methods */
        if (PlatformUtil.getCurrentPlatform() == PlatformUtil.Platform.QCOM) {
            sSetLongshotMethod = findMethod(Camera.class, "setLongshot", boolean.class);
            sSetZSLModeMethod = findMethod(Camera.Parameters.class, "setZSLMode", String.class);
            sSetCameraModeMethod = findMethod(Camera.Parameters.class, "setCameraMode", int.class);
        }
        /** AOSP hide methods */
        sMediaRecorderPause = findMethod(MediaRecorder.class, "pause");
        sDisableShutterSoundMethod = findMethod(Camera.class, "disableShutterSound");
    }

    public static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field findField(Class targetClass, String fieldName) {
        Log.v(TAG, "findField : " + fieldName + " in " + targetClass.getName());
        try {
            return targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method findMethod(Class targetClass, String methodName,
                                     Class<?>... parameterTypes) {
        Log.v(TAG, "findMethod : " + methodName + " in " + targetClass.getName());
        try {
            return targetClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean doMethodSuccess(Method method, Object receiver, Object... args) {
        try {
            doMethod(method, receiver, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static java.lang.Object doMethod(Method method, Object receiver, Object... args)
            throws InvocationTargetException, IllegalAccessException {
        if (method != null) {
            String name = method.getName();
            if (receiver == null) {
                Log.w(TAG, "receiver is null when call " + name + " method.");
                return null;
            }
            Log.d(TAG, "has " + name + " method argList : " + args);
            try {
                return method.invoke(receiver, args);
            } catch (InvocationTargetException e) {
                throw e;
            } catch (IllegalAccessException e) {
                throw e;
            }
        }
        return null;
    }

    public static android.hardware.Camera.Size Size(int w, int h) {
        if (sCameraSizeClass != null) {
            try {
                Constructor constructor = sCameraSizeClass.getDeclaredConstructor(
                        android.hardware.Camera.class, int.class, int.class);
                return (android.hardware.Camera.Size) constructor.newInstance(null, w, h);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** setLongshot */
    public static boolean setLongshot(Camera camera, boolean value) {
        return doMethodSuccess(sSetLongshotMethod, camera, value);
    }

    /** setZSLMode */
    public static void setZSLMode(Camera.Parameters parameters, String mode) {
        doMethodSuccess(sSetZSLModeMethod, parameters, mode);
    }

    /** setCameraMode */
    public static void setCameraMode(Camera.Parameters parameters, int mode) {
        doMethodSuccess(sSetCameraModeMethod, parameters, mode);
    }

    /** pause */
    public static void pauseMediaRecorder(MediaRecorder mediaRecorder) {
        doMethodSuccess(sMediaRecorderPause, mediaRecorder);
    }

    /** disableShutterSound */
    public static void disableShutterSound(Camera camera) {
        doMethodSuccess(sDisableShutterSoundMethod, camera);
    }
}
