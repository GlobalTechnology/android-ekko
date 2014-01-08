package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.INVALID_ID;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.CourseManager;
import org.ekkoproject.android.player.support.v4.fragment.AbstractContentFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseCompletionFragment;
import org.ekkoproject.android.player.support.v4.fragment.LessonFragment;
import org.ekkoproject.android.player.support.v4.fragment.QuizFragment;

import java.util.Collections;
import java.util.List;

public class ManifestContentPagerAdapter extends AbstractManifestPagerAdapter {
    private static final List<CourseContent> NO_CONTENT = Collections.emptyList();

    private List<CourseContent> content = NO_CONTENT;

    public ManifestContentPagerAdapter(final FragmentManager fm, final String guid) {
        super(fm, guid);
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
        return this.content.size() + 1;
    }

    @Override
    public Fragment getItem(final int position) {
        final long courseId = this.getCourseId();
        if (position >= 0 && position < this.content.size()) {
            final CourseContent item = this.content.get(position);

            final Fragment fragment;
            if (item instanceof Lesson) {
                fragment = LessonFragment.newInstance(mGuid, courseId, item.getId());
            } else if (item instanceof Quiz) {
                fragment = QuizFragment.newInstance(mGuid, courseId, item.getId());
            } else {
                fragment = null;
            }
            return fragment;
        } else if(position == this.content.size()) {
            return CourseCompletionFragment.newInstance(mGuid, courseId);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(final int position) {
        if (position >= 0 && position < this.content.size()) {
            return CourseManager.convertId(this.getCourseId(), this.content.get(position).getId());
        } else if (position == this.content.size()) {
            return CourseManager.convertId(this.getCourseId(), "CourseCompletionFragment");
        } else {
            return INVALID_ID;
        }
    }

    @Override
    public int getItemPosition(final Object fragment) {
        // get the contentId represented by the fragment
        final String contentId;
        if (fragment instanceof CourseCompletionFragment) {
            return this.content.size();
        } else if (fragment instanceof AbstractContentFragment) {
            contentId = ((AbstractContentFragment) fragment).getContentId();
        } else if (fragment instanceof LessonFragment) {
            contentId = ((LessonFragment) fragment).getLessonId();
        } else {
            contentId = null;
        }

        // find the position of the contentId and make sure it is the correct type
        if (contentId != null) {
            for (int i = 0; i < this.content.size(); i++) {
                final CourseContent content = this.content.get(i);
                if (contentId.equals(content.getId())) {
                    if ((fragment instanceof LessonFragment && content instanceof Lesson) ||
                            (fragment instanceof QuizFragment && content instanceof Quiz)) {
                        return i;
                    }
                }
            }
        }

        // the fragment wasn't found
        return POSITION_NONE;
    }
}
