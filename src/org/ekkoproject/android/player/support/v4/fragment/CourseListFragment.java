package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.sync.EkkoSyncService.ACTION_UPDATE_COURSES;

import org.appdev.R;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CourseListFragment extends ListFragment {
    public final static String ARG_LAYOUT = "org.ekkoproject.android.player.fragment.LAYOUT";

    private final static int DEFAULT_LAYOUT = 0;

    private int layout = DEFAULT_LAYOUT;
    private int itemLayout = R.layout.course_listitem;
    private String[] itemLayoutFrom = null;
    private int[] itemLayoutTo = null;

    private EkkoDao dao = null;
    private LocalBroadcastReceiver broadcastReceiver = null;

    public static CourseListFragment newInstance() {
        return newInstance(DEFAULT_LAYOUT);
    }

    public static CourseListFragment newInstance(final int layout) {
        final CourseListFragment fragment = new CourseListFragment();

        // handle arguments
        final Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layout);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN Lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dao = new EkkoDao(getActivity());
        this.configLayout();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        switch (this.layout) {
        case R.layout.course_list_menu:
            return inflater.inflate(this.layout, null);
        default:
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setupListAdapter();
        this.setupBroadcastReceiver();
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Activity activity = getActivity();
        if (activity instanceof Listener) {
            ((Listener) activity).onChangeCourse(id);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.cleanupBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dao.close();
    }

    /** END Lifecycle */

    /**
     * configure the layout
     */
    private void configLayout() {
        // select the layout
        final int layout = getArguments().getInt(ARG_LAYOUT, DEFAULT_LAYOUT);
        switch (layout) {
        case R.layout.course_list_menu:
            this.layout = layout;
            break;
        default:
            this.layout = DEFAULT_LAYOUT;
        }

        // configure the item layout based on the main layout
        switch (this.layout) {
        case R.layout.course_list_menu:
        default:
            this.itemLayout = R.layout.course_list_item_simple;
            this.itemLayoutFrom = new String[] { Contract.Course.COLUMN_NAME_TITLE };
            this.itemLayoutTo = new int[] { R.id.title };
            break;
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupListAdapter() {
        // create CursorAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.setListAdapter(new SimpleCursorAdapter(getActivity(), this.itemLayout, (Cursor) null,
                    this.itemLayoutFrom, this.itemLayoutTo, 0));
        } else {
            this.setListAdapter(new SimpleCursorAdapter(getActivity(), this.itemLayout, (Cursor) null,
                    this.itemLayoutFrom, this.itemLayoutTo));
        }

        // trigger an initial update
        this.updateNodeList();
    }

    private void setupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.cleanupBroadcastReceiver();
        }

        this.broadcastReceiver = new LocalBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.broadcastReceiver,
                this.broadcastReceiver.getIntentFilter());
    }

    private void cleanupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.broadcastReceiver);
            this.broadcastReceiver = null;
        }
    }

    private void updateNodeList() {
        new UpdateCursorAsyncTask().execute();
    }

    private class UpdateCursorAsyncTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(final Void... params) {
            synchronized (CourseListFragment.this.dao) {
                return CourseListFragment.this.dao.getCoursesCursor();
            }
        }

        @Override
        protected void onPostExecute(final Cursor c) {
            super.onPostExecute(c);

            // switch to the new db cursor
            final ListAdapter adapter = CourseListFragment.this.getListAdapter();
            if (adapter instanceof CursorAdapter) {
                ((CursorAdapter) adapter).changeCursor(c);
            }
        }
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        private IntentFilter getIntentFilter() {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_UPDATE_COURSES);
            return filter;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_COURSES.equals(action)) {
                CourseListFragment.this.updateNodeList();
            }
        }
    }

    public interface Listener {
        void onChangeCourse(long courseId);
    }
}
