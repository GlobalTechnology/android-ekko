package org.ekkoproject.android.player.view;

import org.ekkoproject.android.player.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ScalableImageView extends ImageView {
    private float aspectRatio = 0.0f;

    public ScalableImageView(final Context context) {
        super(context);
    }

    public ScalableImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs);
    }

    public ScalableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.init(attrs);
    }

    private void init(final AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ScalableImageView);
        this.aspectRatio = a.getFloat(R.styleable.ScalableImageView_aspectRatio, 0.0f);
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

                setMeasuredDimension(width, height);
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

                setMeasuredDimension(width, height);
                return;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
