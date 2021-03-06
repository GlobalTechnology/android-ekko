package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_COURSEID;
import static org.ekkoproject.android.player.fragment.Constants.ARG_GUID;
import static org.ekkoproject.android.player.services.ManifestManager.FLAG_NON_BLOCKING;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.EkkoBroadcastReceiver;
import org.ekkoproject.android.player.services.ManifestManager;
import org.ekkoproject.android.player.services.ProgressManager;
import org.ekkoproject.android.player.tasks.UpdateManifestAdaptersAsyncTask;
import org.ekkoproject.android.player.tasks.UpdateManifestAsyncTask;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractManifestAwareFragment extends AbstractCascadingUserVisibleHintFragment implements EkkoBroadcastReceiver.ManifestUpdateListener {
    private EkkoBroadcastReceiver broadcastReceiver = null;
    private ManifestManager manifestManager = null;
    private ProgressManager progressManager = null;

    private String mGuid = GUID_GUEST;
    private long courseId = INVALID_COURSE;

    private Manifest manifest = null;

    protected static Bundle buildArgs(final String guid, final long courseId) {
        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid != null ? guid : GUID_GUEST);
        args.putLong(ARG_COURSEID, courseId);
        return args;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        final Bundle args = getArguments();
        this.courseId = args.getLong(ARG_COURSEID, INVALID_COURSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mGuid = args.getString(ARG_GUID, GUID_GUEST);
        } else {
            mGuid = args.getString(ARG_GUID);
            if (mGuid == null) {
                mGuid = GUID_GUEST;
            }
        }

        this.manifestManager = ManifestManager.getInstance(getActivity());
        this.progressManager = ProgressManager.getInstance(getActivity(), mGuid);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setupBroadcastReceiver();
        this.updateManifest();
    }

    @Override
    public final void onManifestUpdate(final long courseId) {
        this.updateManifest();
    }

    protected void onManifestUpdate(final Manifest manifest) {
        this.manifest = manifest;
    }

    @Override
    public void onStop() {
        super.onStop();
        this.cleanupBroadcastReceiver();
    }

    /** END lifecycle */

    protected final Manifest getManifest() {
        return this.manifest;
    }

    protected ProgressManager getProgressManager() {
        return this.progressManager;
    }

    protected final String getGuid() {
        return mGuid;
    }

    protected final long getCourseId() {
        return this.courseId;
    }

    protected final Object getPotentialListener() {
        // find the parent object (can be a fragment or activity)
        Object parent = getParentFragment();
        if (parent == null) {
            parent = getActivity();
        }
        return parent;
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

    protected final void updateManifestAdapters(Manifest manifest, final View... views) {
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
            // try fetching the manifest with a non-blocking request if we don't
            // have it
            if (manifest == null) {
                manifest = this.manifestManager.getManifest(this.courseId, FLAG_NON_BLOCKING);
            }

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
        // try retrieving the manifest using a non-blocking request
        final Manifest manifest = this.manifestManager.getManifest(this.courseId, FLAG_NON_BLOCKING);

        if (manifest != null) {
            this.onManifestUpdate(manifest);
        } else {
            new UpdateManifestAsyncTask(this.manifestManager) {
                @Override
                protected void onPostExecute(final Manifest result) {
                    super.onPostExecute(result);
                    AbstractManifestAwareFragment.this.onManifestUpdate(result);
                }
            }.execute(this.courseId);
        }
    }
}
