package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.util.ViewUtils.getBitmapFromView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractListFragment;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.support.v4.adapter.CourseListCursorAdapter;
import org.ekkoproject.android.player.support.v4.content.CourseListCursorLoader;
import org.ekkoproject.android.player.sync.EkkoSyncService;

public class CourseListFragment extends AbstractListFragment {
    private static final String ARG_ANIMATIONHACK = CourseListFragment.class.getName() + ".ARG_ANIMATIONHACK";
    private static final String ARG_LAYOUT = CourseListFragment.class.getName() + ".ARG_LAYOUT";
    private static final String ARG_GUID = CourseListFragment.class.getName() + ".ARG_GUID";
    private static final String ARG_VIEWSTATE = CourseListFragment.class.getName() + ".ARG_VIEWSTATE";
    private static final String ARG_LISTVIEWSTATE = CourseListFragment.class.getName() + ".ARG_LISTVIEWSTATE";

    private static final int LOADER_COURSES = 1;

    private String guid = GUID_GUEST;
    private int layout = DEFAULT_LAYOUT;
    private int itemLayout = R.layout.course_list_item_simple;
    private boolean animationHack = false;
    private Bitmap animationHackImage = null;
    private boolean needsRestore = false;
    private Bundle viewState = new Bundle();

    private ListView listView = null;

    public static CourseListFragment newInstance(final String guid) {
        return newInstance(guid, DEFAULT_LAYOUT);
    }

    public static CourseListFragment newInstance(final String guid, final int layout) {
        return newInstance(guid, layout, false);
    }

    public static CourseListFragment newInstance(final String guid, final int layout, final boolean animationHack) {
        final CourseListFragment fragment = new CourseListFragment();

        // handle arguments
        final Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layout);
        args.putString(ARG_GUID, guid);
        args.putBoolean(ARG_ANIMATIONHACK, animationHack);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN Lifecycle */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        this.setHasOptionsMenu(true);

        // process arguments
        this.configLayout();
        final Bundle args = this.getArguments();
        if (args != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                this.guid = args.getString(ARG_GUID, this.guid);
            } else {
                final String guid = args.getString(ARG_GUID);
                if (guid != null) {
                    this.guid = guid;
                }
            }
            this.animationHack = args.getBoolean(ARG_ANIMATIONHACK, this.animationHack);
        }

        if (savedState != null) {
            if (savedState.containsKey(ARG_VIEWSTATE)) {
                this.viewState = savedState.getBundle(ARG_VIEWSTATE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_course_list, menu);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        switch (this.layout) {
        case R.layout.fragment_course_list_main:
        case R.layout.course_list_menu:
            return inflater.inflate(this.layout, container, false);
        default:
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.findViews();
        this.setupListAdapter();
        this.startLoaders();
        this.needsRestore = true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            EkkoSyncService.syncCourses(getActivity());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        // find the parent object (can be a fragment or activity)
        Object parent = getParentFragment();
        if (parent == null) {
            parent = getActivity();
        }
        if (parent instanceof Listener) {
            ((Listener) parent).onSelectCourse(this, id);
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

        super.onDestroyView();
        this.saveViewState();
        this.cleanupListAdapter();
        this.clearViews();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        this.saveViewState();
        outState.putBundle(ARG_VIEWSTATE, this.viewState);
    }

    /* END Lifecycle */

    /**
     * configure the layout
     */
    private void configLayout() {
        // select the layout
        final Bundle args = this.getArguments();
        final int layout = args != null ? args.getInt(ARG_LAYOUT, DEFAULT_LAYOUT) : DEFAULT_LAYOUT;
        switch (layout) {
        case R.layout.fragment_course_list_main:
        case R.layout.course_list_menu:
            this.layout = layout;
            break;
        default:
            this.layout = DEFAULT_LAYOUT;
        }

        // configure the item layout based on the main layout
        switch (this.layout) {
        case R.layout.fragment_course_list_main:
            this.itemLayout = R.layout.list_item_course_card;
            break;
        case R.layout.course_list_menu:
        default:
            this.itemLayout = R.layout.course_list_item_simple;
            break;
        }
    }

    private void setupListAdapter() {
        this.setListAdapter(new CourseListCursorAdapter(getActivity(), this.itemLayout));
    }

    private void cleanupListAdapter() {
        this.setListAdapter(null);
    }

    private void startLoaders() {
        this.getLoaderManager().initLoader(LOADER_COURSES, null, new CursorLoaderCallbacks()).startLoading();
    }

    private void findViews() {
        this.listView = this.getListView();
    }

    private void clearViews() {
        this.listView = null;
    }

    private void saveViewState() {
        if (this.listView != null) {
            this.viewState.putParcelable(ARG_LISTVIEWSTATE, this.listView.onSaveInstanceState());
        }
    }

    private void restoreViewState() {
        if (this.listView != null) {
            final Parcelable state = this.viewState.getParcelable(ARG_LISTVIEWSTATE);
            if (state != null) {
                this.listView.onRestoreInstanceState(state);
            }
        }
    }

    private class CursorLoaderCallbacks extends SimpleLoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
            switch (id) {
                case LOADER_COURSES:
                    return new CourseListCursorLoader(getActivity(), guid);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(final Loader loader, final Cursor cursor) {
            switch (loader.getId()) {
                case LOADER_COURSES:
                    swapCursor(cursor);
                    if (needsRestore) {
                        restoreViewState();
                        needsRestore = false;
                    }
                    break;
            }
        }
    }

    public interface Listener {
        void onSelectCourse(CourseListFragment fragment, long courseId);
    }
}
