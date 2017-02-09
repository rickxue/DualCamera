package com.zeusis.recorderdemo.filter;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zeusis.recorderdemo.R;

public class FilterNameTextView extends TextView {
    private static final int DEFAULT_ORIENTATION = 90;
    private int mOrientation =  DEFAULT_ORIENTATION;
    
    private int mTextPaddingBottom = 0;

    public FilterNameTextView(Context context) {
        super(context, null);
        
        mTextPaddingBottom = getResources().getDimensionPixelSize(R.dimen.filter_text_padding_bottom);
    }

    public FilterNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.textViewStyle);
        mTextPaddingBottom = getResources().getDimensionPixelSize(R.dimen.filter_text_padding_bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        Paint paint = new Paint();
        float textSize = getTextSize();
        paint.setTextSize(textSize);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int textWidth = (int) paint.measureText(this.getText().toString());

        this.setWidth(width);
        this.setHeight(height);

        switch (mOrientation) {
        case 90:
            canvas.rotate(mOrientation, this.getWidth() / 2f, this.getHeight() / 2f);
            canvas.translate((width - textWidth) / 2, height - (height - width) / 2 - textSize - mTextPaddingBottom);
            break;
        case -90:
            canvas.rotate(mOrientation, this.getWidth() / 2f, this.getHeight() / 2f);
            canvas.translate((width - textWidth) / 2, height - (height - width) / 2 - textSize - mTextPaddingBottom);
            break;
        case 0:
            canvas.rotate(mOrientation, this.getWidth() / 2f, this.getHeight() / 2f);
            canvas.translate((width - textWidth) / 2, height - textSize - mTextPaddingBottom);
            break;
        case 180:
            canvas.rotate(mOrientation, this.getWidth() / 2f, this.getHeight() / 2f);
            canvas.translate((width - textWidth) / 2, height - textSize - mTextPaddingBottom);
            break;
        default:
            break;
        }

        super.onDraw(canvas);
        canvas.restore();
    }

    public void updatemOrientation(int orientation) {
        mOrientation = orientation;
        this.invalidate();
    }
}
