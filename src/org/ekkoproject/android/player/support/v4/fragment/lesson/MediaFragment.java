package org.ekkoproject.android.player.support.v4.fragment.lesson;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import org.appdev.entity.Lesson;
import org.appdev.entity.Media;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.support.v4.fragment.AbstractManifestAwareFragment;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MediaFragment extends AbstractManifestAwareFragment {
    private static final String ARG_MEDIAID = "org.ekkoproject.android.player.support.v4.fragment.lesson.MediaFragment.ARG_MEDIAID";

    private String lessonId = null;
    private String mediaId = null;

    private ResourceManager resourceManager = null;

    private ImageView thumbnail = null;

    public static MediaFragment newInstance(final long courseId, final String lessonId, final String mediaId) {
        final MediaFragment fragment = new MediaFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putString(ARG_CONTENTID, lessonId);
        args.putString(ARG_MEDIAID, mediaId);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.resourceManager = ResourceManager.getInstance(activity);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.lessonId = args.getString(ARG_CONTENTID, null);
            this.mediaId = args.getString(ARG_MEDIAID, null);
        } else {
            this.lessonId = args.getString(ARG_CONTENTID);
            this.mediaId = args.getString(ARG_MEDIAID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson_media, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateMediaThumbnail(manifest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /** END lifecycle */

    private void findViews() {
        this.thumbnail = findView(ImageView.class, R.id.thumbnail);
    }

    private void clearViews() {
        this.thumbnail = null;
    }

    private void updateMediaThumbnail(final Manifest manifest) {
        if (this.thumbnail != null) {
            // find the resource id for the thumbnail
            String resourceId = null;
            if (manifest != null) {
                final Lesson lesson = manifest.getLesson(this.lessonId);
                if (lesson != null) {
                    final Media media = lesson.getMedia(this.mediaId);
                    if (media != null) {
                        resourceId = media.getMediaThumbnailID();

                        // use the actual image if this is an image resource and
                        // there isn't a thumbnail
                        if (resourceId == null && media.isImage()) {
                            resourceId = media.getMediaResourceID();
                        }
                    }
                }
            }

            // update the view
            if (this.thumbnail instanceof ResourceImageView) {
                ((ResourceImageView) this.thumbnail).setResource(this.getCourseId(), resourceId);
            } else {
                this.thumbnail.setImageDrawable(null);
                new LoadImageResourceAsyncTask(this.resourceManager, this.thumbnail, this.getCourseId(), resourceId)
                        .execute();
            }
        }
    }
}