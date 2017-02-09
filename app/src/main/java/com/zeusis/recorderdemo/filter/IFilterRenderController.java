package com.zeusis.recorderdemo.filter;


import android.graphics.SurfaceTexture;

public interface IFilterRenderController {
	
    public void createTexture(SurfaceTexture surfaceTexture, int width, int height);
    public void changeTextureSize(int width, int height);
    public void destoryTexture();

}
