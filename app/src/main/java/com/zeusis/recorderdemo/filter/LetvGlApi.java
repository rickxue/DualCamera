package com.zeusis.recorderdemo.filter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;


public final class LetvGlApi {
    //private static final String TAG = "LetvGlApi";
    private static final String TAG = new String("LetvGlApi");
    public static final String LETV_VERTEX_SHADER_CODE_FBO_TEXTURE = "attribute vec4 vPosition;\n"
            + "attribute vec4 vTexCoordinate;\n" + "uniform mat4 textureTransform;\n"
            // + "attribute vec2 vTexCoordinate;\n"
            + "varying vec2 v_TexCoordinate;\n" + "void main() {"
            + "   v_TexCoordinate = ( vTexCoordinate).xy;"
            // + " v_TexCoordinate = vTexCoordinate;"
            + "   gl_Position = vPosition; }";

    public static final String LETV_VERTEX_SHADER_CODE_OES_TEXTURE = "attribute vec4 vPosition;\n"
            + "attribute vec4 vTexCoordinate;\n" + "uniform mat4 textureTransform;\n"
            // + "attribute vec2 vTexCoordinate;\n"
            + "varying vec2 v_TexCoordinate;\n" + "void main() {" + "   v_TexCoordinate = ( textureTransform * vTexCoordinate).xy;"
            // + " v_TexCoordinate = vTexCoordinate;"
            + "   gl_Position = vPosition; }";

    public static final String LETV_FRAGMENT_SHADER_CODE_FBO_TEXTURE = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            // + "uniform samplerExternalOES texture;"
            + "uniform sampler2D texture;\n" + "varying vec2 v_TexCoordinate;\n" + "void main () {"
            + "    vec4 color = texture2D(texture, v_TexCoordinate);" + "    gl_FragColor = color; }";

    public static final String LETV_FRAGMENT_SHADER_CODE_OES_TEXTURE = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n" + "uniform samplerExternalOES texture;\n"
            // + "uniform sampler2D texture;\n"
            + "varying vec2 v_TexCoordinate;\n" + "void main () {"
            + "    vec4 color = texture2D(texture, v_TexCoordinate);" + "    gl_FragColor = color; }";

    private static final int EGL_OPENGL_ES2_BIT = 4;

    private static Context sContext = null;

    private LetvGlApi() {
    }

    public static void init(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

    public static int createTextures(int[] textureIds, int offset) {
        if (textureIds == null || textureIds.length <= offset)
            return -1;

        GLES20.glGenTextures(textureIds.length, textureIds, offset);
        // editTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIds[offset], 0,
        // 0);

        return textureIds[offset];
    }
    
    public static void createTexture(int[] textureIds,int target,int width,int height){
        GLES20.glGenTextures(textureIds.length, textureIds, 0);
        Log.i(TAG, "createTexture width="+width+", height="+height);
        for(int i=0;i<textureIds.length;i++){
            GLES20.glBindTexture(target, textureIds[i]);
            GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,    GLES20.GL_UNSIGNED_BYTE, null);
        }
    }
    
    public static void resizeTexture(int[] textureIds,int target,int width,int height){
        Log.i(TAG, "resizeTexture width="+width+", height="+height);
        for(int i=0;i<textureIds.length;i++){
            GLES20.glBindTexture(target, textureIds[i]);
            GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,    GLES20.GL_UNSIGNED_BYTE, null);
        }
    }

    public static void deleteTextures(int[] textureIds, int offset) {
        if (textureIds != null && textureIds.length > 0)
            GLES20.glDeleteTextures(textureIds.length, textureIds, 0);
    }
    
    public static void deleteTexture(int[] textureIds){
        GLES20.glDeleteTextures(textureIds.length, textureIds, 0);
    }

    public static void editTexture(int target, int texId, int width, int height) {
        GLES20.glBindTexture(target, texId);
        printGlErrorLog("glBindTexture failed, Target: " + target + ", Id: " + texId);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        printGlErrorLog("glTexParameteri error, Target: " + target + ", Id: " + texId);
    }

    public static int loadShader(int shaderType, String shaderCode) {
        int handle = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(handle, shaderCode);
        GLES20.glCompileShader(handle);

        int[] status = new int[1];
        GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ": " + GLES20.glGetShaderInfoLog(handle));
            GLES20.glDeleteShader(handle);
            handle = 0;
        }
        return handle;
    }

    public static int createVertexFragmentProgram(String vertexShader, String fragmentShader) {
        int vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        if (vertexShaderHandle == 0)
            return 0;
        int fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        if (fragmentShaderHandle == 0)
            return 0;

        int shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
        GLES20.glLinkProgram(shaderProgram);

        int[] status = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "Could not link shader: " + GLES20.glGetProgramInfoLog(shaderProgram));
            GLES20.glDeleteProgram(shaderProgram);
            shaderProgram = 0;
        }

        return shaderProgram;
    }

    public static EGLConfig getEglConfig(EGL10 egl, EGLDisplay display) {
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = new int[] { EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0, EGL10.EGL_NONE };
        int []configAttribs = {
                EGL10.EGL_BUFFER_SIZE, 32,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };

        return (egl.eglChooseConfig(display, configAttribs, configs, 1, configsCount) && configsCount[0] > 0) ? configs[0]
                : null;
    }

    public static void printGlErrorLog(String tag) {
        int _error = GLES20.glGetError();
        // Log.e("printGLErrorLog", tag + ": " + _error);
        if (_error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, tag + ": " + GLUtils.getEGLErrorString(_error));
        }
    }
    
    public static String loadShaderFromAssetsFile(String fname, Resources r) {
        String result = null;
        try {
            InputStream in = r.getAssets().open(fname);
            int ch = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}