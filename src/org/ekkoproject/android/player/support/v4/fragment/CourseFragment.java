package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.util.ViewUtils.fragmentAnimationHack;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestContentPagerAdapter;
import org.ekkoproject.android.player.model.Manifest;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CourseFragment extends AbstractManifestAwareFragment {
    private static final String ARG_ANIMATIONHACK = CourseFragment.class.getName() + ".ARG_ANIMATIONHACK";

    private ViewPager contentPager = null;

    private boolean animationHack = false;

    public static CourseFragment newInstance(final long courseId) {
        return newInstance(courseId, false);
    }

    public static CourseFragment newInstance(final long courseId, final boolean animationHack) {
        final CourseFragment fragment = new CourseFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putBoolean(ARG_ANIMATIONHACK, animationHack);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // load arguments
        this.animationHack = getArguments().getBoolean(ARG_ANIMATIONHACK, this.animationHack);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupContentPager();
    }

    @Override
    protected void onManifestUpdate(Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.contentPager);
    }

    @Override
    public void onPause() {
        if (this.animationHack) {
            fragmentAnimationHack(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        this.clearViews();
        super.onDestroyView();
    }

    /** END lifecycle */

    private void findViews() {
        this.contentPager = findView(ViewPager.class, R.id.content);
    }

    private void clearViews() {
        this.contentPager = null;
    }

    private void setupContentPager() {
        if (this.contentPager != null) {
            this.contentPager.setAdapter(new ManifestContentPagerAdapter(getChildFragmentManager()));
        }
    }
}
