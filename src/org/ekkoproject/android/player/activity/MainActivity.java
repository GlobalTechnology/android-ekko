package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import org.ccci.gto.android.thekey.TheKey;
import org.ccci.gto.android.thekey.support.v4.dialog.LoginDialogFragment;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;
import org.ekkoproject.android.player.sync.EkkoSyncService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements LoginDialogFragment.Listener,
        CourseListFragment.Listener {

    public static final Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /** BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setupActionBar();
        this.initFragments();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // display the login dialog if we don't have a valid GUID
        final TheKey thekey = new TheKey(this, THEKEY_CLIENTID);
        if (thekey.getGuid() == null) {
            this.showLoginDialog();
        } else {
            // trigger a sync
            EkkoSyncService.syncCourses(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onLoginSuccess(final LoginDialogFragment dialog, final String guid) {
        EkkoSyncService.syncCourses(this);
    }

    @Override
    public void onLoginFailure(final LoginDialogFragment dialog) {
        // XXX: do nothing for now
    }

    @Override
    public void onSelectCourse(final CourseListFragment fragment, final long courseId) {
        displayCourse(courseId);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            final Intent intent = newIntent(this);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        case R.id.login:
            this.showLoginDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
                        R.anim.slide_right_out)
                .replace(R.id.frame_content,
                        CourseFragment.newInstance(R.layout.fragment_course_slidingmenu_wrapper, courseId, true))
                .addToBackStack(null).commit();
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void showLoginDialog() {
        final FragmentManager fm = this.getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        final Fragment prev = fm.findFragmentByTag("loginDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        final LoginDialogFragment newFragment = LoginDialogFragment.newInstance(THEKEY_CLIENTID);
        newFragment.show(ft, "loginDialog");
    }
}
