package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import java.util.List;

import org.appdev.app.AppContext;
import org.appdev.entity.CourseContent;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestContentAdapter;
import org.ekkoproject.android.player.adapter.ManifestLessonMediaAdapter;
import org.ekkoproject.android.player.model.Manifest;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class CourseContentSlidingMenu extends AbstractManifestAwareFragment {
    private String contentId;

    private GridView mediaView = null;
    private ListView contentListView = null;

    public static CourseContentSlidingMenu newInstance() {
        return new CourseContentSlidingMenu();
    }

    public static CourseContentSlidingMenu newInstance(final long courseId, final String contentId) {
        final CourseContentSlidingMenu fragment = new CourseContentSlidingMenu();

        // handle arguments
        final Bundle args = buildArgs(courseId);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.contentId = args.getString(ARG_CONTENTID, null);
        } else {
            this.contentId = args.getString(ARG_CONTENTID);
        }

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_right_menu, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupMediaAdapter();
        this.setupContentListAdapter();
    }

    @Override
    protected void onManifestUpdate(Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.mediaView, this.contentListView);
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
        outState.putString(ARG_CONTENTID, this.contentId);
    }

    /** END lifecycle */

    private void findViews() {
        this.mediaView = findView(GridView.class, R.id.mediaList);
        this.contentListView = findView(ListView.class, R.id.contentList);
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
        ProgressBar courseProgress = (ProgressBar) getView().findViewById(R.id.lesson_progressbar);
        final List<CourseContent> content = AppContext.getInstance().getCurCourse().getContent();

        // todo: need to find a better way to decide how a user finish a lesson.

        if (content.size() > 0 && courseProgress != null) {
            int progress = AppContext.getCourseProgress(AppContext.getInstance().getCurCourse());

            courseProgress.setProgress(progress <= courseProgress.getMax() ? progress : courseProgress.getMax());
        }
    }

}
