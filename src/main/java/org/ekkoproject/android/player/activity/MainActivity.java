package org.ekkoproject.android.player.activity;

import static org.ccci.gto.android.common.util.ThreadUtils.isUiThread;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.ccci.gto.android.thekey.TheKeyImpl;
import org.ccci.gto.android.thekey.support.v4.dialog.LoginDialogFragment;
import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;
import org.ekkoproject.android.player.sync.EkkoSyncService;

import me.thekey.android.TheKey;

public class MainActivity extends ActionBarActivity implements LoginDialogFragment.Listener, OnNavigationListener {
    private DrawerLayout drawerLayout = null;
    private ListView drawerView = null;
    private ActionBarDrawerToggle drawerToggle = null;

    private TheKey thekey;

    public static final Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.thekey = new TheKeyImpl(this, THEKEY_CLIENTID);
        this.setContentView(R.layout.activity_main);
        this.findViews();
        this.setupActionBar();
        this.setupNavigationDrawer();

        if(savedInstanceState == null) {
            this.initFragments();

            // display the login dialog if we don't have a valid GUID
            if (this.thekey.getGuid() == null) {
                this.showLoginDialog();
            } else {
                // trigger a sync
                EkkoSyncService.syncCourses(this);
            }
        }
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (this.drawerToggle != null) {
            this.drawerToggle.syncState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // update the title/icon
        // XXX: this is a hack, but the best way of dynamically managing it I
        // could think of with current API's
        this.getSupportActionBar().setTitle("");

        // add menu items
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fm = getSupportFragmentManager();
        if (!fm.popBackStackImmediate()) {
            finish();
        } else if (fm.getBackStackEntryCount() == 0) {
            // enable the drawer indicator
            if (this.drawerToggle != null) {
                this.drawerToggle.setDrawerIndicatorEnabled(true);
            }
        }
    }

    @Override
    public void onLoginSuccess(final LoginDialogFragment dialog, final String guid) {
        EkkoSyncService.syncCourses(this);

        // reset the fragment back stack
        this.resetFragmentBackStack(guid);
    }

    @Override
    public void onLoginFailure(final LoginDialogFragment dialog) {
        // XXX: do nothing for now
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

    // TODO: come up with a sane way of managing navigation items
    private boolean onNavigationDrawerItemSelected(final int position) {
        if (this.drawerLayout != null && this.drawerView != null) {
            this.drawerView.setItemChecked(position, true);
            this.drawerLayout.closeDrawer(this.drawerView);
        }

        switch (position) {
        case 0:
            this.showLoginDialog();
            return true;
        default:
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // handle drawer navigation toggle
            if (this.drawerLayout != null && this.drawerToggle != null) {
                if (this.drawerToggle.isDrawerIndicatorEnabled()) {
                    if (this.drawerLayout.isDrawerVisible(GravityCompat.START)) {
                        this.drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        this.drawerLayout.openDrawer(GravityCompat.START);
                    }
                    return true;
                }
            }

            // trigger the back function
            this.onBackPressed();
            return true;
        case R.id.login:
            this.showLoginDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.drawerToggle != null) {
            this.drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    /* END lifecycle */

    private void findViews() {
        this.drawerLayout = findView(DrawerLayout.class, R.id.drawer_layout);
        this.drawerView = findView(ListView.class, R.id.drawer_content);
    }

    private <T extends View> T findView(final Class<T> clazz, final int id) {
        final View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    private void initFragments() {
        // show the course list
        this.showCourseList(this.thekey.getGuid());
    }

    private void resetFragmentBackStack(final String guid) {
        assert isUiThread() : "the fragment back stack should only be reset on the ui thread";

        // clear the back stack in the fragment manager
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            // pop the base back stack entry
            fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // show the server list as the base fragment
        this.showCourseList(guid);
    }

    private void showCourseList(final String guid) {
        final CourseListFragment fragment =
                CourseListFragment.newInstance(guid != null ? guid : GUID_GUEST, R.layout.fragment_course_list_main);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragment).commit();
    }

    private void openCourse(final long courseId) {
        // attach the course list fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, CourseFragment
                .newInstance(R.layout.fragment_course_drawer_wrapper, courseId)).addToBackStack(null).commit();

        // update the DrawerLayout ActionBar toggle
        if (this.drawerToggle != null) {
            this.drawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    private void setupActionBar() {
        final ActionBar ab = this.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
    }

    private void setupNavigationDrawer() {
        if (this.drawerView != null) {
            // TODO: come up with a sane way of managing navigation items
            this.drawerView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main_drawer_item,
                    new String[] { "Logout" }));
            this.drawerView.setOnItemClickListener(new DrawerOnItemClickListener());
        }

        if (this.drawerLayout != null) {
            this.drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open, R.string.drawer_close);
            this.drawerLayout.setDrawerListener(this.drawerToggle);
        }
    }

    private void showLoginDialog() {
        // Create and show the login dialog only if it is not currently displayed
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null) {
            LoginDialogFragment
                    .newInstance(THEKEY_CLIENTID)
                    .show(fm.beginTransaction().addToBackStack("loginDialog"),
                          "loginDialog");
        }
    }

    private class DrawerOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MainActivity.this.onNavigationDrawerItemSelected(position);
        }
    }
}
