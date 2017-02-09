package com.zeusis.recorderdemo.filter;

public interface  IAnimationController {
    public void startAnimation(int durationMsec);
    public void stopAnimation();
    public boolean isPlayingAnimation();
}
