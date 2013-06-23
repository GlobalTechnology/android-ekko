package org.ekkoproject.android.player.support.v4.fragment;

import org.ekkoproject.android.player.services.ProgressManager;

import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragment;

public class AbstractFragment extends SherlockFragment {
    private ProgressManager progressManager = null;

    /** BEGIN lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        this.progressManager = ProgressManager.getInstance(activity);
        super.onAttach(activity);
    }

    /** END lifecycle */

    protected ProgressManager getProgressManager() {
        return this.progressManager;
    }
}
