package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestQuizContentPagerAdapter;
import org.ekkoproject.android.player.model.Manifest;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class QuizFragment extends AbstractManifestAwareFragment implements AbstractContentFragment.Listener,
        QuestionFragment.Listener {
    private String quizId = null;

    private ViewPager questionsPager = null;

    public static QuizFragment newInstance(final long courseId, final String quizId) {
        final QuizFragment fragment = new QuizFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putString(ARG_CONTENTID, quizId);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.quizId = args.getString(ARG_CONTENTID, null);
        } else {
            this.quizId = args.getString(ARG_CONTENTID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupQuestionsAdapter();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.questionsPager);
    }

    @Override
    public void onNavigatePrevious() {
        if (this.questionsPager != null) {
            final int index = this.questionsPager.getCurrentItem() - 1;
            if (index >= 0) {
                this.questionsPager.setCurrentItem(index, false);
            } else {
                final Object listener = this.getPotentialListener();
                if (listener instanceof Listener) {
                    ((Listener) listener).onNavigatePrevious();
                }
            }
        }
    }

    @Override
    public void onNavigateNext() {
        if (this.questionsPager != null) {
            final PagerAdapter adapter = this.questionsPager.getAdapter();
            final int index = this.questionsPager.getCurrentItem() + 1;
            if (adapter == null || index < adapter.getCount()) {
                this.questionsPager.setCurrentItem(index, false);
            } else {
                final Object listener = this.getPotentialListener();
                if (listener instanceof Listener) {
                    ((Listener) listener).onNavigateNext();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /** END lifecycle */

    public String getQuizId() {
        return this.quizId;
    }

    private void findViews() {
        this.questionsPager = findView(ViewPager.class, R.id.questions);
    }

    private void clearViews() {
        this.questionsPager = null;
    }

    private void setupQuestionsAdapter() {
        if (this.questionsPager != null) {
            this.questionsPager
                    .setAdapter(new ManifestQuizContentPagerAdapter(getChildFragmentManager(), this.quizId));
        }
    }

    public interface Listener {
        void onNavigatePrevious();

        void onNavigateNext();
    }
}
