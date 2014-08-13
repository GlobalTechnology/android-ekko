package org.ekkoproject.android.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;

public class CourseActivity extends AbstractCourseActivity {
    public static Intent newIntent(final Context context, final long courseId) {
        final Intent intent = AbstractCourseActivity.newIntent(courseId);
        intent.setClass(context, CourseActivity.class);
        return intent;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // show course fragment if we aren't restoring state
        if (savedState == null) {
            showCourseFragment();
        }
    }

    /* END lifecycle */

    private void showCourseFragment() {
        final CourseFragment fragment =
                CourseFragment.newInstance(R.layout.fragment_course_drawer_wrapper, mGuid, mCourseId);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }
}
