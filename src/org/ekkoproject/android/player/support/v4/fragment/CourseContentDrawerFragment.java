package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import java.util.Set;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestContentAdapter;
import org.ekkoproject.android.player.adapter.ManifestLessonMediaAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.CourseManager;
import org.ekkoproject.android.player.services.ProgressManager;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class CourseContentDrawerFragment extends AbstractManifestAndProgressAwareFragment implements
        AdapterView.OnItemClickListener {
    private String contentId;

    private GridView mediaView = null;
    private ProgressBar progressBar = null;
    private ListView contentListView = null;

    public static CourseContentDrawerFragment newInstance() {
        return new CourseContentDrawerFragment();
    }

    public static CourseContentDrawerFragment newInstance(final long courseId, final String contentId) {
        final CourseContentDrawerFragment fragment = new CourseContentDrawerFragment();

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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_content_drawer, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupMediaAdapter();
        this.setupContentListAdapter();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateManifestAdapters(manifest, this.mediaView, this.contentListView);
        this.updateProgressBar(manifest, this.getProgress());
    }

    @Override
    protected void onProgressUpdate(final Set<String> progress) {
        super.onProgressUpdate(progress);
        this.updateProgressBar(this.getManifest(), progress);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Object listener = this.getPotentialListener();
        if (parent != null && listener instanceof Listener) {
            switch (parent.getId()) {
            case R.id.mediaList:
                // TODO
                break;
            case R.id.contentList:
                ((Listener) listener).onSelectContent(CourseManager.convertId(this.getCourseId(), id));
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /** END lifecycle */

    private void findViews() {
        this.mediaView = findView(GridView.class, R.id.mediaList);
        this.contentListView = findView(ListView.class, R.id.contentList);
        this.progressBar = findView(ProgressBar.class, R.id.progress);
    }

    private void clearViews() {
        this.mediaView = null;
        this.contentListView = null;
        this.progressBar = null;
    }

    private void setupContentListAdapter() {
        if (this.contentListView != null) {
            // attach adapter
            final ManifestContentAdapter adapter = new ManifestContentAdapter(getActivity());
            adapter.setLessonView(R.layout.list_item_lesson_menu);
            adapter.setQuizView(R.layout.list_item_quiz_menu);
            this.contentListView.setAdapter(adapter);

            // attach select item listener
            this.contentListView.setOnItemClickListener(this);
        }
    }

    private void setupMediaAdapter() {
        if (this.mediaView != null) {
            // attach media adapter
            final ManifestLessonMediaAdapter adapter = new ManifestLessonMediaAdapter(getActivity(), contentId);
            adapter.setVideoView(R.layout.media_list_item_image_thumbnail);
            adapter.setAudioView(R.layout.media_list_item_image_thumbnail);
            adapter.setImageView(R.layout.media_list_item_image_thumbnail);
            this.mediaView.setAdapter(adapter);

            // attach select item listener
            this.contentListView.setOnItemClickListener(this);
        }
    }

    private void updateProgressBar(final Manifest manifest, final Set<String> progress) {
        if (this.progressBar != null) {
            // retrieve the progress
            final Pair<Integer, Integer> rawProgress = ProgressManager.getCourseProgress(manifest, progress);

            // update the progress bar
            this.progressBar.setMax(rawProgress.second);
            this.progressBar.setProgress(rawProgress.first);
        }
    }

    public interface Listener {
        public void onSelectContent(final String contentId);
    }
}
