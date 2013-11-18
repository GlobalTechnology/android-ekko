package org.ekkoproject.android.player.activity;

import static org.ccci.gto.android.common.util.ThreadUtils.isUiThread;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.LICENSED_PROJECTS;
import static org.ekkoproject.android.player.Constants.STATE_GUID;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.thinkfree.showlicense.android.ShowLicense;

import org.ccci.gto.android.common.adapter.MenuListAdapter;
import org.ccci.gto.android.thekey.TheKeyImpl;
import org.ccci.gto.android.thekey.support.v4.dialog.LoginDialogFragment;
import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.fragment.CourseFragment;
import org.ekkoproject.android.player.support.v4.fragment.CourseListFragment;
import org.ekkoproject.android.player.sync.EkkoSyncService;

import me.thekey.android.TheKey;
import me.thekey.android.lib.content.TheKeyBroadcastReceiver;

public class MainActivity extends ActionBarActivity implements OnNavigationListener {
    private static final String STATE_DRAWER_INDICATOR = MainActivity.class + ".STATE_DRAWER_INDICATOR";

    private DrawerLayout drawerLayout = null;
    private ListView drawerView = null;
    private ActionBarDrawerToggle drawerToggle = null;

    private String mGuid;
    private TheKey mTheKey;
    private final TheKeyBroadcastReceiver mTheKeyReceiver = new TheKeyBroadcastReceiver() {
        @Override
        protected void onLogin(final String guid) {
            updateUser(guid);
        }

        @Override
        protected void onLogout(final String guid, final boolean changingUser) {
            // only update if we are not changing users, changing users will have a second onLogin broadcast
            if (!changingUser) {
                updateUser("GUEST");
            }
        }
    };

    public static Intent newIntent(final Context context) {
        return new Intent(context, MainActivity.class);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        mTheKey = TheKeyImpl.getInstance(this, THEKEY_CLIENTID);
        this.setContentView(R.layout.activity_main);
        this.findViews();
        this.setupActionBar();
        this.setupNavigationDrawer();

        if (savedState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                mGuid = savedState.getString(STATE_GUID, null);
            } else if (savedState.containsKey(STATE_GUID)) {
                mGuid = savedState.getString(STATE_GUID);
            }
        }
    }

    @Override
    protected void onPostCreate(final Bundle savedState) {
        super.onPostCreate(savedState);
        if (this.drawerToggle != null) {
            this.drawerToggle.syncState();

            if (savedState != null) {
                this.drawerToggle.setDrawerIndicatorEnabled(savedState.getBoolean(STATE_DRAWER_INDICATOR, true));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // update the title/icon
        // XXX: this is a hack, but the best way of dynamically managing it I could think of with current API's
        this.getSupportActionBar().setTitle("");

        // add menu items
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        // toggle Login/Logout MenuItems
        final MenuItem item = menu.findItem((mGuid == null || mGuid.equals(GUID_GUEST)) ? R.id.login : R.id.logout);
        if (item != null) {
            item.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTheKeyReceiver.registerReceiver(LocalBroadcastManager.getInstance(this));

        // update the current guid as necessary
        updateUser(mTheKey.getGuid());
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fm = getSupportFragmentManager();
        if (!fm.popBackStackImmediate()) {
            finish();
        } else if (fm.getBackStackEntryCount() == 0) {
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
            case R.id.myCourses:
                clearFragmentBackStack();
                showCourseList(false);
                return true;
            case R.id.allCourses:
                clearFragmentBackStack();
                showCourseList(true);
                return true;
            case R.id.login:
                this.showLoginDialog();
                return true;
            case R.id.logout:
                mTheKey.logout();
                return true;
            case R.id.about:
                ShowLicense.createDialog(this, "Open Source Software Used", LICENSED_PROJECTS).show();
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

    @Override
    protected void onPause() {
        super.onPause();
        mTheKeyReceiver.unregisterReceiver(LocalBroadcastManager.getInstance(this));
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_GUID, mGuid);
        if (this.drawerToggle != null) {
            outState.putBoolean(STATE_DRAWER_INDICATOR, this.drawerToggle.isDrawerIndicatorEnabled());
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

    private void updateUser(final String guid) {
        // update the current guid
        final String old = mGuid;
        mGuid = guid != null ? guid : GUID_GUEST;

        // did the current user change?
        if (!mGuid.equals(old)) {
            // trigger a fresh sync of the courses
            EkkoSyncService.syncCourses(this, mGuid);

            // reset fragments
            this.clearFragmentBackStack();
            this.showCourseList(false);

            // reset menus
            this.supportInvalidateOptionsMenu();
            updateNavigationDrawerMenu();
        }
    }

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

    private void setupActionBar() {
        final ActionBar ab = this.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
    }

    private void setupNavigationDrawer() {
        if (this.drawerView != null) {
            final MenuListAdapter adapter =
                    new MenuListAdapter(this, R.layout.activity_main_drawer_item, new MenuBuilder(this));
            adapter.setTitleResourceId(R.id.label);
            adapter.setIconResourceId(R.id.icon);
            this.getMenuInflater().inflate(R.menu.navigation_drawer_main, adapter.getMenu());
            this.drawerView.setAdapter(adapter);
            this.drawerView.setOnItemClickListener(new MenuListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                                        final MenuItem item) {
                    // close the drawerView
                    if (drawerLayout != null && drawerView != null) {
                        drawerLayout.closeDrawer(drawerView);
                    }

                    // pass as an option menu selection
                    onOptionsItemSelected(item);
                }
            });

            // update Nav Drawer menu
            updateNavigationDrawerMenu();
        }

        if (this.drawerLayout != null) {
            this.drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.drawable.ic_drawer,
                    R.string.drawer_open, R.string.drawer_close);
            this.drawerLayout.setDrawerListener(this.drawerToggle);
        }
    }

    private void updateNavigationDrawerMenu() {
        if (this.drawerView != null) {
            final ListAdapter adapter = this.drawerView.getAdapter();
            if (adapter instanceof MenuListAdapter) {
                final Menu menu = ((MenuListAdapter) adapter).getMenu();

                // update login/logout menu item state
                final MenuItem login = menu.findItem(R.id.login);
                final MenuItem logout = menu.findItem(R.id.logout);
                if (login != null) {
                    login.setVisible(mGuid == null || GUID_GUEST.equals(mGuid));
                }
                if (logout != null) {
                    logout.setVisible(mGuid != null && !GUID_GUEST.equals(mGuid));
                }

                ((MenuListAdapter) adapter).synchronizeMenu();
            }
        }
    }

    private void showLoginDialog() {
        // Create and show the login dialog only if it is not currently displayed
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null) {
            LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build().show(
                    fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }

    private void setDrawerIndicatorEnabled(final boolean enable) {
        if (this.drawerToggle != null) {
            this.drawerToggle.setDrawerIndicatorEnabled(enable);
        }
    }
}
