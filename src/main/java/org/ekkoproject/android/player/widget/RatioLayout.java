package org.ekkoproject.android.player.widget;

import org.ekkoproject.android.player.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RatioLayout extends FrameLayout {
    private float aspectRatio = 0.0f;

    public RatioLayout(Context context) {
        super(context);
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs);
    }

    public RatioLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(attrs);
    }

    private void init(final AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RatioLayout);
        final float width = a.getFloat(R.styleable.RatioLayout_aspectRatioWidth, 0.0f);
        final float height = a.getFloat(R.styleable.RatioLayout_aspectRatioHeight, 0.0f);
        if (height > 0.0f && width > 0.0f) {
            this.aspectRatio = width / height;
        } else {
            this.aspectRatio = a.getFloat(R.styleable.RatioLayout_aspectRatio, 0.0f);
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (this.aspectRatio > 0.0f) {
            // determine if we should resize based on the aspect ratio
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            final int horizontalPadding = this.getPaddingLeft() + this.getPaddingRight();
            final int verticalPadding = this.getPaddingTop() + this.getPaddingBottom();

            // scale based off static width
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                final int width = MeasureSpec.getSize(widthMeasureSpec);

                int height = Math.max((int) ((width - horizontalPadding) / this.aspectRatio) + verticalPadding,
                        getSuggestedMinimumHeight());
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
                }

                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                return;
            }
            // scale based off static height
            else if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
                final int height = MeasureSpec.getSize(heightMeasureSpec);

                int width = Math.max((int) ((height - verticalPadding) * this.aspectRatio) + horizontalPadding,
                        getSuggestedMinimumWidth());
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec));
                }

                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
                return;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
