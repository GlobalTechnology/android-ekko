package org.ekkoproject.android.player.view;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MAX_BITMAP_HEIGHT;
import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MAX_BITMAP_WIDTH;
import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MIN_BITMAP_HEIGHT;
import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MIN_BITMAP_WIDTH;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.os.Build;
import android.widget.ImageView;

import org.ccci.gto.android.common.model.Dimension;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;

public interface ResourceImageView {
    public static final class Helper {
        private final ImageView mView;
        private final ResourceManager mResources;

        private long mCourseId = INVALID_COURSE;
        private String mResourceId = null;
        private Dimension mSize = new Dimension(DEFAULT_MIN_BITMAP_WIDTH, DEFAULT_MIN_BITMAP_HEIGHT);
        private Dimension mMaxSize = new Dimension(DEFAULT_MAX_BITMAP_WIDTH, DEFAULT_MAX_BITMAP_HEIGHT);

        public Helper(final ImageView view) {
            mView = view;
            mResources = ResourceManager.getInstance(mView.getContext());
        }

        public void setResource(final long courseId, final String resourceId) {
            if (mCourseId != courseId || (mResourceId != null && !mResourceId.equals(resourceId)) ||
                    (mResourceId == null && resourceId != null)) {
                mCourseId = courseId;
                mResourceId = resourceId;

                mView.setImageDrawable(null);
                triggerUpdate();
            }
        }

        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            if (oldw != w || oldh != h) {
                mSize = new Dimension(w, h);
                this.triggerUpdate();
            }
        }

        @SuppressLint("DrawAllocation")
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public void onDraw(final Canvas canvas) {
            // update the maxSize (only on ICS where we can get the actual max size)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                final int maxWidth = canvas.getMaximumBitmapWidth();
                final int maxHeight = canvas.getMaximumBitmapHeight();
                if (mMaxSize.width != maxWidth || mMaxSize.height != maxHeight) {
                    mMaxSize = new Dimension(maxWidth, maxHeight);
                    this.triggerUpdate();
                }
            }
        }

        private void triggerUpdate() {
            if (mCourseId != INVALID_COURSE && mResourceId != null) {
                final ResourceManager.BitmapOptions opts = new ResourceManager.BitmapOptions(mSize, mMaxSize);
                new LoadImageResourceAsyncTask(mResources, mView, mCourseId, mResourceId, opts).execute();
            } else {
                // clear the image and any pending image_loader_task
                mView.setTag(R.id.image_loader_task, null);
                mView.setImageDrawable(null);
            }
        }
    }

    void setResource(long courseId, String resourceId);
}
