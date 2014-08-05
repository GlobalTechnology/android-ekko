package org.ekkoproject.android.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.ccci.gto.android.common.util.ThreadUtils;
import org.ekkoproject.android.player.BuildConfig;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourseListActivity extends BaseActivity {
    private static final Logger LOG = LoggerFactory.getLogger(CourseListActivity.class);

    private static final String EXTRA_SHOWALL_COURSES = CourseListActivity.class.getName() + ".EXTRA_SHOWALL_COURSES";

    public static Intent newIntent(final Context context, final boolean showAll) {
        final Intent intent = new Intent(context, CourseListActivity.class);
        intent.putExtra(EXTRA_SHOWALL_COURSES, showAll);
        return intent;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // observe the show all flag in the Intent if we are not restoring state
        if (savedState == null) {
            final Intent intent = getIntent();
            showCourseListFragment(intent.getBooleanExtra(EXTRA_SHOWALL_COURSES, false));
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        // change the visible course list if a new one is specified
        if (intent.hasExtra(EXTRA_SHOWALL_COURSES)) {
            showCourseListFragment(intent.getBooleanExtra(EXTRA_SHOWALL_COURSES, false));
        }
    }

    /* END lifecycle */

    @Override
    public void showCourseList(final boolean showAll) {
        if (BuildConfig.DEBUG) {
            ThreadUtils.assertOnUiThread();
        }

        showCourseListFragment(showAll);
    }

    private void showCourseListFragment(final boolean showAll) {
        final CourseListFragment fragment =
                CourseListFragment.newInstance(mGuid, R.layout.fragment_course_list_main, showAll);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }
}
