package org.ekkoproject.android.player.support.v4.fragment.lesson;

import static org.ekkoproject.android.player.BuildConfig.ARCLIGHT_API_KEY;
import static org.ekkoproject.android.player.BuildConfig.VERSION_NAME;
import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_NONE;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_UNKNOWN;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_VIMEO;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_YOUTUBE;
import static org.ekkoproject.android.player.util.ResourceUtils.providerIntent;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.arclight.eventtracker.EventTracker;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.activity.MediaImageActivity;
import org.ekkoproject.android.player.activity.MediaVideoActivity;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Media;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.support.v4.fragment.AbstractManifestAwareFragment;
import org.ekkoproject.android.player.util.ResourceUtils;

public class MediaFragment extends AbstractManifestAwareFragment implements View.OnClickListener {
    private static final String ARG_MEDIAID = MediaFragment.class.getName() + ".ARG_MEDIAID";

    private String lessonId = null;
    private String mediaId = null;

    private Media media = null;

    private View openButton = null;
    private ImageView mThumbnail = null;

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

                        // do some special processing for certain resource types
                        if (resource != null) {
                            // check to see if the resource is a provider resource
                            if (resource.isUri()) {
                                switch (resource.getProvider()) {
                                    case PROVIDER_NONE:
                                        break;
                                    case PROVIDER_YOUTUBE:
                                    case PROVIDER_VIMEO:
                                    case PROVIDER_UNKNOWN:
                                        final Intent intent = providerIntent(getActivity(), resource);
                                        if (intent != null) {
                                            if (intent.resolveActivity(this.getActivity().getPackageManager()) ==
                                                    null) {
                                                Toast.makeText(getActivity(), R.string.media_unable_to_play,
                                                               Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            startActivity(intent);
                                            return;
                                        }
                                    default:
                                        return;
                                }
                            }

                            // Initialize Arclight Event Tracker for Arclight resources
                            if (resource.isArclight()) {
                                final Context context = getActivity();
                                EventTracker.getInstance()
                                        .initialize(context, ARCLIGHT_API_KEY, context.getPackageName(), VERSION_NAME);
                            }
                        }

                        // start video activity
                        startActivity(MediaVideoActivity.newIntent(getActivity(), this.getCourseId(),
                                                                   this.media.getResource()));
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
        mThumbnail = findView(ImageView.class, R.id.thumbnail);
    }

    private void clearViews() {
        this.openButton = null;
        mThumbnail = null;
    }

    private void setupOpenButton() {
        if (this.openButton != null) {
            this.openButton.setOnClickListener(this);
        }
    }

    private void updateMediaThumbnail() {
        if (mThumbnail != null) {
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
            ResourceUtils.setImage(mThumbnail, getCourseId(), resourceId);
        }
    }
}
