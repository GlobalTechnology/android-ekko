package org.ekkoproject.android.player.adapter;

import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;

import android.support.v4.app.FragmentManager;

public abstract class AbstractManifestLessonPagerAdapter extends AbstractManifestPagerAdapter {
    private final String lessonId;

    private Lesson lesson;

    public AbstractManifestLessonPagerAdapter(final FragmentManager fm, final String lessonId) {
        super(fm);
        this.lessonId = lessonId;
    }

    @Override
    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        // update the lesson
        Lesson lesson = null;
        if (manifest != null) {
            lesson = manifest.getLesson(this.lessonId);
        }
        this.onNewLesson(lesson);
    }

    protected void onNewLesson(final Lesson lesson) {
        this.lesson = lesson;
    }

    protected Lesson getLesson() {
        return this.lesson;
    }
}
