package org.ekkoproject.android.player.adapter;

import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;

public abstract class AbstractManifestLessonPagerAdapter extends AbstractManifestPagerAdapter {
    private final String lessonId;

    private Lesson lesson;

    public AbstractManifestLessonPagerAdapter(final FragmentManager fm, final String guid, final String lessonId) {
        super(fm, guid);
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
