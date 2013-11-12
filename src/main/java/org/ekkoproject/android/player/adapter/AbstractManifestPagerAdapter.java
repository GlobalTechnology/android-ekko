package org.ekkoproject.android.player.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.ekkoproject.android.player.model.Manifest;

public abstract class AbstractManifestPagerAdapter extends FragmentStatePagerAdapter implements ManifestAdapter {
    protected final String mGuid;
    private Manifest manifest = null;

    public AbstractManifestPagerAdapter(final FragmentManager fm, final String guid) {
        super(fm);
        mGuid = guid;
    }

    public final synchronized Manifest swapManifest(final Manifest manifest) {
        final Manifest old = this.manifest;
        this.onNewManifest(manifest);
        notifyDataSetChanged();
        return old;
    }

    protected void onNewManifest(final Manifest manifest) {
        this.manifest = manifest;
    }

    protected Manifest getManifest() {
        return this.manifest;
    }
}
