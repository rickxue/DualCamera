package com.zeusis.recorderdemo.filter;

public class TextureDecorator extends AbstractEntity {
	
	protected AbstractTexture mEntity = null;
	
	protected TextureViewManager mTextureViewManger = null;
	
	public TextureDecorator(AbstractTexture entity,int width,int height){
		
		mEntity = entity;
		mWidth = width;
		mHeight = height;
	}

	@Override
	public void initEntity() {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawEntity(float[] transformMatrix) {
		// TODO Auto-generated method stub
//		mEntity.drawEntity(transformMatrix);
	}

	@Override
	public void destoryEntity() {
		// TODO Auto-generated method stub
//		mEntity.destoryEntity();
	}

	@Override
	public void changeSize(int width, int height) {
		// TODO Auto-generated method stub
	    mWidth = width;
        mHeight = height;
//		mEntity.changeSize(width, height);
	}

}
