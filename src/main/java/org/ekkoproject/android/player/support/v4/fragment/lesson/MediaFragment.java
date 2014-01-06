package org.ekkoproject.android.player.support.v4.fragment.lesson;

import static org.ekkoproject.android.player.model.Resource.PROVIDER_NONE;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_UNKNOWN;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_VIMEO;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_YOUTUBE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;
import static org.ekkoproject.android.player.util.ResourceUtils.providerIntent;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.activity.MediaImageActivity;
import org.ekkoproject.android.player.activity.MediaVideoActivity;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Media;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.support.v4.fragment.AbstractManifestAwareFragment;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;

public class MediaFragment extends AbstractManifestAwareFragment implements View.OnClickListener {
    private static final String ARG_MEDIAID = MediaFragment.class.getName() + ".ARG_MEDIAID";

    private String lessonId = null;
    private String mediaId = null;

    private ResourceManager resourceManager = null;
    private Media media = null;

    private View openButton = null;
    private ImageView thumbnail = null;

    public static MediaFragment newInstance(final String guid, final long courseId, final String lessonId,
                                            final String mediaId) {
        final MediaFragment fragment = new MediaFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId);
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
        this.setupOpenButton();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        Media media = null;
        if (manifest != null) {
            final Lesson lesson = manifest.getLesson(this.lessonId);
            if (lesson != null) {
                media = lesson.getMedia(this.mediaId);
            }
        }
        this.media = media;

        this.updateMediaThumbnail();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.openButton:
                if (this.media != null) {
                    if (this.media.isVideo()) {
                        // get the target resource
                        final Manifest manifest = this.getManifest();
                        final Resource resource =
                                manifest != null ? manifest.getResource(this.media.getResource()) : null;

                        // mark the media as viewed
                        this.getProgressManager().recordProgressAsync(this.getCourseId(), this.mediaId);

                        // check to see if the resource is a provider resource
                        if (resource != null && resource.isUri()) {
                            switch (resource.getProvider()) {
                                case PROVIDER_NONE:
                                    break;
                                case PROVIDER_YOUTUBE:
                                case PROVIDER_VIMEO:
                                case PROVIDER_UNKNOWN:
                                    final Intent intent = providerIntent(getActivity(), resource);
                                    if (intent != null) {
                                        startActivity(intent);
                                        return;
                                    }
                                default:
                                    return;
                            }
                        }

                        startActivity(MediaVideoActivity
                                              .newIntent(getActivity(), this.getCourseId(), this.media.getResource()));
                    } else if (this.media.isImage()) {
                        // mark the media as viewed
                        this.getProgressManager().recordProgressAsync(this.getCourseId(), this.mediaId);

                        startActivity(MediaImageActivity
                                              .newIntent(getActivity(), this.getCourseId(), this.media.getResource()));
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /** END lifecycle */

    private void findViews() {
        this.openButton = findView(View.class, R.id.openButton);
        this.thumbnail = findView(ImageView.class, R.id.thumbnail);
    }

    private void clearViews() {
        this.openButton = null;
        this.thumbnail = null;
    }

    private void setupOpenButton() {
        if (this.openButton != null) {
            this.openButton.setOnClickListener(this);
        }
    }

    private void updateMediaThumbnail() {
        if (this.thumbnail != null) {
            // find the resource id for the thumbnail
            String resourceId = null;
            if (this.media != null) {
                resourceId = media.getThumbnail();

                // use the actual image if this is an image resource and
                // there isn't a thumbnail
                if (resourceId == null && media.isImage()) {
                    resourceId = media.getResource();
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
