package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ekkoproject.android.player.model.Manifest;

public abstract class AbstractManifestPagerAdapter extends FragmentPagerAdapter implements ManifestAdapter {
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

    protected long getCourseId() {
        return this.manifest != null ? this.manifest.getCourseId() : INVALID_COURSE;
    }
}
