package org.ekkoproject.android.player.tasks;

import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ManifestManager;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

public abstract class UpdateManifestAsyncTask extends AsyncTask<Long, Void, Manifest> {
    private final ManifestManager manager;

    protected UpdateManifestAsyncTask(final ManifestManager manager) {
        this.manager = manager;
    }

    @Override
    protected Manifest doInBackground(final Long... params) {
        if (params.length > 0) {
            return manager.getManifest(params[0]);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AsyncTask<Long, Void, Manifest> execute(final long courseId) {
        final Long[] params = new Long[] { courseId };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        } else {
            return this.execute(params);
        }
    }
}
