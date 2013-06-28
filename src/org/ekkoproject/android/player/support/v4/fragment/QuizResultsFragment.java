package org.ekkoproject.android.player.support.v4.fragment;

import java.util.Set;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.ProgressManager;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QuizResultsFragment extends AbstractContentFragment {
    private TextView score = null;

    public static QuizResultsFragment newInstance(final long courseId, final String quizId) {
        final QuizResultsFragment fragment = new QuizResultsFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId, quizId);
        fragment.setArguments(args);

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
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        this.score = findView(TextView.class, R.id.score);
    }

    private void clearViews() {
        this.score = null;
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
    protected void updateProgressBar(final ProgressBar progressBar, final Manifest manifest, final Set<String> progress) {
        if (manifest != null) {
            final Quiz quiz = manifest.getQuiz(this.getContentId());
            if (quiz != null) {
                progressBar.setMax(quiz.getQuestions().size());
                progressBar.setProgress(quiz.getQuestions().size());
            } else {
                progressBar.setMax(0);
                progressBar.setProgress(0);
            }
        }
    }
}
