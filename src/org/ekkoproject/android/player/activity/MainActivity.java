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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements LoginDialogFragment.Listener,
        CourseListFragment.Listener {
    private DrawerLayout drawerLayout = null;
    private ListView drawerView = null;
    private ActionBarDrawerToggle drawerToggle = null;

    public static final Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /** BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.findViews();
        this.setupActionBar();
        this.setupNavigationDrawer();
        this.initFragments();
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (this.drawerToggle != null) {
            this.drawerToggle.syncState();
        }
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
        // update the title/icon
        // XXX: this is a hack, but the best way of dynamically managing it I
        // could think of with current API's
        this.getSupportActionBar().setTitle("EKKO");

        // add menu items
        final MenuInflater inflater = getSupportMenuInflater();
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
    }

    @Override
    public void onLoginFailure(final LoginDialogFragment dialog) {
        // XXX: do nothing for now
    }

    @Override
    public void onSelectCourse(final CourseListFragment fragment, final long courseId) {
        openCourse(courseId);
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

    /** END lifecycle */

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
        // attach the course list fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_content, CourseListFragment.newInstance(R.layout.course_list_main, true)).commit();
    }

    private void openCourse(final long courseId) {
        // attach the course list fragment
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in,
                        R.anim.slide_right_out)
                .replace(R.id.frame_content,
                        CourseFragment.newInstance(R.layout.fragment_course_drawer_wrapper, courseId, true))
                .addToBackStack(null).commit();

        // update the DrawerLayout ActionBar toggle
        if (this.drawerToggle != null) {
            this.drawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setupNavigationDrawer() {
        if (this.drawerView != null) {
            // TODO: come up with a sane way of managing navigation items
            this.drawerView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main_drawer_item, new String[] {
                    "Login", "Languages" }));
            this.drawerView.setOnItemClickListener(new DrawerOnItemClickListener());
        }

        if (this.drawerLayout != null) {
            this.drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open, R.string.drawer_close);
            this.drawerLayout.setDrawerListener(this.drawerToggle);
        }
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

    private class DrawerOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MainActivity.this.onNavigationDrawerItemSelected(position);
        }
    }
}
