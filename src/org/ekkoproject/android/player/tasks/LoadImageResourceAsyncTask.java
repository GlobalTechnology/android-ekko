package org.ekkoproject.android.player.tasks;

import java.lang.ref.WeakReference;

import org.appdev.R;
import org.ekkoproject.android.player.services.ResourceManager;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class LoadImageResourceAsyncTask extends AsyncTask<Void, Void, Bitmap> {
    private final ResourceManager manager;
    private final WeakReference<ImageView> view;
    private final long courseId;
    private final String resourceId;
    private final int width;
    private final int height;

    public LoadImageResourceAsyncTask(final ResourceManager manager, final ImageView view, final long courseId,
            final String resourceId) {
        this(manager, view, courseId, resourceId, view.getWidth(), view.getHeight());
    }

    public LoadImageResourceAsyncTask(final ResourceManager manager, final ImageView view, final long courseId,
            final String resourceId, final int width, final int height) {
        this.manager = manager;
        this.courseId = courseId;
        this.resourceId = resourceId;
        this.width = width > 0 ? width : 50;
        this.height = height > 0 ? height : 50;
        view.setTag(R.id.image_loader_task, new WeakReference<AsyncTask<?, ?, ?>>(this));
        this.view = new WeakReference<ImageView>(view);
    }

    @Override
    protected Bitmap doInBackground(final Void... params) {
        if (checkImageView()) {
            final Bitmap bitmap = manager.getBitmap(this.courseId, this.resourceId, this.width, this.height);
            return bitmap;
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (bitmap != null && checkImageView()) {
            final ImageView view = this.view.get();
            if (view != null) {
                view.setImageBitmap(bitmap);
                view.setTag(R.id.image_loader_task, null);
            }
        }
    }

    private boolean checkImageView() {
        final ImageView view = this.view.get();
        if (view != null) {
            final Object ref = view.getTag(R.id.image_loader_task);
            if (ref instanceof WeakReference) {
                final Object task = ((WeakReference<?>) ref).get();
                return this == task;
            }
        }
        return false;
    }
}
