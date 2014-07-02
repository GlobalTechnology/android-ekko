package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.ARG_LAYOUT;
import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.support.v4.widget.MultiDrawerListener;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestContentPagerAdapter;
import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.GoogleAnalyticsManager;
import org.ekkoproject.android.player.widget.FocusingDrawerListener;

import java.util.List;

public class CourseFragment extends AbstractManifestAwareFragment implements LessonFragment.Listener,
        AbstractContentFragment.OnNavigateListener, CourseContentDrawerFragment.Listener,
        ViewPager.OnPageChangeListener {
    private static final String STATE_CONTENT_PAGER = CourseFragment.class.getName() + ".STATE_CONTENT_PAGER";

    private int layout = R.layout.fragment_course;
    private String contentId = null;

    private DrawerLayout drawerLayout = null;
    private boolean contentPagerInitialized = true;
    private ViewPager contentPager = null;
    private Parcelable contentPagerState = null;

    private GoogleAnalyticsManager mGoogleAnalytics;

    public static CourseFragment newInstance(final int layout, final String guid, final long courseId) {
        final CourseFragment fragment = new CourseFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId);
        args.putInt(ARG_LAYOUT, layout);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        mGoogleAnalytics = GoogleAnalyticsManager.getInstance(getActivity());
        this.setHasOptionsMenu(true);

        // load arguments
        final Bundle args = getArguments();
        this.layout = args.getInt(ARG_LAYOUT, R.layout.fragment_course);

        // restore saved state
        if (savedState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                this.contentId = savedState.getString(ARG_CONTENTID, this.contentId);
            } else {
                final String contentId = savedState.getString(ARG_CONTENTID);
                if (contentId != null) {
                    this.contentId = contentId;
                }
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedState) {
        return inflater.inflate(this.layout, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        this.findViews();
        this.setupContentPager(savedState);
        this.setupNavigationDrawer();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // update the title/icon
        // XXX: this is a hack, but the best way of dynamically managing it I could think of with current API's
        final Manifest manifest = this.getManifest();
        if (manifest != null) {
            final FragmentActivity activity = this.getActivity();
            if(activity instanceof ActionBarActivity) {
                ((ActionBarActivity) activity).getSupportActionBar().setTitle(manifest.getTitle());
            }
        }

        // add menu items
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_course, menu);

        // hide root menu items
        menu.setGroupVisible(R.id.rootMenuItems, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleAnalytics.sendEvent("Course", getCourseId());
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.lessons:
            if (this.drawerLayout != null) {
                if (this.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    this.drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    this.drawerLayout.openDrawer(GravityCompat.END);
                }
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.contentPager);

        if (manifest != null) {
            // set the contentId if it is not currently set
            if (this.contentId == null) {
                final List<CourseContent> content = manifest.getContent();
                if (content.size() > 0) {
                    this.onSelectContent(content.get(0).getId());
                }
            }

            // restore pager state if we haven't initialized it yet
            if (!this.contentPagerInitialized) {
                this.contentPager.onRestoreInstanceState(this.contentPagerState);
                this.contentPagerInitialized = true;
                this.contentPagerState = null;
            }

            // update the action bar
            this.getActivity().supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onNavigateFirst() {
        if (this.contentPager != null) {
            this.contentPager.setCurrentItem(0, false);
        }
    }

    @Override
    public void onNavigatePrevious() {
        if (this.contentPager != null) {
            final int index = this.contentPager.getCurrentItem() - 1;
            this.contentPager.setCurrentItem(index, false);
        }
    }

    @Override
    public void onNavigateNext() {
        if (this.contentPager != null) {
            final int index = this.contentPager.getCurrentItem() + 1;
            this.contentPager.setCurrentItem(index, false);
        }
    }

    @Override
    public void onSelectContent(final String contentId) {
        // only update if contentId changed
        if (!((contentId == null && this.contentId == null) || (contentId != null && contentId.equals(this.contentId)))) {
            this.contentId = contentId;
            this.updateContentPager();
            this.updateNavigationDrawer();
        }
        this.closeNavigationDrawer();
    }

    @Override
    public void onPageSelected(final int position) {
        final Manifest manifest = this.getManifest();
        if (manifest != null) {
            final List<CourseContent> content = manifest.getContent();
            if (content.size() > position) {
                this.onSelectContent(content.get(position).getId());
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onDestroyView() {
        // save pager state
        if (this.contentPager != null && this.contentPagerInitialized) {
            this.contentPagerState = this.contentPager.onSaveInstanceState();
        }

        this.clearViews();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_CONTENTID, this.contentId);
        if (this.contentPager != null && this.contentPagerInitialized) {
            outState.putParcelable(STATE_CONTENT_PAGER, this.contentPager.onSaveInstanceState());
        } else {
            outState.putParcelable(STATE_CONTENT_PAGER, this.contentPagerState);
        }
    }

    /** END lifecycle */

    private void findViews() {
        this.contentPager = findView(ViewPager.class, R.id.content);
        this.drawerLayout = findView(DrawerLayout.class, R.id.drawer_layout);
    }

    private void setupContentPager(final Bundle savedState) {
        if (this.contentPager != null) {
            this.contentPager.setAdapter(new ManifestContentPagerAdapter(getChildFragmentManager(), getGuid()));
            this.contentPager.setOnPageChangeListener(this);

            // restore the content pager state
            if (savedState != null) {
                this.contentPagerInitialized = false;
                this.contentPagerState = savedState.getParcelable(STATE_CONTENT_PAGER);
            }
        }
    }

    private void clearViews() {
        this.contentPager = null;
        this.drawerLayout = null;
    }

    private void setupNavigationDrawer() {
        if (this.drawerLayout != null) {
            this.drawerLayout.setDrawerListener(
                    new MultiDrawerListener(new FocusingDrawerListener(), new DrawerLayout.SimpleDrawerListener() {
                        @Override
                        public void onDrawerOpened(final View drawerView) {
                            mGoogleAnalytics.sendEvent("Course Navigation", getCourseId());
                        }
                    })
            );
        }
    }

    private void updateNavigationDrawer() {
        if (this.drawerLayout != null) {
            final CourseContentDrawerFragment fragment =
                    CourseContentDrawerFragment.newInstance(getGuid(), getCourseId(), this.contentId);
            getChildFragmentManager().beginTransaction().replace(R.id.frame_drawer_right, fragment).commit();
        }
    }

    private void closeNavigationDrawer() {
        if (this.drawerLayout != null) {
            this.drawerLayout.closeDrawers();
        }
    }

    private void updateContentPager() {
        if (this.contentPager != null) {
            final Manifest manifest = this.getManifest();
            int index = 0;
            if (manifest != null) {
                index = manifest.findContent(this.contentId);
            }
            this.contentPager.setCurrentItem(index, false);
        }
    }

    /** ignored callbacks */

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
}
