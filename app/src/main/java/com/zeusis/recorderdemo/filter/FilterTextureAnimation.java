package com.zeusis.recorderdemo.filter;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.os.Message;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;


public class FilterTextureAnimation extends TextureDecorator implements IAnimationController {
    private static final String TAG = "FilterTextureAnimation";

    private AnimationTimer mAnimationTimer = null;

    public boolean mIsPlayingAnimation = false;
    public boolean mReadyAnimation = false;

    private int mFilterAnimationShaderProgram;

    public static enum AnimationState {
        None, InFilter, OutFilter, ZoomOutFilter, ZoomInFilter
    }

    private AnimationState mAnimationState = AnimationState.None;

    private int[] mLastFboIds = new int[1];
    private int mFilterAnimationTextureIds[] = new int[1];
    private int[] mFilterAnimtionTextureFboIds = new int[1];

    public FilterTextureAnimation(TextureViewManager textureViewManger, int width, int height, AbstractTexture abstractTextureEntity) {
        super(abstractTextureEntity,width,height);
        // TODO Auto-generated constructor stub
        mTextureViewManger = textureViewManger;
    }

    @Override
    public void initEntity() {
        // TODO Auto-generated method stub
        super.initEntity();

        String filterAnimationVertexShader = LetvGlApi.loadShaderFromAssetsFile("filter/filter_animation_vertex.sh", mTextureViewManger.getCameraActivity().getResources());
        String filterAnimationFragmentShader = LetvGlApi.loadShaderFromAssetsFile("filter/filter_animation_frag.sh", mTextureViewManger.getCameraActivity().getResources());
        mFilterAnimationShaderProgram = LetvGlApi.createVertexFragmentProgram(filterAnimationVertexShader, filterAnimationFragmentShader);
        Log.e("TAG", "mFilterAnimationShaderProgram: " + mFilterAnimationShaderProgram);

        LetvGlApi.createTexture(mFilterAnimationTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
        GLES20.glGenFramebuffers(1, mFilterAnimtionTextureFboIds, 0);

        mAnimationTimer = new AnimationTimer();
        mAnimationTimer.setInterpolator(new AccelerateInterpolator());

        MatrixState.setInitStack();
    }

    @Override
    public void drawEntity(float[] transformMatrix) {
        // TODO Auto-generated method stub
    	drawFilterAnimationTexture(transformMatrix);
    }

    @Override
    public void destoryEntity() {
        // TODO Auto-generated method stub
    	GLES20.glDeleteProgram(mFilterAnimationShaderProgram);
    	
        LetvGlApi.deleteTexture(mFilterAnimtionTextureFboIds);
    }

    @Override
    public void changeSize(int width, int height) {
        // TODO Auto-generated method stub
        super.changeSize(width, height);
        LetvGlApi.resizeTexture(mFilterAnimationTextureIds, GLES20.GL_TEXTURE_2D, mWidth, mHeight);
    }

    @Override
    public void startAnimation(int durationMsec) {
        // TODO Auto-generated method stub
        if (mTextureViewManger.getFilterEffectFlag() && mTextureViewManger.isRenderCellFilter()) {
            setAnimationState(AnimationState.InFilter);
        }

        if (!mTextureViewManger.getFilterEffectFlag() && !mTextureViewManger.isRenderCellFilter()) {
            setAnimationState(AnimationState.OutFilter);
        }

        if (mTextureViewManger.getFilterEffectFlag() && !mTextureViewManger.isRenderCellFilter()) {
            setAnimationState(AnimationState.ZoomOutFilter);
        }

        mIsPlayingAnimation = true;
        mReadyAnimation = true;

        mAnimationTimer.start();
        mAnimationTimer.setDuration(durationMsec);
        sendMessageStartAnimation();
    }

    @Override
    public void stopAnimation() {
        // TODO Auto-generated method stub
        setAnimationState(AnimationState.None);
        mIsPlayingAnimation = false;
        sendMessageStopAnimation();
    }

    @Override
    public boolean isPlayingAnimation() {
        // TODO Auto-generated method stub
        return mIsPlayingAnimation;
    }

    private boolean drawFilterAnimationTexture(float[] transformMatrix) {
        long currentTimeMillis = System.currentTimeMillis();
        mAnimationTimer.calculate(currentTimeMillis);
        drawFilterAnimation(mTextureViewManger.getCurrentEffectIndex(), mAnimationTimer.getProgress(), transformMatrix);
        if (mAnimationTimer.getProgress() >= 1.0f) {
            stopAnimation();
        }
        return true;
    }
    
    private boolean drawFilterAnimation(int cellIndex, float progress, float[] transformMatrix) {
        if (mReadyAnimation) {
        	readyFilterAnimationTextue(transformMatrix);
            mReadyAnimation = false;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
/*        if(CameraFeature.isFilterAnimationSupported()) {
            doFilterAnimation2(cellIndex, true, progress, transformMatrix);
        }else*/ {
            doFilterAnimation(mTextureViewManger.getCurrentEffectIndex(), true, progress);
        }
        mTextureViewManger.requestRender();
        return true;
    }

    public void readyFilterAnimationTextue(float[] transformMatrix) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        LetvGlApi.printGlErrorLog("glClear");

        int texture = GLES20.GL_TEXTURE16;
        GLES20.glActiveTexture(texture);
        LetvGlApi.printGlErrorLog("glActiveTexture");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterAnimationTextureIds[0]);
        LetvGlApi.printGlErrorLog("glBindTexture1");

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mLastFboIds, 0);


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFilterAnimtionTextureFboIds[0]);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
        		mFilterAnimationTextureIds[0], 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE){
        	Log.i(TAG, "FramebufferStatus: " + status);
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        mEntity.doCellFilters(transformMatrix, mFilterAnimtionTextureFboIds[0]);

        // Draw texture
        GLES20.glUseProgram(mFilterAnimationShaderProgram);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mLastFboIds[0]);
    }

    private void drawFilterAnimationEntity(int program, int texture) {
    	
        GLES20.glActiveTexture(GLES20.GL_TEXTURE16);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterAnimationTextureIds[0]);

        int muMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);

        int textureParamHandle = GLES20.glGetUniformLocation(program, "uSamplerTex");

        GLES20.glUniform1i(textureParamHandle, GLES20.GL_TEXTURE16 - GLES20.GL_TEXTURE0);
        
        mEntity.drawEntity(program);
    }

    public void doFilterAnimation2Textue(float[] transformMatrix, int indexOfCell, boolean isZoomIn, float progress) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        LetvGlApi.printGlErrorLog("glClear");

        int texture = GLES20.GL_TEXTURE16;
        GLES20.glActiveTexture(texture);
        LetvGlApi.printGlErrorLog("glActiveTexture");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterAnimationTextureIds[0]);
        LetvGlApi.printGlErrorLog("glBindTexture1");

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, mLastFboIds, 0);


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFilterAnimtionTextureFboIds[0]);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                mFilterAnimationTextureIds[0], 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE){
            Log.i(TAG, "FramebufferStatus: " + status);
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        //mEntity.doCellFilters(transformMatrix, mFilterAnimtionTextureFboIds[0]);
        mEntity.doCellFiltersAnimation(transformMatrix, mFilterAnimtionTextureFboIds[0], indexOfCell, isZoomIn, progress);

        // Draw texture
        GLES20.glUseProgram(mFilterAnimationShaderProgram);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mLastFboIds[0]);
    }

    private boolean doFilterAnimation2(int indexOfCell, boolean isZoomIn, float progress, float[] transformMatrix) {
        int sizeRatio = mTextureViewManger.getCameraPictureSizeRatio();
        AnimationState state = getAnimationState();

        switch (state) {
            case InFilter:
                doFilterAnimation2Textue(transformMatrix, indexOfCell, true, progress);
                break;
            case OutFilter:
                doFilterAnimation2Textue(transformMatrix, indexOfCell, false, progress);
                break;
            case ZoomOutFilter:
                doFilterAnimation2Textue(transformMatrix, indexOfCell, false, progress);
                break;
            case ZoomInFilter:
                doFilterAnimation2Textue(transformMatrix, indexOfCell, false, progress);
                break;
            default:
                doFilterAnimation2Textue(transformMatrix, indexOfCell, false, progress);
                break;
        }

        if (sizeRatio == 1) {
            // 16:9
            GLES20.glEnable(GL10.GL_SCISSOR_TEST);
            GLES20.glScissor(0, mHeight / 4, mWidth, mHeight);
            drawFilterAnimationEntity(mFilterAnimationShaderProgram, GLES20.GL_TEXTURE16);
            GLES20.glDisable(GL10.GL_SCISSOR_TEST);
        } else {
            // 1:1 or 4:3
            drawFilterAnimationEntity(mFilterAnimationShaderProgram, GLES20.GL_TEXTURE16);
        }
        return true;
    }


    private boolean doFilterAnimation(int indexOfCell, boolean isZoomIn, float progress) {
        float moveX = 0;
        float moveY = 0;
        int sizeRatio = mTextureViewManger.getCameraPictureSizeRatio();
        if (sizeRatio == 1) {
            // 16:9
            float deltaY = 2.0f / 4;
            float[][] moveData16To9 = new float[][] { { 2.0f, -2.0f }, { 2.0f, 0.0f - deltaY },
                    { 2.0f, 2.0f - deltaY * 2 }, { 0.0f, -2.0f }, { 0.0f, 0.0f - deltaY }, { 0.0f, 2.0f - deltaY * 2 },
                    { -2.0f, -2.0f }, { -2.0f, 0.0f - deltaY }, { -2.0f, 2.0f - deltaY * 2 } };
            moveX = moveData16To9[indexOfCell][0];
            moveY = moveData16To9[indexOfCell][1];
        } else {
            // 1:1 or 4:3
            float[][] moveData = new float[][] { { 2.0f, -2.0f }, { 2.0f, 0.0f }, { 2.0f, 2.0f }, { 0.0f, -2.0f },
                    { 0.0f, 0.0f }, { 0.0f, 2.0f }, { -2.0f, -2.0f }, { -2.0f, 0.0f }, { -2.0f, 2.0f } };
            moveX = moveData[indexOfCell][0];
            moveY = moveData[indexOfCell][1];
        }

        float scaleX = sSquareSize * 3.0f;
        float scaleY = sSquareSize * 3.0f;
        MatrixState.pushMatrix();
        AnimationState state = getAnimationState();
        switch (state) {
        case InFilter:
            zoom(moveX, moveY, 0, 0, scaleX, scaleY, 1, 1, progress);
            break;
        case OutFilter:
            zoom(0, 0, moveX, moveY, 1, 1, scaleX, scaleY, progress);
            break;
        case ZoomOutFilter:
            zoom(0, 0, moveX, moveY, 1, 1, scaleX, scaleY, progress);
            break;
        case ZoomInFilter:
            zoom(moveX, moveY, 0, 0, scaleX, scaleY, 1, 1, progress);
            break;
        }

        if (sizeRatio == 1) {
            // 16:9
            GLES20.glEnable(GL10.GL_SCISSOR_TEST);
            GLES20.glScissor(0, mHeight / 4, mWidth, mHeight);
            drawFilterAnimationEntity(mFilterAnimationShaderProgram, GLES20.GL_TEXTURE16);
            GLES20.glDisable(GL10.GL_SCISSOR_TEST);
        } else {
            // 1:1 or 4:3
        	drawFilterAnimationEntity(mFilterAnimationShaderProgram, GLES20.GL_TEXTURE16);
        }

        MatrixState.popMatrix();
        return true;
    }

    private void zoom(float moveFromX, float moveFromY, float moveToX, float moveToY, float scaleFromX,
            float scaleFromY, float scaleToX, float scaleToY, float progress) {
        float scaleX = scaleFromX * (1 - progress) + scaleToX * progress;
        float scaleY = scaleFromY * (1 - progress) + scaleToY * progress;

        float moveX = moveFromX * (1 - progress) + moveToX * progress;
        float moveY = moveFromY * (1 - progress) + moveToY * progress;
        MatrixState.translate(moveX, moveY, 0);
        MatrixState.scale(scaleX, scaleY, 1.0f);
    }

    public void setAnimationState(AnimationState transition) {
        mAnimationState = transition;
    }

    public AnimationState getAnimationState() {
        return mAnimationState;
    }
    
    private void sendMessageStartAnimation(){
        Message message = mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().obtainMessage();
        message.what = 0;
        mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().sendMessage(message);
    }
    
    private void sendMessageStopAnimation(){
    	Message message = mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().obtainMessage();
        message.what = 1;
        mTextureViewManger.getTextureViewAnimationManager().getMainThreadHandler().sendMessage(message);
    }

}
