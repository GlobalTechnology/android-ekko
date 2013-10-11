package org.ekkoproject.android.player.support.v4.fragment;

import java.util.Set;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestQuizContentPagerAdapter;
import org.ekkoproject.android.player.model.Manifest;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class QuizFragment extends AbstractContentFragment implements AbstractContentFragment.OnNavigateListener {
    private ViewPager contentPager = null;

    private boolean showAnswers = false;

    public static QuizFragment newInstance(final long courseId, final String quizId) {
        final QuizFragment fragment = new QuizFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId, quizId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupContentPager();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.contentPager);
    }

    @Override
    public void onNavigateFirst() {
        if (this.contentPager != null) {
            this.contentPager.setCurrentItem(0, false);
        }
    }

    @Override
    public void onNavigatePrevious() {
        // try to handle it locally
        if (this.contentPager != null) {
            final int index = this.contentPager.getCurrentItem() - 1;
            if (index >= 0) {
                this.contentPager.setCurrentItem(index, false);
                return;
            }
        }

        // propagate request to parent
        final Object listener = this.getPotentialListener();
        if (listener instanceof OnNavigateListener) {
            ((OnNavigateListener) listener).onNavigatePrevious();
        }
    }

    @Override
    public void onNavigateNext() {
        // try to handle this locally
        if (this.contentPager != null) {
            final PagerAdapter adapter = this.contentPager.getAdapter();
            final int index = this.contentPager.getCurrentItem() + 1;
            if (adapter == null || index < adapter.getCount()) {
                this.contentPager.setCurrentItem(index, false);
                return;
            }
        }

        // propagate request to parent
        final Object listener = this.getPotentialListener();
        if (listener instanceof OnNavigateListener) {
            ((OnNavigateListener) listener).onNavigateNext();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        this.contentPager = findView(ViewPager.class, R.id.questions);
    }

    private void clearViews() {
        this.contentPager = null;
    }

    private void setupContentPager() {
        if (this.contentPager != null) {
            final ManifestQuizContentPagerAdapter adapter = new ManifestQuizContentPagerAdapter(
                    getChildFragmentManager(), this.getContentId());
            adapter.setShowAnswers(this.showAnswers);
            this.contentPager.setAdapter(adapter);
        }
    }

    public void showAnswers() {
        this.showAnswers = true;
        if (this.contentPager != null) {
            final PagerAdapter adapter = this.contentPager.getAdapter();
            if (adapter instanceof ManifestQuizContentPagerAdapter) {
                ((ManifestQuizContentPagerAdapter) adapter).setShowAnswers(this.showAnswers);
            }
        }
    }

    @Override
    protected Pair<Integer, Integer> getProgress(final Manifest manifest, final Set<String> progress) {
        // we don't have progress for a QuizFragment
        return null;
    }
}
