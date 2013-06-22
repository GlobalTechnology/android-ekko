package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.util.Collections;
import java.util.List;

import org.appdev.entity.Question;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.support.v4.fragment.QuestionFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ManifestQuizQuestionPagerAdapter extends AbstractManifestQuizPagerAdapter {
    private static final List<Question> NO_QUESTIONS = Collections.emptyList();

    private List<Question> questions = NO_QUESTIONS;
    
    public ManifestQuizQuestionPagerAdapter(final FragmentManager fm, final String quizId) {
        super(fm, quizId);
    }

    @Override
    protected void onNewQuiz(final Quiz quiz) {
        super.onNewQuiz(quiz);
        if (quiz != null) {
            this.questions = quiz.getQuestions();
        } else {
            this.questions = NO_QUESTIONS;
        }
    }

    @Override
    public int getCount() {
        return this.questions.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final Manifest manifest = this.getManifest();
        final long courseId = manifest != null ? manifest.getCourseId() : INVALID_COURSE;
        final Quiz quiz = this.getQuiz();
        final String quizId = quiz != null ? quiz.getId() : null;
        final String questionId = this.questions.size() > position ? this.questions.get(position).getGuid() : null;
        return QuestionFragment.newInstance(courseId, quizId, questionId);
    }

    @Override
    public int getItemPosition(final Object fragment) {
        if (fragment instanceof QuestionFragment) {
            // search for this fragment
            final String questionId = ((QuestionFragment) fragment).getQuestionId();
            if (questionId != null) {
                for (int i = 0; i < this.questions.size(); i++) {
                    if (questionId.equals(this.questions.get(i).getGuid())) {
                        return i;
                    }
                }
            }
        }

        // the object wasn't found
        return POSITION_NONE;
    }
}
