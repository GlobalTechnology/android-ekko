package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;

public class CourseActivity extends BaseActivity {
    private long mCourseId = INVALID_COURSE;

    public static Intent newIntent(final Context context, final long courseId) {
        final Intent intent = new Intent(context, CourseActivity.class);
        intent.putExtra(EXTRA_COURSEID, courseId);
        return intent;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        setupNavigationDrawer();

        // resolve the course id
        final Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_COURSEID)) {
            mCourseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        }

        // show course fragment if we aren't restoring state
        if (savedState == null) {
            showCourseFragment();
        }
    }

    /* END lifecycle */

    private void setupNavigationDrawer() {
        if (mDrawerToggle != null) {
            // disable the drawer indicator
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    @Override
    public void showCourseList(boolean showAll) {
        super.showCourseList(showAll);
        finish();
    }

    private void showCourseFragment() {
        final CourseFragment fragment =
                CourseFragment.newInstance(R.layout.fragment_course_drawer_wrapper, mGuid, mCourseId);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }
}
