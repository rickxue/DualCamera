package com.zeusis.recorderdemo.filter;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;


public class GLRendererImpl implements IGLRenderer , SurfaceTexture.OnFrameAvailableListener{
    
    private static final String TAG = "GLRendererImpl";

    private Object mFrameLock = new Object();
    
    private float[] mPreviewTextureTransform = new float[16];
    
    private boolean mFrameAvailable = false;
    private boolean mAdjustViewport = false;
    private int mWidth, mHeight;
    
    private int mFrameNum = 0;
    private long mStartTime = 0L;
    
    private TextureViewManager mTextureViewManger = null;
    private SurfaceTexture mPreviewTexture;
    
    public boolean mWaitRenderData = false;
    
    public AbstractTexture mTextureViewTexture = null;
    public AbstractEntity mFilterTexture = null;
    public AbstractEntity mFilterAnimation = null;
    public AbstractEntity mTransitionAnimation = null;
    
//    public IOesTextureVisiter = null;
    
    public GLRendererImpl(TextureViewManager textureViewManger,int width,int height){
        mTextureViewManger = textureViewManger;
        mAdjustViewport = true;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void create() {
        // TODO Auto-generated method stub
    	mTextureViewTexture = new TextureViewTexture(mTextureViewManger,mWidth,mHeight);
    	mTextureViewTexture.initEntity();
        mPreviewTexture = new SurfaceTexture(mTextureViewTexture.getTextureViewTextureId());
        mPreviewTexture.setOnFrameAvailableListener(this);
        
        mFilterTexture = new FilterTexture(mTextureViewManger,mWidth,mHeight,mTextureViewTexture);
        mFilterTexture.initEntity();
        
        mFilterAnimation = new FilterTextureAnimation(mTextureViewManger,mWidth,mHeight,mTextureViewTexture);
        mFilterAnimation.initEntity();

        mTransitionAnimation = new TransitionAnimation(mTextureViewManger,mWidth,mHeight,mTextureViewTexture);
        mTransitionAnimation.initEntity();
        
        mTextureViewManger.getTextureViewAnimationManager().setFilterAnimationController((IAnimationController) mFilterAnimation);
        mTextureViewManger.getTextureViewAnimationManager().setTransitionAnimationController((IAnimationController) mTransitionAnimation);

    }
    

    @Override
    public boolean drawFrame() {
        // TODO Auto-generated method stub
        boolean result = false;
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        if (mAdjustViewport) {
            adjustViewport();
            mWaitRenderData = true;
        }

        synchronized (mFrameLock) {
            if (mFrameAvailable) {
                mPreviewTexture.updateTexImage();
                mPreviewTexture.getTransformMatrix(mPreviewTextureTransform);
                mFrameAvailable = false;
            }
        }

       result = drawEntity();
       
       return result;
    }


    @Override
    public void destory() {
        // TODO Auto-generated method stub      
        mTextureViewTexture.destoryEntity();
        mFilterTexture.destoryEntity();
        mFilterAnimation.destoryEntity();
        mTransitionAnimation.destoryEntity();

        if(mPreviewTexture != null) {
            mPreviewTexture.setOnFrameAvailableListener(null);
            mPreviewTexture.release();
            mPreviewTexture = null;
        }
    }
    
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mFrameNum == 0)
            mStartTime = System.currentTimeMillis();
        mFrameNum++;
        if (System.currentTimeMillis() - mStartTime >= 1000) {
            Log.i(TAG, "FrameCount: " + mFrameNum);
            mFrameNum = 0;
        }

        synchronized (mFrameLock) {
            mFrameAvailable = true;
        }
        
        mTextureViewManger.requestRender();
    }
    
    @Override
    public SurfaceTexture getPreviewTexture() {
        return mPreviewTexture;
    }

    @Override
    public void changeTextureSize(int width, int height) {
        // TODO Auto-generated method stub
        mWidth = width;
        mHeight = height;
        mAdjustViewport = true;
    }
    
    public void startFilterAnimation(){
        ((IAnimationController) mFilterAnimation).startAnimation(250);
    }
    
    public void startTransitionAnimation(int durationMsec){
        ((IAnimationController) mTransitionAnimation).startAnimation(durationMsec); 
    }
    
    public void stopTransitionAnimation(){
        ((IAnimationController) mTransitionAnimation).stopAnimation();
    }

    private void adjustViewport() {
        GLES20.glViewport(0, 0, mWidth, mHeight);

        mAdjustViewport = false;
        mFilterTexture.changeSize(mWidth, mHeight);
        mTextureViewTexture.changeSize(mWidth, mHeight);
        mFilterAnimation.changeSize(mWidth, mHeight);
        mTransitionAnimation.changeSize(mWidth, mHeight);
        if(((TransitionAnimation) mTransitionAnimation).isPlayingAnimation()){
            ((TransitionAnimation) mTransitionAnimation).stopAnimation();
        }
    }
    
    private boolean drawEntity(){
        if (((TransitionAnimation) mTransitionAnimation).isPlayingAnimation()) {
        	mTransitionAnimation.drawEntity(mPreviewTextureTransform);
            return true;
        }

		if (((IAnimationController) mFilterAnimation).isPlayingAnimation()) {
			mFilterAnimation.drawEntity(mPreviewTextureTransform);
			return true;
		}

        if (true/*mTextureViewManger.getFilterEffectFlag()&&mTextureViewManger.isInitiFilterEngine()*/) {
        	mFilterTexture.drawEntity(mPreviewTextureTransform);
            return true;
        } else {
            
            if (mWaitRenderData) {
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
                mWaitRenderData = false;
                return true;
            }

            mTextureViewTexture.drawEntity(mPreviewTextureTransform);
            return true;
        }
    }
}