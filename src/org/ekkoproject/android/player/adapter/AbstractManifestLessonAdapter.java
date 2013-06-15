package org.ekkoproject.android.player.adapter;

import org.appdev.entity.CourseContent;
import org.appdev.entity.Lesson;
import org.ekkoproject.android.player.model.Manifest;

import android.content.Context;

public abstract class AbstractManifestLessonAdapter<T> extends AbstractManifestAdapter<T> {
    private final String lessonId;

    private Lesson lesson;

    public AbstractManifestLessonAdapter(final Context context, final String lessonId) {
        super(context);
        this.lessonId = lessonId;
    }

    protected Lesson getLesson() {
        return this.lesson;
    }

    @Override
    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        // find the lesson
        Lesson lesson = null;
        if (manifest != null && this.lessonId != null) {
            for (final CourseContent content : manifest.getContent()) {
                if (content instanceof Lesson && this.lessonId.equals(content.getId())) {
                    lesson = (Lesson) content;
                    break;
                }
            }
        }
        this.onNewLesson(lesson);
    }

    protected void onNewLesson(final Lesson lesson) {
        this.lesson = lesson;
    }
}
