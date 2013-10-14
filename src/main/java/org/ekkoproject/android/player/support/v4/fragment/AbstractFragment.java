package org.ekkoproject.android.player.support.v4.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.services.ProgressManager;

import java.util.List;

public class AbstractFragment extends Fragment implements OnUpdateVisibilityListener {
    private ProgressManager progressManager = null;

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        this.progressManager = ProgressManager.getInstance(activity);
        super.onAttach(activity);
    }

    @Override
    public void onUpdateUserVisibleHint(final boolean isVisibleToUser) {
        // broadcast potential visibility change to all children fragments
        final FragmentManager fm;
        if(this.isAdded() && (fm = this.getChildFragmentManager()) != null) {
            final List<Fragment> frags = fm.getFragments();
            if(frags != null) {
                for (final Fragment child : frags) {
                    if (child instanceof OnUpdateVisibilityListener && this == child.getParentFragment()) {
                        ((OnUpdateVisibilityListener) child).onUpdateUserVisibleHint(child.getUserVisibleHint());
                    }
                }
            }
        }
    }

    /* END lifecycle */

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
