package org.ekkoproject.android.player.adapter;

import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Lesson;

import android.content.Context;

public abstract class AbstractManifestLessonAdapter<T> extends AbstractManifestContentAdapter<T> {
    private Lesson lesson;

    public AbstractManifestLessonAdapter(final Context context, final String lessonId) {
        super(context, lessonId);
    }

    @Override
    protected void onNewContent(final CourseContent content) {
        super.onNewContent(content);

        // check if this is a valid lesson
        Lesson lesson = null;
        if (content instanceof Lesson) {
            lesson = (Lesson) content;
        }
        this.onNewLesson(lesson);
    }

    protected void onNewLesson(final Lesson lesson) {
        this.lesson = lesson;
    }

    protected String getLessonId() {
        return this.getContentId();
    }

    protected Lesson getLesson() {
        return this.lesson;
    }
}
