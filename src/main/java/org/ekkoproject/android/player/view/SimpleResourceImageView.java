package org.ekkoproject.android.player.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SimpleResourceImageView extends ImageView implements ResourceImageView {
    private final Helper mHelper;

    public SimpleResourceImageView(final Context context) {
        super(context);
        mHelper = new Helper(this);
    }

    public SimpleResourceImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mHelper = new Helper(this);
    }

    public SimpleResourceImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mHelper = new Helper(this);
    }

    @Override
    public void setResource(final long courseId, final String resourceId) {
        mHelper.setResource(courseId, resourceId);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHelper.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    @SuppressLint("WrongCall")
    protected void onDraw(final Canvas canvas) {
        mHelper.onDraw(canvas);
        super.onDraw(canvas);
    }
}
