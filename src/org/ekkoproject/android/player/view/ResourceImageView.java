package org.ekkoproject.android.player.view;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ResourceImageView extends ImageView {
    private final ResourceManager manager;

    private int width = 0;
    private int height = 0;
    private long courseId = INVALID_COURSE;
    private String resourceId = null;

    public ResourceImageView(final Context context) {
        super(context);
        this.manager = ResourceManager.getInstance(context);
    }

    public ResourceImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.manager = ResourceManager.getInstance(context);
    }

    public ResourceImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.manager = ResourceManager.getInstance(context);
    }

    public void setResource(final long courseId, final String resourceId) {
        final boolean needsUpdate = this.courseId != courseId
                || (this.resourceId != null && !this.resourceId.equals(resourceId))
                || (this.resourceId == null && resourceId != null);

        this.courseId = courseId;
        this.resourceId = resourceId;

        if (needsUpdate) {
            this.setImageDrawable(null);
            this.triggerUpdate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;

        if (oldw != w || oldh != h) {
            this.triggerUpdate();
        }
    }

    private void triggerUpdate() {
        if (this.courseId != INVALID_COURSE && this.resourceId != null && this.width > 0 && this.height > 0) {
            new LoadImageResourceAsyncTask(this.manager, this, this.courseId, this.resourceId, this.width, this.height)
                    .execute();
        } else {
            this.setImageDrawable(null);
        }
    }
}
