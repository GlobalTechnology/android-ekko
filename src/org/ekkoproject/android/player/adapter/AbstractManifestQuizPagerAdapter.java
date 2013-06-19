package org.ekkoproject.android.player.adapter;

import org.appdev.entity.Quiz;
import org.ekkoproject.android.player.model.Manifest;

import android.support.v4.app.FragmentManager;

public abstract class AbstractManifestQuizPagerAdapter extends AbstractManifestPagerAdapter {
    private final String quizId;

    private Quiz quiz = null;

    public AbstractManifestQuizPagerAdapter(final FragmentManager fm, final String quizId) {
        super(fm);
        this.quizId = quizId;
    }

    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        Quiz quiz = null;
        if (manifest != null) {
            quiz = manifest.getQuiz(this.quizId);
        }
        this.onNewQuiz(quiz);
    }

    protected void onNewQuiz(final Quiz quiz) {
        this.quiz = quiz;
    }

    protected Quiz getQuiz() {
        return this.quiz;
    }
}
