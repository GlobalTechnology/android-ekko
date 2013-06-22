package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.util.Collections;
import java.util.List;

import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.support.v4.fragment.lesson.TextFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ManifestLessonTextPagerAdapter extends AbstractManifestLessonPagerAdapter {
    private static final List<String> NO_TEXT = Collections.emptyList();

    private List<String> text = NO_TEXT;

    public ManifestLessonTextPagerAdapter(final FragmentManager fm, final String lessonId) {
        super(fm, lessonId);
    }

    @Override
    protected void onNewLesson(Lesson lesson) {
        super.onNewLesson(lesson);

        if (lesson != null) {
            this.text = lesson.getText();
        } else {
            this.text = NO_TEXT;
        }
    }

    @Override
    public int getCount() {
        return this.text.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final Manifest manifest = this.getManifest();
        final long courseId = manifest != null ? manifest.getCourseId() : INVALID_COURSE;
        final Lesson lesson = this.getLesson();
        final String lessonId = lesson != null ? lesson.getId() : null;
        return TextFragment.newInstance(courseId, lessonId, position);
    }
}
