package com.zeusis.recorderdemo.filter;

import android.graphics.Rect;

public interface IFilterEngine {
    public void initFilter();
    public void destroyFilter();
    public void renderFilter(int effectId, int texId, Rect texRect, int texW, int texH, int fboId, Rect fboRect, int fboW, int fboH, float[] matrix);

}
