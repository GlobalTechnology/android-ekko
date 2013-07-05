package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import static org.ekkoproject.android.player.util.ViewUtils.getBitmapFromView;

import java.lang.ref.WeakReference;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.services.EkkoBroadcastReceiver;
import org.ekkoproject.android.player.services.ProgressManager;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.sync.EkkoSyncService;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CourseListFragment extends SherlockListFragment implements EkkoBroadcastReceiver.CourseUpdateListener {
    private static final String ARG_ANIMATIONHACK = CourseListFragment.class.getName() + ".ARG_ANIMATIONHACK";
    private static final String ARG_LAYOUT = CourseListFragment.class.getName() + ".ARG_LAYOUT";
    private static final String ARG_VIEWSTATE = CourseListFragment.class.getName() + ".ARG_VIEWSTATE";
    private static final String ARG_LISTVIEWSTATE = CourseListFragment.class.getName() + ".ARG_LISTVIEWSTATE";

    private int layout = DEFAULT_LAYOUT;
    private int itemLayout = R.layout.course_list_item_simple;
    private boolean animationHack = false;
    private Bitmap animationHackImage = null;
    private boolean needsRestore = false;
    private Bundle viewState = new Bundle();

    private ProgressManager progressManager = null;
    private ResourceManager resourceManager = null;
    private EkkoDao dao = null;
    private EkkoBroadcastReceiver broadcastReceiver = null;

    private ListView listView = null;

    public static CourseListFragment newInstance() {
        return newInstance(DEFAULT_LAYOUT);
    }

    public static CourseListFragment newInstance(final int layout) {
        return newInstance(layout, false);
    }

    public static CourseListFragment newInstance(final int layout, final boolean animationHack) {
        final CourseListFragment fragment = new CourseListFragment();

        // handle arguments
        final Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layout);
        args.putBoolean(ARG_ANIMATIONHACK, animationHack);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN Lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.dao = EkkoDao.getInstance(activity);
        this.progressManager = ProgressManager.getInstance(activity);
        this.resourceManager = ResourceManager.getInstance(activity);
    }

    @Override
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        this.setHasOptionsMenu(true);
        this.configLayout();
        this.animationHack = getArguments().getBoolean(ARG_ANIMATIONHACK, this.animationHack);

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
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.needsRestore = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setupListAdapter();
        this.setupBroadcastReceiver();
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
    public void onCourseUpdate() {
        this.updateCoursesList();
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
    public void onStop() {
        super.onStop();
        this.saveViewState();
        this.cleanupBroadcastReceiver();
        this.cleanupListAdapter();
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
        this.clearViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // XXX: this is disabled because of a potential race between the close
        // and the update task
        // this.dao.close();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        this.saveViewState();
        outState.putBundle(ARG_VIEWSTATE, this.viewState);
    }

    /** END Lifecycle */

    /**
     * configure the layout
     */
    private void configLayout() {
        // select the layout
        final int layout = getArguments().getInt(ARG_LAYOUT, DEFAULT_LAYOUT);
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

    private static final String[] FROM = new String[] { Contract.Course.COLUMN_NAME_TITLE,
            Contract.Course.COLUMN_NAME_BANNER_RESOURCE, Contract.Course._ID };
    private static final int[] TO = new int[] { R.id.title, R.id.banner, R.id.progress };

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupListAdapter() {
        // create CursorAdapter
        final SimpleCursorAdapter adapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            adapter = new SimpleCursorAdapter(getActivity(), this.itemLayout, (Cursor) null, FROM, TO, 0);
        } else {
            adapter = new SimpleCursorAdapter(getActivity(), this.itemLayout, (Cursor) null, FROM, TO);
        }
        adapter.setViewBinder(new CourseViewBinder());
        this.setListAdapter(adapter);

        // trigger an initial update
        this.updateCoursesList();
    }

    private void setupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.cleanupBroadcastReceiver();
        }

        this.broadcastReceiver = new EkkoBroadcastReceiver(this).registerReceiver();
    }

    private void cleanupListAdapter() {
        this.setListAdapter(null);
    }

    private void cleanupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.broadcastReceiver.unregisterReceiver();
            this.broadcastReceiver = null;
        }
    }

    private void updateCoursesList() {
        new UpdateCursorAsyncTask().execute();
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

    private class UpdateCursorAsyncTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(final Void... params) {
            return CourseListFragment.this.dao.getCoursesCursor(Contract.Course.COLUMN_NAME_ACCESSIBLE + " = ?",
                    new String[] { "1" });
        }

        @Override
        protected void onPostExecute(final Cursor c) {
            super.onPostExecute(c);

            // switch to the new db cursor
            final ListAdapter adapter = CourseListFragment.this.getListAdapter();
            if (adapter instanceof CursorAdapter) {
                ((CursorAdapter) adapter).changeCursor(c);
                if (CourseListFragment.this.needsRestore) {
                    CourseListFragment.this.restoreViewState();
                    CourseListFragment.this.needsRestore = false;
                }
            }
        }
    }

    private class UpdateProgressBarAsyncTask extends AsyncTask<Long, Void, Pair<Integer, Integer>> {
        private final WeakReference<ProgressBar> progressBar;

        protected UpdateProgressBarAsyncTask(final ProgressBar progressBar) {
            progressBar.setTag(R.id.progress_bar_update_task, new WeakReference<AsyncTask<?, ?, ?>>(this));
            this.progressBar = new WeakReference<ProgressBar>(progressBar);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public AsyncTask<Long, Void, Pair<Integer, Integer>> execute(final long courseId) {
            final Long[] params = new Long[] { courseId };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
            } else {
                return this.execute(params);
            }
        }

        @Override
        protected Pair<Integer, Integer> doInBackground(final Long... params) {
            if (params.length > 0 && this.checkProgressBar()) {
                return CourseListFragment.this.progressManager.getCourseProgress(params[0]);
            }

            // default to no progress
            return Pair.create(0, 1);
        }

        @Override
        protected void onPostExecute(final Pair<Integer, Integer> progress) {
            super.onPostExecute(progress);
            if (this.checkProgressBar()) {
                final ProgressBar progressBar = this.progressBar.get();
                if (progressBar != null) {
                    progressBar.setMax(progress.second);
                    progressBar.setProgress(progress.first);
                }
            }
        }

        private boolean checkProgressBar() {
            final ProgressBar progressBar = this.progressBar.get();
            if (progressBar != null) {
                final Object ref = progressBar.getTag(R.id.progress_bar_update_task);
                if (ref instanceof WeakReference) {
                    final Object task = ((WeakReference<?>) ref).get();
                    return this == task;
                }
            }
            return false;
        }
    }

    private class CourseViewBinder implements ViewBinder {
        @Override
        public boolean setViewValue(final View view, final Cursor c, final int columnIndex) {
            switch (view.getId()) {
            case R.id.banner:
                if (view instanceof ResourceImageView) {
                    ((ResourceImageView) view).setResource(
                            c.getLong(c.getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)),
                            c.getString(columnIndex));
                } else if (view instanceof ImageView) {
                    ((ImageView) view).setImageDrawable(null);
                    new LoadImageResourceAsyncTask(CourseListFragment.this.resourceManager, (ImageView) view,
                            c.getLong(c.getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)),
                            c.getString(columnIndex)).execute();
                }
                return true;
            case R.id.progress:
                if (view instanceof ProgressBar) {
                    ((ProgressBar) view).setProgress(0);
                    new UpdateProgressBarAsyncTask((ProgressBar) view).execute(c.getLong(c
                            .getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)));
                }
                return true;
            }

            return false;
        }
    }

    public interface Listener {
        void onSelectCourse(CourseListFragment fragment, long courseId);
    }
}
