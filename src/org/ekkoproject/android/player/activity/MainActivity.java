package org.ekkoproject.android.player.activity;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainActivity extends SherlockFragmentActivity implements CourseListFragment.Listener {
    public static final Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /** BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.initFragments();
    }

    @Override
    public void onSelectCourse(final CourseListFragment fragment, final long courseId) {
        displayCourse(courseId);
    }

    /** END lifecycle */

    private void initFragments() {
        // attach the course list fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content, CourseListFragment.newInstance(R.layout.course_list_main, true)).commit();
    }

    private void displayCourse(final long courseId) {
        // attach the course list fragment
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in,
                        R.anim.slide_right_out).replace(R.id.frame_content, CourseFragment.newInstance(courseId, true))
                .addToBackStack(null).commit();
    }
}
