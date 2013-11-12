package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Text;
import org.ekkoproject.android.player.support.v4.fragment.lesson.TextFragment;

import java.util.Collections;
import java.util.List;

public class ManifestLessonTextPagerAdapter extends AbstractManifestLessonPagerAdapter {
    private static final List<Text> NO_TEXT = Collections.emptyList();

    private List<Text> text = NO_TEXT;

    public ManifestLessonTextPagerAdapter(final FragmentManager fm, final String guid, final String lessonId) {
        super(fm, guid, lessonId);
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
        final Text text = this.text.get(position);
        final String textId = text != null ? text.getId() : null;
        return TextFragment.newInstance(mGuid, courseId, lessonId, textId);
    }
}
