package com.zeusis.recorderdemo.filter;

import android.opengl.GLES20;
import android.os.Message;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

public class TransitionAnimation extends TextureDecorator implements IAnimationController {
    private static final String TAG = "TransitionAnimationEntity";

    public boolean mPlayTransitionAnimation = false;
    public boolean mReadyTransitionTexture = false;
    public boolean mResizeMessageFlag = false;

    private AnimationTimer mAnimationTimer = null;
    
    private int mCurrentFrameTexutreId;
    private int mTransitionAnimationShaderProgram;
    
    public TransitionAnimation(TextureViewManager textureViewManger, int width, int height, AbstractTexture abstractTextureEntity) {
        super(abstractTextureEntity, width, height);
        // TODO Auto-generated constructor stub
        mTextureViewManger = textureViewManger;
    }

    @Override
    public void initEntity() {
        // TODO Auto-generated method stub
        super.initEntity();
        mAnimationTimer = new AnimationTimer();
        mAnimationTimer.setInterpolator(new AccelerateInterpolator());

        String transitionAnimationVertexShader = LetvGlApi.loadShaderFromAssetsFile("filter/transition_animation_vertex.sh", mTextureViewManger.getCameraActivity().getResources());
        String transitionAnimationFragmentShader = LetvGlApi.loadShaderFromAssetsFile("filter/transition_animation_frag.sh", mTextureViewManger.getCameraActivity().getResources());
        mTransitionAnimationShaderProgram = LetvGlApi.createVertexFragmentProgram(transitionAnimationVertexShader, transitionAnimationFragmentShader);
        Log.i(TAG, "mTransitionAnimationShaderProgram: " + mTransitionAnimationShaderProgram);
    }

    @Override
    public void drawEntity(float[] transformMatrix) {
        // TODO Auto-generated method stub
        drawTransitionAnimation();
    }

    @Override
    public void destoryEntity() {
        // TODO Auto-generated method stub
        GLES20.glDeleteProgram(mTransitionAnimationShaderProgram);
    }

    @Override
    public void changeSize(int width, int height) {
        // TODO Auto-generated method stub
        super.changeSize(width, height);
    }

    @Override
    public void startAnimation(int durationMsec) {
        // TODO Auto-generated method stub
        mPlayTransitionAnimation = true;
        mReadyTransitionTexture = true;
        mResizeMessageFlag = true;
        mAnimationTimer.setDuration(durationMsec);
    }

    @Override
    public void stopAnimation() {
        // TODO Auto-generated method stub
        mPlayTransitionAnimation = false;
        if (mAnimationTimer.getProgress() < 1)
            return;
        if (mAnimationTimer != null)
        	mAnimationTimer.forceStop();
    }

    @Override
    public boolean isPlayingAnimation() {
        // TODO Auto-generated method stub
        return mPlayTransitionAnimation;
    }

    private boolean drawTransitionAnimation() {
        if (mReadyTransitionTexture) {
            mCurrentFrameTexutreId = mEntity.getCurrentFrameTextureId();
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
            mAnimationTimer.start();
            mAnimationTimer.setStartTime(System.currentTimeMillis());
            mReadyTransitionTexture = false;
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        boolean result = mAnimationTimer.calculate(currentTimeMillis);
        if (mAnimationTimer.getProgress() < 0.8f) {
            drawTransitionAnimation(mAnimationTimer.getProgress());
            if (isPlayingAnimation()) {
                mTextureViewManger.requestRender();
            }
        } else {
            if (mResizeMessageFlag) {
                sendMessageResizeTextView();
                mResizeMessageFlag = false;
            }
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        }

        return true;
    }

    private void drawTextureBlend(int program, float duration) {
        int linearInterpolationDuration = GLES20.glGetUniformLocation(program, "uT");
        GLES20.glUniform1f(linearInterpolationDuration, duration);

        int textureParamHandle = GLES20.glGetUniformLocation(program, "uSamplerTex");
        GLES20.glUniform1i(textureParamHandle, 0);
        
        int textureBlendParamHandle = GLES20.glGetUniformLocation(program, "uSamplerTex_Blend");
        GLES20.glUniform1i(textureBlendParamHandle, 1);

        mEntity.drawEntity(program);
    }

    private boolean drawTransitionAnimation(float duration) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        LetvGlApi.printGlErrorLog("glClear");

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGaussBlurTextureIds[1]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterTextureIds[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCurrentFrameTexutreId);
        GLES20.glUseProgram(mTransitionAnimationShaderProgram);
        drawTextureBlend(mTransitionAnimationShaderProgram, duration);
        return true;
    }
    
    public void sendMessageResizeTextView() {
        Message message = mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().obtainMessage();
        message.what = 2;
        mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().sendMessage(message);
    }


}
