package org.ekkoproject.android.player.support.v4.fragment;

import org.ekkoproject.android.player.services.ProgressManager;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentUtils;

import com.actionbarsherlock.app.SherlockFragment;

public class AbstractFragment extends SherlockFragment implements OnUpdateVisibilityListener {
    private ProgressManager progressManager = null;

    /** BEGIN lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        this.progressManager = ProgressManager.getInstance(activity);
        super.onAttach(activity);
    }

    @Override
    public void onUpdateUserVisibleHint(final boolean isVisibleToUser) {
        // broadcast potential visibility change to all children fragments
        for (final Fragment child : FragmentUtils.getChildFragments(this)) {
            if (child instanceof OnUpdateVisibilityListener) {
                ((OnUpdateVisibilityListener) child).onUpdateUserVisibleHint(child.getUserVisibleHint());
            }
        }
    }

    /** END lifecycle */

    protected ProgressManager getProgressManager() {
        return this.progressManager;
    }

    @Override
    public boolean getUserVisibleHint() {
        if (super.getUserVisibleHint()) {
            final Fragment parent = this.getParentFragment();
            if (parent != null) {
                return parent.getUserVisibleHint();
            }
            return true;
        }
        return false;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.onUpdateUserVisibleHint(this.getUserVisibleHint());
    }
}
