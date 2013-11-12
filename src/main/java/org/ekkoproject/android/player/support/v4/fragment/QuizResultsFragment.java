package org.ekkoproject.android.player.support.v4.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.ProgressManager;

import java.util.Set;

public class QuizResultsFragment extends AbstractContentFragment implements View.OnClickListener {
    private TextView score = null;

    private View nextButton = null;
    private View restartButton = null;

    public static QuizResultsFragment newInstance(final String guid, final long courseId, final String quizId) {
        final QuizResultsFragment fragment = new QuizResultsFragment();
        fragment.setArguments(buildArgs(guid, courseId, quizId));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_results, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupButtons();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateScore(manifest, this.getProgress());
    }

    @Override
    protected void onProgressUpdate(final Set<String> progress) {
        super.onProgressUpdate(progress);
        this.updateScore(this.getManifest(), progress);
    }

    @Override
    public void onClick(final View v) {
        final Object listener = this.getPotentialListener();
        if (listener instanceof OnNavigateListener) {
            switch (v.getId()) {
            case R.id.finish:
                ((OnNavigateListener) listener).onNavigateNext();
                break;
            case R.id.show_answers:
                final Fragment parent = this.getParentFragment();
                if (parent instanceof QuizFragment) {
                    ((QuizFragment) parent).showAnswers();
                }
                ((OnNavigateListener) listener).onNavigateFirst();
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        this.score = findView(TextView.class, R.id.score);
        this.nextButton = findView(View.class, R.id.finish);
        this.restartButton = findView(View.class, R.id.show_answers);
    }

    private void clearViews() {
        this.score = null;
        this.nextButton = null;
        this.restartButton = null;
    }

    private void setupButtons() {
        if (this.nextButton != null) {
            this.nextButton.setOnClickListener(this);
        }
        if (this.restartButton != null) {
            this.restartButton.setOnClickListener(this);
        }
    }

    private void updateScore(final Manifest manifest, final Set<String> progress) {
        if (this.score != null) {
            final Pair<Integer, Integer> score;
            if (manifest != null && progress != null) {
                final Quiz quiz = manifest.getQuiz(this.getContentId());
                score = ProgressManager.getQuizProgress(this.getCourseId(), quiz, progress);
            } else {
                score = Pair.create(0, 0);
            }

            this.score.setText(score.first + "/" + score.second);
        }
    }

    @Override
    protected Pair<Integer, Integer> getProgress(final Manifest manifest, final Set<String> progress) {
        if (manifest != null) {
            final Quiz quiz = manifest.getQuiz(this.getContentId());
            if (quiz != null) {
                return Pair.create(1, 1);
            }
        }

        return null;
    }
}
