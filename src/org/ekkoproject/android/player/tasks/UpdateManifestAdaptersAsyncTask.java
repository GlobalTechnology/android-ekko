package org.ekkoproject.android.player.tasks;

import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ManifestManager;

public class UpdateManifestAdaptersAsyncTask extends UpdateManifestAsyncTask {
    private final ManifestAdapter[] adapters;

    public UpdateManifestAdaptersAsyncTask(final ManifestManager manager, final ManifestAdapter... adapters) {
        super(manager);
        this.adapters = adapters;
    }

    @Override
    protected void onPostExecute(final Manifest result) {
        super.onPostExecute(result);
        for (final ManifestAdapter adapter : this.adapters) {
            adapter.swapManifest(result);
        }
    }
}
