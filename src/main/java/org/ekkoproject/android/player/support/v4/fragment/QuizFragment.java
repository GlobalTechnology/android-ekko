package org.ekkoproject.android.player.support.v4.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.util.BroadcastUtils;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestQuizContentPagerAdapter;
import org.ekkoproject.android.player.model.Manifest;

import java.util.Set;

public class QuizFragment extends AbstractContentFragment implements AbstractContentFragment.OnNavigateListener {
    private ViewPager contentPager = null;

    private boolean mShowAnswers = false;

    public static QuizFragment newInstance(final String guid, final long courseId, final String quizId) {
        final QuizFragment fragment = new QuizFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId, quizId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
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
            final ManifestQuizContentPagerAdapter adapter =
                    new ManifestQuizContentPagerAdapter(getChildFragmentManager(), getGuid(), getContentId());
            this.contentPager.setAdapter(adapter);
        }
    }

    public void setShowAnswers(final boolean showAnswers) {
        if (mShowAnswers != showAnswers) {
            mShowAnswers = showAnswers;

            // broadcast the new mShowAnswers state to all existing fragments
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                    Broadcasts.showAnswersIntent(this.getCourseId(), this.getContentId(), this.mShowAnswers));
        }
    }

    public boolean getShowAnswers() {
        return mShowAnswers;
    }

    @Override
    protected Pair<Integer, Integer> getProgress(final Manifest manifest, final Set<String> progress) {
        // we don't have progress for a QuizFragment
        return null;
    }

    public static final class Broadcasts {
        private static final String ACTION_SHOW_ANSWERS = QuizFragment.class.getName() + ".ACTION_SHOW_ANSWERS";
        private static final String EXTRA_SHOW_ANSWERS = QuizFragment.class.getName() + ".EXTRA_SHOW_ANSWERS";

        private static final Uri BASE_COURSE = Uri.parse("ekko://course/");

        private static Uri quizUri(final long courseId, final String quizId) {
            return BASE_COURSE.buildUpon().appendPath(Long.toString(courseId)).appendPath("quiz").appendPath(quizId)
                    .build();
        }

        /* Intents */
        private static Intent showAnswersIntent(final long courseId, final String quizId, final boolean showAnswers) {
            return new Intent(ACTION_SHOW_ANSWERS, quizUri(courseId, quizId)).putExtra(EXTRA_SHOW_ANSWERS, showAnswers);
        }

        /* Intent Filters */
        public static IntentFilter showAnswersFilter(final long courseId, final String quizId) {
            final IntentFilter filter = new IntentFilter(ACTION_SHOW_ANSWERS);
            BroadcastUtils.addDataUri(filter, quizUri(courseId, quizId));
            return filter;
        }
    }

    public abstract static class QuizBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Broadcasts.ACTION_SHOW_ANSWERS.equals(action)) {
                this.onShowAnswers(intent.getBooleanExtra(Broadcasts.EXTRA_SHOW_ANSWERS, false));
            }
        }

        protected abstract void onShowAnswers(boolean showAnswers);
    }
}
