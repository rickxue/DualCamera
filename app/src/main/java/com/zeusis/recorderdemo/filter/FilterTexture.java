package com.zeusis.recorderdemo.filter;

import android.opengl.GLES20;
import android.util.Log;

public class FilterTexture extends TextureDecorator {

    private static final String TAG = "FilterTexture";

    private int[] mFilterTextureIds = new int[1];
    //private int[] mFilterTextureFboIds = new int[1];

    //private int[] mLastFboIds = new int[1];

    private int mFilterShaderProgram;
    
    public FilterTexture(TextureViewManager textureViewManger, int width, int height, AbstractTexture abstractTextureEntity) {
        super(abstractTextureEntity, width, height);
        mTextureViewManger = textureViewManger;
    }

    @Override
    public void initEntity() {
        // TODO Auto-generated method stub
        super.initEntity();

        LetvGlApi.createTexture(mFilterTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
        //GLES20.glGenFramebuffers(1, mFilterTextureFboIds, 0);

        String filterVertexShader = LetvGlApi.loadShaderFromAssetsFile("filter/filter_vertex.sh", mTextureViewManger.getCameraActivity().getResources());
        String filterfragmentShader = LetvGlApi.loadShaderFromAssetsFile("filter/filter_frag.sh", mTextureViewManger.getCameraActivity().getResources());
        mFilterShaderProgram = LetvGlApi.createVertexFragmentProgram(filterVertexShader, filterfragmentShader);
        Log.i(TAG, "mFilterShaderProgram: " + mFilterShaderProgram);
    }

    @Override
    public void drawEntity(float[] transformMatrix) {
        // TODO Auto-generated method stub
    	drawFilterTexture(transformMatrix);
    }

    @Override
    public void destoryEntity() {
        // TODO Auto-generated method stub
        GLES20.glDeleteProgram(mFilterShaderProgram);

        LetvGlApi.deleteTexture(mFilterTextureIds);
    }

    @Override
    public void changeSize(int width, int height) {
        // TODO Auto-generated method stub
        super.changeSize(width, height);
        LetvGlApi.resizeTexture(mFilterTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
    }

    private boolean drawFilterTexture(float[] transformMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterTextureIds[0]);

/*        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mLastFboIds, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFilterTextureFboIds[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                mFilterTextureIds[0], 0);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);*/

        if (mTextureViewManger.isRenderCellFilter()) {
        	mEntity.doCellFilters(transformMatrix, 0);//mFilterTextureFboIds[0]);
        } else {
        	
        	mEntity.doEffectFilter(mTextureViewManger.getCurrentEffectIndex(), transformMatrix, 0);//mFilterTextureFboIds[0]);
        }

/*        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mLastFboIds[0]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE16);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterTextureIds[0]);

        GLES20.glUseProgram(mFilterShaderProgram);
        drawFilterTextureEntity(mFilterShaderProgram, GLES20.GL_TEXTURE16);*/
        return true;
    }

    private void drawFilterTextureEntity(int program, int texture) {
        int textureParamHandle = GLES20.glGetUniformLocation(program, "texture");
        GLES20.glUniform1i(textureParamHandle, texture - GLES20.GL_TEXTURE0);
        mEntity.drawEntity(program);
    }

}