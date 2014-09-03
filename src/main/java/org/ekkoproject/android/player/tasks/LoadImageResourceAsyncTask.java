package org.ekkoproject.android.player.tasks;

import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MIN_BITMAP_HEIGHT;
import static org.ekkoproject.android.player.services.ResourceManager.DEFAULT_MIN_BITMAP_WIDTH;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.services.ResourceManager;

import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoadImageResourceAsyncTask extends AsyncTask<Void, Void, Bitmap> {
    private static final ThreadPoolExecutor LOAD_IMAGE_THREAD_POOL;

    static {
        final ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, "LoadImageResourceAsyncTask #" + mCount.getAndIncrement());
            }
        };
        LOAD_IMAGE_THREAD_POOL =
                new ThreadPoolExecutor(7, 7, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), tf);
        LOAD_IMAGE_THREAD_POOL.allowCoreThreadTimeOut(true);
    }

    private final ResourceManager mResources;
    private final WeakReference<ImageView> mView;
    private final long mCourseId;
    private final String mResourceId;
    private final ResourceManager.BitmapOptions mOpts;

    public LoadImageResourceAsyncTask(final ImageView view, final long courseId, final String resourceId) {
        this(view, courseId, resourceId, view.getWidth(), view.getHeight());
    }

    public LoadImageResourceAsyncTask(final ImageView view, final long courseId, final String resourceId, final int w,
                                      final int h) {
        this(view, courseId, resourceId, new ResourceManager.BitmapOptions(w > 0 ? w : DEFAULT_MIN_BITMAP_WIDTH,
                                                                           h > 0 ? h : DEFAULT_MIN_BITMAP_HEIGHT));
    }

    public LoadImageResourceAsyncTask(final ImageView view, final long courseId, final String resourceId,
                                      final ResourceManager.BitmapOptions opts) {
        mResources = ResourceManager.getInstance(view.getContext());
        mView = new WeakReference<>(view);
        mCourseId = courseId;
        mResourceId = resourceId;
        mOpts = opts;

        view.setTag(R.id.image_loader_task, new WeakReference<AsyncTask<?, ?, ?>>(this));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AsyncTask<Void, Void, Bitmap> execute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return super.executeOnExecutor(LOAD_IMAGE_THREAD_POOL);
        } else {
            return super.execute();
        }
    }

    @Override
    protected Bitmap doInBackground(final Void... params) {
        if (checkImageView()) {
            return mResources.getBitmap(mCourseId, mResourceId, mOpts);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (checkImageView()) {
            if (bitmap != null) {
                final ImageView view = mView.get();
                if (view != null) {
                    view.setImageBitmap(bitmap);
                    view.setTag(R.id.image_loader_task, null);
                }
            }
        }
    }

    private boolean checkImageView() {
        final ImageView view = mView.get();
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
