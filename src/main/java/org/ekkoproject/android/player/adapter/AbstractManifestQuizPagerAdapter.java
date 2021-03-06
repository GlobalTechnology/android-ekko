package org.ekkoproject.android.player.adapter;

import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Quiz;

public abstract class AbstractManifestQuizPagerAdapter extends AbstractManifestPagerAdapter {
    private final String quizId;

    private Quiz quiz = null;

    public AbstractManifestQuizPagerAdapter(final FragmentManager fm, final String guid, final String quizId) {
        super(fm, guid);
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
