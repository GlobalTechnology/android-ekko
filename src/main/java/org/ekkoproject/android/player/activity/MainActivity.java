package org.ekkoproject.android.player.activity;

import static org.ccci.gto.android.common.util.ThreadUtils.isUiThread;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;

public class MainActivity extends BaseActivity implements OnNavigationListener {
    public static Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /* BEGIN lifecycle */

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // toggle Drawer Indicator if we have nothing left in the fragment stack
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() == 0) {
            // enable the drawer indicator
            this.setDrawerIndicatorEnabled(true);
        }
    }

    @Override
    public void onSelectCourse(final long courseId) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                openCourse(courseId);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myCourses:
                clearFragmentBackStack();
                showCourseList(false);
                return true;
            case R.id.allCourses:
                clearFragmentBackStack();
                showCourseList(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onChangingUser(final String oldGuid, final String newGuid) {
        super.onChangingUser(oldGuid, newGuid);

        // reset fragments
        this.clearFragmentBackStack();
        this.showCourseList(false);

        // reset menus
        this.supportInvalidateOptionsMenu();
    }

    /* END lifecycle */

    private void clearFragmentBackStack() {
        assert isUiThread() : "the fragment back stack should only be cleared on the ui thread";

        // clear the back stack in the fragment manager
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            // pop the base back stack entry
            fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // enable the drawer indicator
        this.setDrawerIndicatorEnabled(true);
    }

    private void showCourseList(final boolean showAll) {
        final CourseListFragment fragment =
                CourseListFragment.newInstance(mGuid, R.layout.fragment_course_list_main, showAll);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }

    private void openCourse(final long courseId) {
        final CourseFragment fragment =
                CourseFragment.newInstance(R.layout.fragment_course_drawer_wrapper, mGuid, courseId);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).addToBackStack(null)
                .commit();

        // disable the DrawerLayout drawer ActionBar toggle
        this.setDrawerIndicatorEnabled(false);
    }

    private void setDrawerIndicatorEnabled(final boolean enable) {
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(enable);
        }
    }
}
