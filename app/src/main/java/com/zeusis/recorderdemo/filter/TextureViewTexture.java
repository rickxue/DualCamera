package com.zeusis.recorderdemo.filter;

import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

public class TextureViewTexture  extends AbstractTexture{
    private static final String TAG = "TextureViewTexture";

    private int mTextureViewTextureIds[] = new int[1];
    private int mTextureViewShaderProgram;
    
    
    private int[] mLastFboIds = new int[1];
    private int[] mCurrentFrameFboIds = new int[1];
    private int mCurrentFrameTextureIds[] = new int[1];
    
    private int[] mFilterTextureFboId = new int[1];
    private int[] mFilterTextureIds = new int[1];
    
    private float[] mTransformMatrix = new float[16];
    
    public TextureViewTexture(TextureViewManager textureViewManger,int width, int height) {
        super(width, height);
        // TODO Auto-generated constructor stub
        mTextureViewManger = textureViewManger;
    }

    @Override
    public void initEntity() {
        // TODO Auto-generated method stub
        super.initEntity();
        LetvGlApi.createTexture(mTextureViewTextureIds, GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mWidth, mHeight);
        LetvGlApi.createTexture(mCurrentFrameTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
        
        String textureviewVertexShader =LetvGlApi.loadShaderFromAssetsFile("filter/textureview_vertex.sh", mTextureViewManger.getCameraActivity().getResources());
        String textureviewfragmentShader =LetvGlApi.loadShaderFromAssetsFile("filter/textureview_frag.sh", mTextureViewManger.getCameraActivity().getResources());  
        mTextureViewShaderProgram = LetvGlApi.createVertexFragmentProgram(textureviewVertexShader ,textureviewfragmentShader);
        Log.i(TAG, "mTextureViewShaderProgram: " + mTextureViewShaderProgram);
        
        LetvGlApi.createTexture(mFilterTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
        
        GLES20.glGenFramebuffers(1, mCurrentFrameFboIds, 0);
        
        GLES20.glGenFramebuffers(1, mFilterTextureFboId, 0);
    }

    @Override
    public void drawEntity(float[] transformMatrix) {
        // TODO Auto-generated method stub
    	drawTextureViewTexture(transformMatrix);
        mTransformMatrix = transformMatrix;
    }

    @Override
    public void destoryEntity() {
        // TODO Auto-generated method stub
        GLES20.glDeleteProgram(mTextureViewShaderProgram);
        
        LetvGlApi.deleteTexture(mTextureViewTextureIds);
        LetvGlApi.deleteTexture(mCurrentFrameTextureIds);
        LetvGlApi.deleteTexture(mFilterTextureIds);
    }

    @Override
    public void changeSize(int width, int height) {
        // TODO Auto-generated method stub
        super.changeSize(width, height);
        LetvGlApi.resizeTexture(mTextureViewTextureIds, GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mWidth, mHeight);
        LetvGlApi.resizeTexture(mCurrentFrameTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
        LetvGlApi.resizeTexture(mFilterTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
    }

    @Override
    public void doCellFiltersAnimation(float[] transformMatrix, int filterTextureFboId,int indexOfCell, boolean isZoomIn, float progress) {
        long startTime = System.currentTimeMillis();

        Rect texRect = new Rect(0, 0, mWidth, mHeight);
        int ratio = mTextureViewManger.getCameraPictureSizeRatio();
        if (ratio == 1) {// clip texRect for 16:9
            texRect.set(0, 0, mWidth, mHeight - mHeight / 4);
        }
        Rect outputFboRect = new Rect(0, 0, mWidth, mHeight);
        int lRes = 0;
        boolean isFind = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if((i*3+j)==indexOfCell){
                    isFind = true;
                    continue;
                }
                outputFboRect.set(mTextureViewManger.indexFilterCellInFilter(i, j));
                mTextureViewManger.renderFilterEffectWithFbo(FilterUtil.indexFilterEffectId(i * 3 + j), mTextureViewTextureIds[0],
                        texRect, mWidth, mHeight, filterTextureFboId, outputFboRect, mWidth, mHeight,
                        transformMatrix);
            }
        }
        if(isFind){
            outputFboRect.set(getCurrentAnimationFilterRect(indexOfCell, isZoomIn, progress));
            mTextureViewManger.renderFilterEffectWithFbo(FilterUtil.indexFilterEffectId(indexOfCell), mTextureViewTextureIds[0],
                    texRect, mWidth, mHeight, filterTextureFboId, outputFboRect, mWidth, mHeight,
                    transformMatrix);
        }
        Log.d(TAG, "doStep Result: " + lRes + "   TimeCost: " + (System.currentTimeMillis() - startTime));
    }

    private Rect getCurrentAnimationFilterRect(int indexOfCell, boolean isZoomIn, float progress){
        int column = indexOfCell/3;
        int row = indexOfCell%3;
        Rect originRect = mTextureViewManger.indexFilterCellInFilter(column, row);
        Rect displayRect = new Rect(0,0,mWidth,mHeight);
        if(isZoomIn){
            return zoomRect(displayRect, originRect, progress);
        }else {
            return zoomRect(originRect, displayRect, progress);
        }
    }

    private Rect zoomRect(Rect origin, Rect dest, float progress) {
        float left = (float) origin.left * (1 - progress) + (float)dest.left * progress;
        float top = (float)origin.top * (1 - progress) + (float)dest.top * progress;

        float right = (float)origin.right * (1 - progress) + (float)dest.right * progress;
        float bottom = (float)origin.bottom * (1 - progress) + (float)dest.bottom * progress;

        return new Rect((int)left, (int)top, (int)right, (int)bottom);
    }

    @Override
    public void doCellFilters(float[] transformMatrix, int filterTextureFboId) {
        long startTime = System.currentTimeMillis();

        Rect texRect = new Rect(0, 0, mWidth, mHeight);
        int ratio = mTextureViewManger.getCameraPictureSizeRatio();
        if (ratio == 1) {// clip texRect for 16:9
            texRect.set(0, 0, mWidth, mHeight - mHeight / 4);
        }
        Rect outputFboRect = new Rect(0, 0, mWidth, mHeight);
        int lRes = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                outputFboRect.set(mTextureViewManger.indexFilterCellInFilter(i, j));
                mTextureViewManger.renderFilterEffectWithFbo(FilterUtil.indexFilterEffectId(i * 3 + j), mTextureViewTextureIds[0],
                        texRect, mWidth, mHeight, filterTextureFboId, outputFboRect, mWidth, mHeight,
                        transformMatrix);
            }
        }
        Log.d(TAG, "doStep Result: " + lRes + "   TimeCost: " + (System.currentTimeMillis() - startTime));
    }

    @Override
    public void doEffectFilter(int effectId, float[] transformMatrix, int filterTextureFboId) {
        Rect texRect = new Rect(0, 0, mWidth, mHeight);
        Rect outputFboRect = new Rect(0, 0, mWidth, mHeight);
        mTextureViewManger.renderFilterEffectWithFbo(FilterUtil.indexFilterEffectId(effectId), mTextureViewTextureIds[0], texRect,
                mWidth, mHeight, filterTextureFboId, outputFboRect, mWidth, mHeight, transformMatrix);
    }

    @Override
    public int getCurrentFrameTextureId() {
        // TODO Auto-generated method stub
        drawTextureCurrentFrame(mTransformMatrix);
        return mCurrentFrameTextureIds[0];
    }
    
    @Override
    public int getTextureViewTextureId(){
        return mTextureViewTextureIds[0];
    }
    
    private boolean drawTextureViewTexture(float[] transformMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE16);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureViewTextureIds[0]);
        GLES20.glUseProgram(mTextureViewShaderProgram);
        drawTextureViewTextureEntity(mTextureViewShaderProgram, GLES20.GL_TEXTURE16,transformMatrix);
        return true;
    }
    
    private boolean drawTextureCurrentFrame(float[] transformMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        LetvGlApi.printGlErrorLog("glClear");

        // --------------------------------------------- FBO
        // --------------------------------------------------
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mLastFboIds, 0);

        int texture = GLES20.GL_TEXTURE16;
        // GLES20.glActiveTexture(texture);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mCurrentFrameFboIds[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                mCurrentFrameTextureIds[0], 0);

         GLES20.glActiveTexture(texture);
         GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,mTextureViewTextureIds[0]);
        
         GLES20.glUseProgram(mTextureViewShaderProgram);
         drawTextureViewTextureEntity(mTextureViewShaderProgram, texture,transformMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mLastFboIds[0]);

        return true;
    }
    
    
    private void drawTextureViewTextureEntity(int program, int texture,float[] transformMatrix) {
        int textureParamHandle = GLES20.glGetUniformLocation(program, "texture");
        int textureTranformHandle = GLES20.glGetUniformLocation(program, "textureTransform");
        GLES20.glUniform1i(textureParamHandle, texture - GLES20.GL_TEXTURE0);
        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, transformMatrix, 0);
        
        super.drawEntity(program);
    }
    

}
