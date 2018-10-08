package com.jesussoto.android.giphy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.jesussoto.android.giphy.R;

/**
 * GifImageView that allows to define a custom aspect ratio like 4:3, 16:9 any other.
 */
public class AspectRatioImageView extends AppCompatImageView {

    private float mAspectRatio = 0f;

    public AspectRatioImageView(Context context) {
        this(context, null, 0);
    }

    public AspectRatioImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs !=  null) {
            TypedArray array = context.obtainStyledAttributes(attrs,
                    R.styleable.AspectRatioImageView, defStyleAttr, 0);

            mAspectRatio = array.getFloat(R.styleable.AspectRatioImageView_aspectRatio, 0f);

            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / mAspectRatio);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightSpec);
    }

    public void setAspectRatio(@FloatRange(from = 0f) float aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }
}
