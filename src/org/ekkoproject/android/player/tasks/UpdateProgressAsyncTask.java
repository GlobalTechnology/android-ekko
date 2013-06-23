package org.ekkoproject.android.player.tasks;

import java.util.Set;

import org.ekkoproject.android.player.services.ProgressManager;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

public abstract class UpdateProgressAsyncTask extends AsyncTask<Long, Void, Set<String>> {
    private final ProgressManager manager;

    protected UpdateProgressAsyncTask(final ProgressManager manager) {
        this.manager = manager;
    }

    @Override
    protected final Set<String> doInBackground(final Long... params) {
        if (params.length > 0) {
            return this.manager.getProgress(params[0]);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AsyncTask<Long, Void, Set<String>> execute(final long courseId) {
        final Long[] params = new Long[] { courseId };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        } else {
            return this.execute(params);
        }
    }
}
