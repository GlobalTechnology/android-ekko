package org.ekkoproject.android.player.tasks;

import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ManifestManager;

public class UpdateManifestAdapterAsyncTask extends UpdateManifestAsyncTask {
    private final ManifestAdapter<?> adapter;

    public UpdateManifestAdapterAsyncTask(final ManifestManager manager, final ManifestAdapter<?> adapter) {
        super(manager);
        this.adapter = adapter;
    }

    @Override
    protected void onPostExecute(final Manifest result) {
        super.onPostExecute(result);
        this.adapter.swapManifest(result);
    }
}
