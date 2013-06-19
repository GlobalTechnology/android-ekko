package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.util.Collections;
import java.util.List;

import org.appdev.entity.CourseContent;
import org.appdev.entity.Lesson;
import org.appdev.entity.Quiz;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.support.v4.fragment.LessonFragment;
import org.ekkoproject.android.player.support.v4.fragment.QuizFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ManifestContentPagerAdapter extends AbstractManifestPagerAdapter {
    private static final List<CourseContent> NO_CONTENT = Collections.emptyList();

    private List<CourseContent> content = NO_CONTENT;

    public ManifestContentPagerAdapter(final FragmentManager fm) {
        super(fm);
    }

    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        if (manifest != null) {
            this.content = manifest.getContent();
        } else {
            this.content = NO_CONTENT;
        }
    }

    @Override
    public int getCount() {
        return this.content.size();
    }

    @Override
    public Fragment getItem(final int position) {
        final Manifest manifest = this.getManifest();
        final long courseId = manifest != null ? manifest.getCourseId() : INVALID_COURSE;
        final CourseContent item = this.content.get(position);

        final Fragment fragment;
        if (item instanceof Lesson) {
            fragment = LessonFragment.newInstance(courseId, item.getId());
        } else if (item instanceof Quiz) {
            fragment = QuizFragment.newInstance(courseId, item.getId());
        } else {
            fragment = null;
        }
        return fragment;
    }

    @Override
    public int getItemPosition(final Object fragment) {
        // get the contentId represented by the fragment
        final String contentId;
        if (fragment instanceof LessonFragment) {
            contentId = ((LessonFragment) fragment).getLessonId();
        } else if (fragment instanceof QuizFragment) {
            contentId = ((QuizFragment) fragment).getQuizId();
        } else {
            contentId = null;
        }

        // find the position of the contentId and make sure it is the correct
        // type
        if (contentId != null) {
            for (int i = 0; i < this.content.size(); i++) {
                final CourseContent content = this.content.get(i);
                if (contentId.equals(content.getId())) {
                    if ((fragment instanceof LessonFragment && content instanceof Lesson)
                            || (fragment instanceof QuizFragment && content instanceof Quiz)) {
                        return i;
                    }
                }
            }
        }

        // the fragment wasn't found
        return POSITION_NONE;
    }
}
