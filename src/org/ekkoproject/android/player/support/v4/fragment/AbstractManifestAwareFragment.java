package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_COURSEID;

import java.util.ArrayList;
import java.util.List;

import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.EkkoBroadcastReceiver;
import org.ekkoproject.android.player.services.ManifestManager;
import org.ekkoproject.android.player.tasks.UpdateManifestAdaptersAsyncTask;
import org.ekkoproject.android.player.tasks.UpdateManifestAsyncTask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

public abstract class AbstractManifestAwareFragment extends Fragment implements
        EkkoBroadcastReceiver.ManifestUpdateListener {
    private EkkoBroadcastReceiver broadcastReceiver = null;
    private ManifestManager manifestManager = null;

    private long courseId = INVALID_COURSE;

    protected static final Bundle buildArgs(final long courseId) {
        final Bundle args = new Bundle();
        args.putLong(ARG_COURSEID, courseId);
        return args;
    }

    /** BEGIN lifecycle */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.manifestManager = ManifestManager.getInstance(getActivity());

        // process arguments
        this.courseId = getArguments().getLong(ARG_COURSEID, INVALID_COURSE);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.updateManifest();
        this.setupBroadcastReceiver();
    }

    @Override
    public final void onManifestUpdate(final long courseId) {
        this.updateManifest();
    }

    protected void onManifestUpdate(final Manifest manifest) {
    }

    @Override
    public void onStop() {
        super.onStop();
        this.cleanupBroadcastReceiver();
    }

    /** END lifecycle */

    protected final ManifestManager getManifestManager() {
        return this.manifestManager;
    }

    protected final long getCourseId() {
        return this.courseId;
    }

    private void setupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.cleanupBroadcastReceiver();
        }

        this.broadcastReceiver = new EkkoBroadcastReceiver(this, this.courseId).registerReceiver();
    }

    private void cleanupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.broadcastReceiver.unregisterReceiver();
            this.broadcastReceiver = null;
        }
    }

    protected final <T extends View> T findView(final Class<T> clazz, final int id) {
        final View root = getView();
        if (root != null) {
            final View view = root.findViewById(id);
            if (clazz.isInstance(view)) {
                return clazz.cast(view);
            }
        }
        return null;
    }

    protected final void updateManifestAdapters(final Manifest manifest, final View... views) {
        final List<ManifestAdapter> adapters = new ArrayList<ManifestAdapter>(views.length);
        for (final View view : views) {
            if (view instanceof ViewPager) {
                final PagerAdapter adapter = ((ViewPager) view).getAdapter();
                if (adapter instanceof ManifestAdapter) {
                    adapters.add((ManifestAdapter) adapter);
                }
            } else if (view instanceof AdapterView) {
                final Adapter adapter = ((AdapterView<?>) view).getAdapter();
                if (adapter instanceof ManifestAdapter) {
                    adapters.add((ManifestAdapter) adapter);
                }
            }
        }

        if (adapters.size() > 0) {
            // lookup the manifest in the background if we don't have it already
            if (manifest == null) {
                new UpdateManifestAdaptersAsyncTask(this.manifestManager, adapters.toArray(new ManifestAdapter[adapters
                        .size()])).execute(this.courseId);
            } else {
                for (final ManifestAdapter adapter : adapters) {
                    adapter.swapManifest(manifest);
                }
            }
        }
    }

    private void updateManifest() {
        new UpdateManifestAsyncTask(this.manifestManager) {
            @Override
            protected void onPostExecute(final Manifest result) {
                super.onPostExecute(result);
                onManifestUpdate(result);
            }
        }.execute(this.courseId);
    }
}
