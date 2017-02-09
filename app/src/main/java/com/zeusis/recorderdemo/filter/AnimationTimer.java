package com.zeusis.recorderdemo.filter;

import android.view.animation.Interpolator;

public class AnimationTimer {

    private static final long ANIMATION_START = -1;
    private static final long NO_ANIMATION = -2;

    private long mStartTime = NO_ANIMATION;
    private int mDuration;
    private Interpolator mInterpolator;
    private float mProgress;

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void start() {
        mStartTime = ANIMATION_START;
    }

    public void setStartTime(long time) {
        mStartTime = time;
    }

    public boolean isActive() {
        return mStartTime != NO_ANIMATION;
    }

    public void forceStop() {
        mStartTime = NO_ANIMATION;
    }

    public boolean calculate(long currentTimeMillis) {
        if (mStartTime == NO_ANIMATION) return false;
        if (mStartTime == ANIMATION_START) mStartTime = currentTimeMillis;
        int elapse = (int) (currentTimeMillis - mStartTime);
        float x = clamp((float) elapse / mDuration, 0f, 1f);
        Interpolator i = mInterpolator;
        mProgress = i != null ? i.getInterpolation(x) : x; 
//        onCalculate(mProgress);
        if (elapse >= mDuration) mStartTime = NO_ANIMATION;
        return mStartTime != NO_ANIMATION;
    }
    
    public float getProgress(){
        return mProgress;
    }

//    abstract protected void onCalculate(float progress);
    
    public static float clamp(float x, float min, float max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

}