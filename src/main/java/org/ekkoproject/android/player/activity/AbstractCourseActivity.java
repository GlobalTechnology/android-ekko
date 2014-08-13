package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.ekkoproject.android.player.util.StringUtils;

import java.util.List;

public abstract class AbstractCourseActivity extends BaseActivity {
    protected long mCourseId = INVALID_COURSE;

    protected static Intent newIntent(final long courseId) {
        final Intent intent = new Intent();
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
        final Uri data;
        if (intent.hasExtra(EXTRA_COURSEID)) {
            mCourseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        } else if ((data = intent.getData()) != null) {
            final List<String> path = data.getPathSegments();
            for (int i = 0; i < path.size() - 1; i++) {
                if ("course".equalsIgnoreCase(path.get(i))) {
                    mCourseId = StringUtils.toLong(path.get(i + 1), INVALID_COURSE);
                    break;
                }
            }
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
}
