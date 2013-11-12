package org.ekkoproject.android.player.support.v4.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.ccci.gto.android.common.support.v4.fragment.AbstractFragment;

import java.util.List;

public class AbstractCascadingUserVisibleHintFragment extends AbstractFragment implements OnUpdateVisibilityListener {
    /* BEGIN lifecycle */

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

    @Override
    public boolean getUserVisibleHint() {
        if (super.getUserVisibleHint()) {
            final Fragment parent = this.getParentFragment();
            return parent == null || parent.getUserVisibleHint();
        }

        return false;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.onUpdateUserVisibleHint(this.getUserVisibleHint());
    }
}
