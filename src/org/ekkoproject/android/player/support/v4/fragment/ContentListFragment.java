package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.appdev.entity.Lesson;
import org.appdev.entity.Quiz;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.adapter.ManifestContentAdapter;
import org.ekkoproject.android.player.services.EkkoBroadcastReceiver;
import org.ekkoproject.android.player.services.ManifestManager;
import org.ekkoproject.android.player.tasks.UpdateManifestAdaptersAsyncTask;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ContentListFragment extends ListFragment implements EkkoBroadcastReceiver.ManifestUpdateListener {
    private final static String ARG_LAYOUT = ContentListFragment.class.getName() + ".ARG_LAYOUT";
    private final static String ARG_COURSEID = ContentListFragment.class.getName() + ".ARG_COURSEID";

    private int layout = DEFAULT_LAYOUT;
    private int lessonLayout = R.layout.list_item_lesson_menu;
    private int quizLayout = R.layout.list_item_quiz_menu;

    private EkkoBroadcastReceiver broadcastReceiver = null;

    public static ContentListFragment newInstance(final int layout, final long courseId) {
        final ContentListFragment fragment = new ContentListFragment();

        // handle arguments
        final Bundle args = new Bundle();
        args.putLong(ARG_COURSEID, courseId);
        args.putInt(ARG_LAYOUT, layout);
        fragment.setArguments(args);

        return fragment;
    }

    private long getCourseId() {
        return getArguments().getLong(ARG_COURSEID, INVALID_COURSE);
    }

    /** BEGIN Lifecycle */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configLayout();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        switch (this.layout) {
        case R.layout.fragment_content_list_menu:
            return inflater.inflate(this.layout, container, false);
        default:
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setupManifestAdapter();
        this.setupBroadcastReceiver();
    }

    @Override
    public void onManifestUpdate(final long courseId) {
        this.updateManifestAdapter();
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final Activity activity = getActivity();
        if (activity instanceof Listener) {
            final Object item = getListView().getItemAtPosition(position);
            if (item instanceof Lesson) {
                ((Listener) activity).onSelectLesson(this, getCourseId(), ((Lesson) item).getId());
            } else if (item instanceof Quiz) {
                ((Listener) activity).onSelectQuiz(this, getCourseId(), ((Quiz) item).getId());
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.cleanupBroadcastReceiver();
        this.cleanupManifestAdapter();
    }

    /** END Lifecycle */

    /**
     * configure the layout
     */
    private void configLayout() {
        // select the layout
        final int layout = getArguments().getInt(ARG_LAYOUT, DEFAULT_LAYOUT);
        switch (layout) {
        case R.layout.fragment_content_list_menu:
            this.layout = layout;
            break;
        default:
            this.layout = DEFAULT_LAYOUT;
        }

        // configure the item layout based on the main layout
        switch (this.layout) {
        case R.layout.fragment_content_list_menu:
        default:
            this.lessonLayout = R.layout.list_item_lesson_menu;
            this.quizLayout = R.layout.list_item_quiz_menu;
            break;
        }
    }

    private void setupManifestAdapter() {
        // create Adapter
        final ManifestContentAdapter adapter = new ManifestContentAdapter(getActivity());
        adapter.setLessonView(this.lessonLayout);
        adapter.setQuizView(this.quizLayout);
        this.setListAdapter(adapter);

        // trigger initial load
        this.updateManifestAdapter();
    }

    private void updateManifestAdapter() {
        final ListAdapter adapter = this.getListAdapter();
        if (adapter instanceof ManifestAdapter) {
            new UpdateManifestAdaptersAsyncTask(ManifestManager.getInstance(getActivity()), (ManifestAdapter) adapter)
                    .execute(getCourseId());
        }
    }

    private void cleanupManifestAdapter() {
        this.setListAdapter(null);
    }

    private void setupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.cleanupBroadcastReceiver();
        }

        this.broadcastReceiver = new EkkoBroadcastReceiver(this, this.getCourseId()).registerReceiver();
    }

    private void cleanupBroadcastReceiver() {
        if (this.broadcastReceiver != null) {
            this.broadcastReceiver.unregisterReceiver();
            this.broadcastReceiver = null;
        }
    }

    public interface Listener {
        void onSelectLesson(Fragment fragment, long courseId, String lessonId);

        void onSelectQuiz(Fragment fragment, long courseId, String quizId);
    }
}
