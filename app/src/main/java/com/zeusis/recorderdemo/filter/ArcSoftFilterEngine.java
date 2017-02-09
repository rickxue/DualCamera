package com.zeusis.recorderdemo.filter;


import com.arcsoft.filter.ArcFilterEngine;

import android.graphics.Rect;

public class ArcSoftFilterEngine implements IFilterEngine {
    
    private ArcFilterEngine mFilterEngine = null;

    @Override
    public void initFilter() {
        // TODO Auto-generated method stub
        if (mFilterEngine == null) {
            mFilterEngine = new ArcFilterEngine();// .getInstance();
            mFilterEngine.Init();
            mFilterEngine.prepareEngine();
        }
    }

    @Override
    public void destroyFilter() {
        // TODO Auto-generated method stub
        if(mFilterEngine!=null){
            mFilterEngine.UnInit();
            mFilterEngine = null;
        }
    }

    @Override
    public void renderFilter(int effectId, int texId, Rect texRect, int texW, int texH, int fboId, Rect fboRect,
            int fboW, int fboH, float[] matrix) {
        // TODO Auto-generated method stub
        if(mFilterEngine!=null){
            mFilterEngine.doStep(effectId, texId, true,matrix,texRect, texW, texH, fboId, fboRect, fboW, fboH,0);
        }
    }
    
    static {
        System.loadLibrary("ArcFilter");
    }

}
