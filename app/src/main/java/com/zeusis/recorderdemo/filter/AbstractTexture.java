package com.zeusis.recorderdemo.filter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;



public abstract class AbstractTexture extends AbstractEntity {
    
	private static final String TAG = "AbstractTexture";

    private static float sSquareCoords[] = { -sSquareSize, sSquareSize, 0f, -sSquareSize, -sSquareSize, 0f, sSquareSize,
            -sSquareSize, 0f, sSquareSize, sSquareSize, 0f, };

    private static float[] mTextureCoords = {
        0.0f, 1.0f, 0.0f, 1.0f, 
        0.0f, 0.0f, 0.0f, 1.0f, 
        1.0f, 0.0f, 0.0f, 1.0f, 
        1.0f, 1.0f, 0.0f, 1.0f ,
    };
    
    protected static short sDrawOrder[] = { 0, 1, 2, 2, 0, 3 };

    protected FloatBuffer mVertexBuffer;
    protected ShortBuffer mDrawListBuffer;
    protected FloatBuffer mTextureBuffer;
    protected TextureViewManager mTextureViewManger = null;
    
    public AbstractTexture(int width,int height){
        mWidth = width;
        mHeight = height;
    }
    
    @Override
    public void initEntity() {
        // TODO Auto-generated method stub
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(sDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(sDrawOrder);
        mDrawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(sSquareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(sSquareCoords);
        mVertexBuffer.position(0);

        ByteBuffer texturebb1 = ByteBuffer.allocateDirect(mTextureCoords.length * 4);
        texturebb1.order(ByteOrder.nativeOrder());
        mTextureBuffer = texturebb1.asFloatBuffer();
        mTextureBuffer.put(mTextureCoords);
        mTextureBuffer.position(0);
    }
    
	public void drawEntity(int program) {
		// TODO Auto-generated method stub
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, mVertexBuffer);

        int textureCoordinateHandle = GLES20.glGetAttribLocation(program, "vTexCoordinate");
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 4 * COORDS_PER_VERTEX, mTextureBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, sDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
		
	}
    
    @Override
    public void changeSize(int width, int height) {
        // TODO Auto-generated method stub
        mWidth = width;
        mHeight = height;
    }

    public abstract void doCellFiltersAnimation(float[] transformMatrix, int filterTextureFboId,int indexOfCell, boolean isZoomIn, float progress);
    public abstract void doCellFilters(float[] transformMatrix, int filterTextureFboId);
    public abstract void doEffectFilter(int effectId, float[] transformMatrix, int filterTextureFboId);
    public abstract int getTextureViewTextureId();
    public abstract int getCurrentFrameTextureId();
//    public abstract int getCurrentFilterTextureId();

}
