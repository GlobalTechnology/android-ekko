package android.support.v4.app;

import java.util.Collections;
import java.util.List;

/** In ur package. stealing ur properties */
public final class FragmentUtils {
    public final static List<Fragment> getChildFragments(final Fragment frag) {
        if (frag != null && frag.mChildFragmentManager != null) {
            return getChildFragments(frag.mChildFragmentManager);
        }

        return Collections.emptyList();
    }

    public final static List<Fragment> getChildFragments(final FragmentManager fm) {
        if (fm instanceof FragmentManagerImpl) {
            final List<Fragment> frags = ((FragmentManagerImpl) fm).mAdded;
            if (frags != null && frags.size() > 0) {
                return Collections.unmodifiableList(frags);
            }
        }

        return Collections.emptyList();
    }
}
