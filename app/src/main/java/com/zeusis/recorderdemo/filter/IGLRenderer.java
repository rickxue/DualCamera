package com.zeusis.recorderdemo.filter;

import android.graphics.SurfaceTexture;

public interface IGLRenderer {  
    public void create();
    public boolean drawFrame();
    public void destory();
    public SurfaceTexture getPreviewTexture();
    public void changeTextureSize(int width, int height);
}  
