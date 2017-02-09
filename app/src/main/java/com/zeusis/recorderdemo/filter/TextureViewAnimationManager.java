package com.zeusis.recorderdemo.filter;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public class TextureViewAnimationManager {
	
	private Handler mMainThreadHandler;
	
	private TextureViewManager mTextureViewManager = null;
	private Activity mCameraActivity = null;
	
    public static final int REQUEST_START_TRANSITION_ANIMATION = 0;
    public static final int REQUEST_STOP_TRANSITION_ANIMATION = 0;
    public static final int REQUEST_START_FILTER_ANIMATION = 0;
    public static final int REQUEST_STOP_FILTER_ANIMATION = 0;
    
    private IAnimationController mFilterAnimationController = null;
    private IAnimationController mTransitionAnimationController = null;
    
    public int mAnimationType = -1;
    public boolean mNeedResize = false;
	
	public TextureViewAnimationManager(Activity activity, TextureViewManager textureViewManager) {
		// TODO Auto-generated constructor stub
		mTextureViewManager = textureViewManager;
		mCameraActivity = activity;
		createMainThreadHandler();
	}
	
    public  Handler getMainThreadHandler(){
        return mMainThreadHandler;
    }
	
	private void createMainThreadHandler() {
        mMainThreadHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case 1://finish filter animation
                	mTextureViewManager.setFilterNameVisible(mTextureViewManager.getFilterEffectFlag() && mTextureViewManager.isRenderCellFilter());
                    break;
                case 0://start filter animation
                	mTextureViewManager.setFilterNameVisible(false);
                    break;
                case 2://resizeTextureView
                	resizeTextureView();
                    break;
                }
            }
        };
    }

	public void setFilterAnimationController(IAnimationController animationController){
		mFilterAnimationController = animationController;
	}
	
	public void setTransitionAnimationController(IAnimationController animationController){
		mTransitionAnimationController = animationController;
	}

	public void startTransitionAnimation(int animationType, boolean resize, int durationMsec){
		mTransitionAnimationController.startAnimation(durationMsec);
		mAnimationType = animationType;
		mNeedResize = resize;
	}
	
	public void stopTransitionAnimation(){
		mTransitionAnimationController.stopAnimation();
	}
	
	public void stratFilterAnimation(int durationMsec){
		mFilterAnimationController.startAnimation(durationMsec);
	}
	
	public void stopFilterAnimation(){
		mFilterAnimationController.stopAnimation();
	}
	
	public void resizeTextureView(){
		switch(mAnimationType){
        case 1:
            //mCameraActivity.resizeTextrueView(1);
            break;
        case 2:
            //mCameraActivity.resizeTextrueView(0);
            break;
        }
        
        if (!mNeedResize) {
            stopTransitionAnimation();
        }
	}

}
