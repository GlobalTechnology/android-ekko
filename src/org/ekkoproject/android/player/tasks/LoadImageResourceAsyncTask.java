package org.ekkoproject.android.player.tasks;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.services.ResourceManager;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

public final class LoadImageResourceAsyncTask extends AsyncTask<Void, Void, Bitmap> {
    private static final Executor LOAD_IMAGE_THREAD_POOL = new ThreadPoolExecutor(3, 7, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "LoadImageResourceAsyncTask #" + mCount.getAndIncrement());
                }
            });

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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AsyncTask<Void, Void, Bitmap> execute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return this.executeOnExecutor(LOAD_IMAGE_THREAD_POOL);
        } else {
            return this.execute(new Void[] {});
        }
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
