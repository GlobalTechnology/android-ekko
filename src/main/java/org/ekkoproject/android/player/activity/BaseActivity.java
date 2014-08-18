package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.BuildConfig.THEKEY_CLIENTID;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.LICENSED_PROJECTS;
import static org.ekkoproject.android.player.Constants.STATE_GUID;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
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
import org.ccci.gto.android.common.util.ThreadUtils;
import org.ekkoproject.android.player.BuildConfig;
import org.ekkoproject.android.player.NavigationListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.services.GoogleAnalyticsManager;
import org.ekkoproject.android.player.sync.EkkoSyncService;

import java.util.Date;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;
import me.thekey.android.lib.content.TheKeyBroadcastReceiver;
import me.thekey.android.lib.support.v4.dialog.LoginDialogFragment;

public abstract class BaseActivity extends ActionBarActivity implements NavigationListener {
    private static final String STATE_DRAWER_INDICATOR = BaseActivity.class + ".STATE_DRAWER_INDICATOR";

    private static final long INITIAL_SYNC_MAX_AGE = 3 * 60 * 60 * 1000; // 3 hours
    private static Date mLastSync;

    DrawerLayout mDrawerLayout;
    ListView mDrawerView;
    ActionBarDrawerToggle mDrawerToggle;

    private GoogleAnalyticsManager mGoogleAnalytics;
    TheKey mTheKey;

    String mGuid;
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

    /* BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(this);
        mTheKey = TheKeyImpl.getInstance(this, THEKEY_CLIENTID);
        this.setContentView(R.layout.activity_base);
        this.findViews();
        this.setupActionBar();
        this.setupNavigationDrawer();

        // load the current guid from the saved state
        if (savedState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                mGuid = savedState.getString(STATE_GUID, null);
            } else if (savedState.containsKey(STATE_GUID)) {
                mGuid = savedState.getString(STATE_GUID);
            }
        }

        // initialize guid if we don't have one
        if (mGuid == null) {
            updateUser(mTheKey.getGuid());
        }

        // start an initial background sync if we haven't done one recently
        // XXX: is this necessary?
        if (mLastSync == null || mLastSync.before(new Date(System.currentTimeMillis() - INITIAL_SYNC_MAX_AGE))) {
            EkkoSyncService.syncCourses(this, mGuid);
            mLastSync = new Date();
        }
    }

    @Override
    protected void onPostCreate(final Bundle savedState) {
        super.onPostCreate(savedState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();

            if (savedState != null) {
                mDrawerToggle.setDrawerIndicatorEnabled(savedState.getBoolean(STATE_DRAWER_INDICATOR, true));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTheKeyReceiver.registerReceiver(LocalBroadcastManager.getInstance(this));

        // update the current guid as necessary
        updateUser(mTheKey.getGuid());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // update the title/icon
        // XXX: this is a hack, but the best way of dynamically managing it I could think of with current API's
        this.getSupportActionBar().setTitle("");

        // add menu items
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_base, menu);

        // toggle Login/Logout MenuItems
        final MenuItem item = menu.findItem((mGuid == null || mGuid.equals(GUID_GUEST)) ? R.id.login : R.id.logout);
        if (item != null) {
            item.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    protected void onChangingUser(final String oldGuid, final String newGuid) {
        // trigger a fresh sync of the courses
        EkkoSyncService.syncCourses(this, newGuid);

        // reset menus
        supportInvalidateOptionsMenu();
        updateNavigationDrawerMenu();

        // show root My Courses if the user has changed
        if (oldGuid != null) {
            this.showCourseList(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // handle drawer navigation toggle
                if (mDrawerLayout != null && mDrawerToggle != null) {
                    if (mDrawerToggle.isDrawerIndicatorEnabled()) {
                        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            mDrawerLayout.openDrawer(GravityCompat.START);
                        }
                        return true;
                    }
                }

                // Navigate up the activity stack
                final Intent intent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, intent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities();
                } else {
                    NavUtils.navigateUpTo(this, intent);
                }
                overridePendingTransition(0, 0);

                return true;
            case R.id.myCourses:
                this.showCourseList(false);
                return true;
            case R.id.allCourses:
                this.showCourseList(true);
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
    public void onBackPressed() {
        final FragmentManager fm = getSupportFragmentManager();
        if (!fm.popBackStackImmediate()) {
            finish();
            final Intent intent = getIntent();
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NO_ANIMATION) != 0) {
                overridePendingTransition(0, 0);
            }
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
        if (mDrawerToggle != null) {
            outState.putBoolean(STATE_DRAWER_INDICATOR, mDrawerToggle.isDrawerIndicatorEnabled());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        mDrawerLayout = findView(DrawerLayout.class, R.id.drawer_layout);
        mDrawerView = findView(ListView.class, R.id.drawer_content);
    }

    private void clearViews() {
        mDrawerLayout = null;
        mDrawerView = null;
    }

    protected final <T extends View> T findView(final Class<T> clazz, final int id) {
        final View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    private void setupActionBar() {
        final ActionBar ab = this.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
    }

    private void setupNavigationDrawer() {
        // setup drawer toggle
        if (mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
                                                      R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        // setup drawer menu
        if (mDrawerView != null) {
            final MenuListAdapter adapter =
                    new MenuListAdapter(this, R.layout.activity_base_drawer_item, new MenuBuilder(this));
            adapter.setTitleResourceId(R.id.label);
            adapter.setIconResourceId(R.id.icon);
            this.getMenuInflater().inflate(R.menu.navigation_drawer_base, adapter.getMenu());
            mDrawerView.setAdapter(adapter);
            mDrawerView.setOnItemClickListener(new MenuListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                                        final MenuItem item) {
                    // close the mDrawerView
                    if (mDrawerLayout != null && mDrawerView != null) {
                        mDrawerLayout.closeDrawer(mDrawerView);
                    }

                    // pass as an option menu selection
                    onOptionsItemSelected(item);
                }
            });

            // update Nav Drawer menu
            updateNavigationDrawerMenu();
        }
    }

    private void updateNavigationDrawerMenu() {
        if (mDrawerView != null) {
            final ListAdapter adapter = mDrawerView.getAdapter();
            if (adapter instanceof MenuListAdapter) {
                final Menu menu = ((MenuListAdapter) adapter).getMenu();

                // update login/logout menu item state
                final MenuItem login = menu.findItem(R.id.login);
                final MenuItem logout = menu.findItem(R.id.logout);
                final boolean guest = mGuid == null || GUID_GUEST.equals(mGuid);
                if (login != null) {
                    login.setVisible(guest);
                }
                if (logout != null) {
                    logout.setVisible(!guest);
                }

                // synchronize the menu state
                ((MenuListAdapter) adapter).synchronizeMenu();
            }
        }
    }

    @Override
    public void showCourseList(final boolean showAll) {
        if (BuildConfig.DEBUG) {
            ThreadUtils.assertOnUiThread();
        }

        final Intent intent = CourseListActivity.newIntent(this, showAll);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void showCourse(final long courseId) {
        if (BuildConfig.DEBUG) {
            ThreadUtils.assertOnUiThread();
        }

        final Intent intent = CourseActivity.newIntent(this, courseId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void showLoginDialog() {
        // Create and show the login dialog only if it is not currently displayed
        final FragmentManager fm = this.getSupportFragmentManager();
        if (fm.findFragmentByTag("loginDialog") == null) {
            // track a login view
            mGoogleAnalytics.sendEvent("Login");

            LoginDialogFragment.builder().clientId(THEKEY_CLIENTID).build()
                    .show(fm.beginTransaction().addToBackStack("loginDialog"), "loginDialog");
        }
    }

    private void updateUser(final String guid) {
        // update the current guid
        final String old = mGuid;
        mGuid = guid != null ? guid : GUID_GUEST;

        // did the current user change?
        if (!mGuid.equals(old)) {
            onChangingUser(old, mGuid);
        }
    }
}
