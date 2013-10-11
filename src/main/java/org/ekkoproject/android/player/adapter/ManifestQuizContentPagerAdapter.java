package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.util.Collections;
import java.util.List;

import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Question;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.support.v4.fragment.QuestionFragment;
import org.ekkoproject.android.player.support.v4.fragment.QuizResultsFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ManifestQuizContentPagerAdapter extends AbstractManifestQuizPagerAdapter {
    private static final List<Question> NO_QUESTIONS = Collections.emptyList();

    private List<Question> questions = NO_QUESTIONS;

    private boolean showAnswers = false;

    public ManifestQuizContentPagerAdapter(final FragmentManager fm, final String quizId) {
        super(fm, quizId);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onNewQuiz(final Quiz quiz) {
        super.onNewQuiz(quiz);
        if (quiz != null) {
            this.questions = quiz.getQuestions();
        } else {
            this.questions = NO_QUESTIONS;
        }
    }

    /* END lifecycle */

    public void setShowAnswers(final boolean showAnswers) {
        final boolean stateChanged = showAnswers != this.showAnswers;
        this.showAnswers = showAnswers;
        if (stateChanged) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return this.questions.size() + 1;
    }

    @Override
    public Fragment getItem(final int position) {
        final Manifest manifest = this.getManifest();
        final long courseId = manifest != null ? manifest.getCourseId() : INVALID_COURSE;
        final Quiz quiz = this.getQuiz();
        final String quizId = quiz != null ? quiz.getId() : null;

        // last page (results)
        if (this.questions.size() == position) {
            return QuizResultsFragment.newInstance(courseId, quizId);
        } else {
            final String questionId = this.questions.size() > position ? this.questions.get(position).getId() : null;
            return QuestionFragment.newInstance(courseId, quizId, questionId, this.showAnswers);
        }
    }

    @Override
    public int getItemPosition(final Object fragment) {
        if (fragment instanceof QuestionFragment) {
            // XXX: this is a bit of a hack to push showAnswers updates
            ((QuestionFragment) fragment).setShowAnswers(this.showAnswers);

            // search for this fragment
            final String questionId = ((QuestionFragment) fragment).getQuestionId();
            if (questionId != null) {
                for (int i = 0; i < this.questions.size(); i++) {
                    if (questionId.equals(this.questions.get(i).getId())) {
                        return i;
                    }
                }
            }
        } else if (fragment instanceof QuizResultsFragment) {
            // results fragment should always be last
            return this.questions.size();
        }

        // the object wasn't found
        return POSITION_NONE;
    }
}
