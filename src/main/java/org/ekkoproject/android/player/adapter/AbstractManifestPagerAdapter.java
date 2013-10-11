package org.ekkoproject.android.player.adapter;

import org.ekkoproject.android.player.model.Manifest;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public abstract class AbstractManifestPagerAdapter extends FragmentStatePagerAdapter implements ManifestAdapter {
    private Manifest manifest = null;

    public AbstractManifestPagerAdapter(final FragmentManager fm) {
        super(fm);
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
