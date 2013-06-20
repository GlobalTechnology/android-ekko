package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.ARG_LAYOUT;
import static org.ekkoproject.android.player.util.ViewUtils.getBitmapFromView;

import java.util.List;

import org.appdev.entity.CourseContent;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestContentPagerAdapter;
import org.ekkoproject.android.player.model.Manifest;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class CourseFragment extends AbstractManifestAwareFragment implements LessonFragment.Listener,
        ViewPager.OnPageChangeListener {
    private static final String ARG_ANIMATIONHACK = CourseFragment.class.getName() + ".ARG_ANIMATIONHACK";

    private int layout = R.layout.fragment_course;
    private boolean animationHack = false;
    private Bitmap animationHackImage = null;
    private String contentId = null;

    private ViewPager contentPager = null;
    private DrawerLayout drawerLayout = null;
    private SlidingMenu slidingMenu = null;

    public static CourseFragment newInstance(final long courseId) {
        return newInstance(courseId, false);
    }

    public static CourseFragment newInstance(final long courseId, final boolean animationHack) {
        return newInstance(R.layout.fragment_course, courseId, animationHack);
    }

    public static CourseFragment newInstance(final int layout, final long courseId, final boolean animationHack) {
        final CourseFragment fragment = new CourseFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putBoolean(ARG_ANIMATIONHACK, animationHack);
        args.putInt(ARG_LAYOUT, layout);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        this.setHasOptionsMenu(true);

        // load arguments
        final Bundle args = getArguments();
        this.layout = args.getInt(ARG_LAYOUT, R.layout.fragment_course);
        this.animationHack = getArguments().getBoolean(ARG_ANIMATIONHACK, this.animationHack);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(this.layout, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupContentPager();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_course, menu);
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
            if (this.slidingMenu != null) {
                this.slidingMenu.toggle();
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
        }
    }

    @Override
    public void onNextContent(final String contentId) {
        final Manifest manifest = this.getManifest();
        if (manifest != null) {
            final int index = manifest.findContent(contentId) + 1;
            final List<CourseContent> content = manifest.getContent();
            if (content.size() > index) {
                this.onSelectContent(content.get(index).getId());
            }
        }
    }

    @Override
    public void onPreviousContent(final String contentId) {
        final Manifest manifest = this.getManifest();
        if (manifest != null) {
            final int index = manifest.findContent(contentId) - 1;
            final List<CourseContent> content = manifest.getContent();
            if (content.size() > 0) {
                this.onSelectContent(content.get(index < 0 ? 0 : index).getId());
            }
        }
    }

    public void onSelectContent(final String contentId) {
        // only update if contentId changed
        if (!((contentId == null && this.contentId == null) || (contentId != null && contentId.equals(this.contentId)))) {
            this.contentId = contentId;
            this.updateContentPager();
            this.updateNavigationDrawer();
        }
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
    public void onPause() {
        if (this.animationHack) {
            final View view = getView();
            if (view != null) {
                this.animationHackImage = getBitmapFromView(view);
            }
        }
        super.onPause();
    }

    @Override
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onDestroyView() {
        if (this.animationHack && this.animationHackImage != null) {
            final View view = getView();
            if (view != null) {
                final BitmapDrawable background = new BitmapDrawable(getResources(), this.animationHackImage);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(background);
                } else {
                    view.setBackgroundDrawable(background);
                }
            }
        }
        this.animationHackImage = null;

        this.clearViews();
        super.onDestroyView();
    }

    /** END lifecycle */

    private void findViews() {
        this.contentPager = findView(ViewPager.class, R.id.content);
        this.drawerLayout = findView(DrawerLayout.class, R.id.drawer_layout);
        this.slidingMenu = findView(SlidingMenu.class, R.id.slidingmenu);
    }

    private void clearViews() {
        this.contentPager = null;
        this.drawerLayout = null;
        this.slidingMenu = null;
    }

    private void updateNavigationDrawer() {
        if (this.slidingMenu != null || this.drawerLayout != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_drawer_right,
                            CourseContentSlidingMenu.newInstance(this.getCourseId(), contentId)).commit();
        }
    }

    private void setupContentPager() {
        if (this.contentPager != null) {
            this.contentPager.setAdapter(new ManifestContentPagerAdapter(getChildFragmentManager()));
            this.contentPager.setOnPageChangeListener(this);
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
