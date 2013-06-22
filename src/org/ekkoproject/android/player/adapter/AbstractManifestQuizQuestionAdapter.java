package org.ekkoproject.android.player.adapter;

import org.appdev.entity.Question;
import org.ekkoproject.android.player.model.Quiz;

import android.content.Context;

public abstract class AbstractManifestQuizQuestionAdapter<T> extends AbstractManifestQuizAdapter<T> {
    private Question question = null;

    private final String questionId;

    public AbstractManifestQuizQuestionAdapter(final Context context, final String quizId, String questionId) {
        super(context, quizId);
        this.questionId = questionId;
    }

    protected void onNewQuiz(final Quiz quiz) {
        super.onNewQuiz(quiz);

        Question question = null;
        if (quiz != null) {
            question = quiz.getQuestion(this.questionId);
        }
        this.onNewQuestion(question);
    }

    protected void onNewQuestion(final Question question) {
        this.question = question;
    }

    protected String getQuestionId() {
        return this.questionId;
    }

    protected Question getQuestion() {
        return this.question;
    }
}
