package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_ID;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.model.Question;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.CourseManager;
import org.ekkoproject.android.player.support.v4.fragment.QuestionFragment;
import org.ekkoproject.android.player.support.v4.fragment.QuizResultsFragment;

import java.util.Collections;
import java.util.List;

public class ManifestQuizContentPagerAdapter extends AbstractManifestQuizPagerAdapter {
    private static final List<Question> NO_QUESTIONS = Collections.emptyList();

    private List<Question> questions = NO_QUESTIONS;

    public ManifestQuizContentPagerAdapter(final FragmentManager fm, final String guid, final String quizId) {
        super(fm, guid, quizId);
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

    @Override
    public int getCount() {
        return this.questions.size() + 1;
    }

    @Override
    public Fragment getItem(final int position) {
        final long courseId = this.getCourseId();
        final Quiz quiz = this.getQuiz();
        final String quizId = quiz != null ? quiz.getId() : null;

        if (position >= 0 && position < this.questions.size()) {
            final String questionId = this.questions.get(position).getId();
            return QuestionFragment.newInstance(mGuid, courseId, quizId, questionId);
        } else if (this.questions.size() == position) {
            return QuizResultsFragment.newInstance(mGuid, courseId, quizId);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(final int position) {
        if (position >= 0 && position < this.questions.size()) {
            return CourseManager.convertId(this.getCourseId(), this.questions.get(position).getId());
        } else if (position == this.questions.size()) {
            return CourseManager.convertId(this.getCourseId(), "QuizResultsFragment");
        } else {
            return INVALID_ID;
        }
    }

    @Override
    public int getItemPosition(final Object fragment) {
        if (fragment instanceof QuestionFragment) {
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
