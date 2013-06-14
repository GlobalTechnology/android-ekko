package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;
import static org.ekkoproject.android.player.fragment.Constants.ARG_COURSEID;

import java.util.ArrayList;
import java.util.List;

import org.appdev.app.AppContext;
import org.appdev.entity.CourseContent;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestAdapter;
import org.ekkoproject.android.player.adapter.ManifestContentAdapter;
import org.ekkoproject.android.player.adapter.ManifestLessonMediaAdapter;
import org.ekkoproject.android.player.services.ManifestManager;
import org.ekkoproject.android.player.tasks.UpdateManifestAdaptersAsyncTask;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class CourseContentSlidingMenu extends Fragment {
    private GridView mediaView = null;
    private ListView contentListView = null;

    private long courseId;
    private String contentId;

    public static CourseContentSlidingMenu newInstance() {
        return new CourseContentSlidingMenu();
    }

    public static CourseContentSlidingMenu newInstance(final long courseId, final String contentId) {
        final CourseContentSlidingMenu fragment = new CourseContentSlidingMenu();

        // handle arguments
        final Bundle args = new Bundle();
        args.putLong(ARG_COURSEID, courseId);
        args.putString(ARG_CONTENTID, contentId);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = getArguments();
        this.courseId = args.getLong(ARG_COURSEID, INVALID_COURSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.contentId = args.getString(ARG_CONTENTID, null);
        } else {
            this.contentId = args.getString(ARG_CONTENTID);
        }

        // restore saved state
        if (savedState != null) {
            this.courseId = savedState.getLong(ARG_COURSEID, this.courseId);
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_right_menu, null);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        // this.attachContentList();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setupMediaAdapter();
        this.setupContentListAdapter();
        this.updateManifestAdapters(this.mediaView, this.contentListView);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.cleanupMediaAdapter();
        this.cleanupContentListAdapter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ARG_COURSEID, this.courseId);
        outState.putString(ARG_CONTENTID, this.contentId);
    }

    /** END lifecycle */

    private void findViews() {
        final View mediaView = getActivity().findViewById(R.id.mediaList);
        if (mediaView instanceof GridView) {
            this.mediaView = (GridView) mediaView;
        }

        final View contentListView = getActivity().findViewById(R.id.contentList);
        if (contentListView instanceof ListView) {
            this.contentListView = (ListView) contentListView;
        }
    }

    private void clearViews() {
        this.mediaView = null;
        this.contentListView = null;
    }

    private void setupContentListAdapter() {
        if (this.contentListView != null) {
            // create Adapter
            final ManifestContentAdapter adapter = new ManifestContentAdapter(getActivity());
            adapter.setLessonView(R.layout.content_list_item_lesson_simple);
            adapter.setQuizView(R.layout.content_list_item_quiz_simple);

            // attach adapter
            this.contentListView.setAdapter(adapter);
        }
    }

    private void setupMediaAdapter() {
        if (this.mediaView != null) {
            // create Adapter
            final ManifestLessonMediaAdapter adapter = new ManifestLessonMediaAdapter(getActivity(), contentId);
            adapter.setVideoView(R.layout.media_list_item_image_thumbnail);
            adapter.setAudioView(R.layout.media_list_item_image_thumbnail);
            adapter.setImageView(R.layout.media_list_item_image_thumbnail);

            // attach media adapter
            this.mediaView.setAdapter(adapter);
        }
    }

    private void updateManifestAdapters(final AbsListView... views) {
        final List<ManifestAdapter<?>> adapters = new ArrayList<ManifestAdapter<?>>(2);
        for (final AbsListView view : views) {
            if (view != null) {
                final ListAdapter adapter = view.getAdapter();
                if (adapter instanceof ManifestAdapter) {
                    adapters.add((ManifestAdapter<?>) adapter);
                }
            }
        }

        if (adapters.size() > 0) {
            new UpdateManifestAdaptersAsyncTask(ManifestManager.getInstance(getActivity()),
                    adapters.toArray(new ManifestAdapter<?>[adapters.size()])).execute(courseId);
        }
    }

    private void cleanupContentListAdapter() {
        if (this.contentListView != null) {
            this.contentListView.setAdapter(null);
        }
    }

    private void cleanupMediaAdapter() {
        if (this.mediaView != null) {
            this.mediaView.setAdapter(null);
        }
    }

    @Deprecated
    public void updateProgressBar() {
        ProgressBar courseProgress = (ProgressBar) getActivity().findViewById(R.id.lesson_progressbar);
        final List<CourseContent> content = AppContext.getInstance().getCurCourse().getContent();

        // todo: need to find a better way to decide how a user finish a lesson.

        if (content.size() > 0 && courseProgress != null) {
            int progress = AppContext.getCourseProgress(AppContext.getInstance().getCurCourse());

            courseProgress.setProgress(progress <= courseProgress.getMax() ? progress : courseProgress.getMax());
        }
    }

}
