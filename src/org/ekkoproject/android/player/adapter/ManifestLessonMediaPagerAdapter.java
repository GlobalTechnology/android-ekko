package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.util.Collections;
import java.util.List;

import org.appdev.entity.Lesson;
import org.appdev.entity.Media;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.support.v4.fragment.lesson.MediaFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ManifestLessonMediaPagerAdapter extends AbstractManifestLessonPagerAdapter {
    private static final List<Media> NO_MEDIA = Collections.emptyList();

    private List<Media> media = NO_MEDIA;

    public ManifestLessonMediaPagerAdapter(final FragmentManager fm, final String lessonId) {
        super(fm, lessonId);
    }

    @Override
    protected void onNewLesson(final Lesson lesson) {
        super.onNewLesson(lesson);

        if (lesson != null) {
            this.media = lesson.getMedia();
        } else {
            this.media = NO_MEDIA;
        }
    }

    @Override
    public int getCount() {
        return this.media.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final Manifest manifest = this.getManifest();
        final long courseId = manifest != null ? manifest.getCourseId() : INVALID_COURSE;
        final Lesson lesson = this.getLesson();
        final String lessonId = lesson != null ? lesson.getId() : null;
        final Media media = this.media.get(position);
        final String mediaId = media != null ? media.getId() : null;
        return MediaFragment.newInstance(courseId, lessonId, mediaId);
    }
}
