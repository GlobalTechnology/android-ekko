package org.ekkoproject.android.player.adapter;

import org.appdev.entity.CourseContent;
import org.appdev.entity.Quiz;

import android.content.Context;

public abstract class AbstractManifestQuizAdapter<T> extends AbstractManifestContentAdapter<T> {
    private Quiz quiz = null;

    public AbstractManifestQuizAdapter(final Context context, final String quizId) {
        super(context, quizId);
    }

    @Override
    protected void onNewContent(final CourseContent content) {
        super.onNewContent(content);

        // check if this is a valid quiz
        Quiz quiz = null;
        if (content instanceof Quiz) {
            quiz = (Quiz) content;
        }
        this.onNewQuiz(quiz);
    }

    protected void onNewQuiz(final Quiz quiz) {
        this.quiz = quiz;
    }

    protected String getQuizId() {
        return this.getContentId();
    }

    protected Quiz getQuiz() {
        return this.quiz;
    }
}
