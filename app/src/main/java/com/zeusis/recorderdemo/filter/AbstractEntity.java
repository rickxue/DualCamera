package com.zeusis.recorderdemo.filter;

public abstract class AbstractEntity {
	
    protected static final int COORDS_PER_VERTEX = 4;
    protected static final float sSquareSize = 1f;
	
	protected int mWidth;
	protected int mHeight;
	
    public abstract void initEntity();

    public abstract void drawEntity(float[] transformMatrix);

    public abstract void destoryEntity();
    
    public abstract void changeSize(int width,int height);
}
