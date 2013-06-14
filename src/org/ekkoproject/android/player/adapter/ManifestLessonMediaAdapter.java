package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;

import java.util.Collections;
import java.util.List;

import org.appdev.R;
import org.appdev.entity.Lesson;
import org.appdev.entity.Media;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.tasks.LoadImageResourceAsyncTask;
import org.ekkoproject.android.player.view.ResourceImageView;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class ManifestLessonMediaAdapter extends ManifestLessonAdapter<Media> {
    private static final List<Media> NO_MEDIA = Collections.emptyList();

    private static final int VIEW_TYPE_AUDIO = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_VIDEO = 2;

    private final ResourceManager resourceManager;

    private List<Media> media = NO_MEDIA;

    private int audioView = DEFAULT_LAYOUT;
    private int imageView = DEFAULT_LAYOUT;
    private int videoView = DEFAULT_LAYOUT;

    public ManifestLessonMediaAdapter(final Context context, final String lessonId) {
        super(context, lessonId);
        this.resourceManager = ResourceManager.getInstance(context);
    }

    public void setAudioView(final int layout) {
        this.audioView = layout;
    }

    public void setImageView(final int layout) {
        this.imageView = layout;
    }

    public void setVideoView(final int layout) {
        this.videoView = layout;
    }

    @Override
    protected void onNewLesson(final Lesson lesson) {
        super.onNewLesson(lesson);

        if (lesson != null) {
            this.media = lesson.getMedia();
        } else {
            this.media = NO_MEDIA;
        }
    }

    @Override
    public int getCount() {
        return this.media.size();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        final Media item = this.media.get(position);
        if (item.isAudio()) {
            return VIEW_TYPE_AUDIO;
        } else if (item.isVideo()) {
            return VIEW_TYPE_VIDEO;
        } else if (item.isImage()) {
            return VIEW_TYPE_IMAGE;
        }

        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    protected int getLayout(final int viewType) {
        switch (viewType) {
        case VIEW_TYPE_AUDIO:
            return this.audioView;
        case VIEW_TYPE_VIDEO:
            return this.videoView;
        case VIEW_TYPE_IMAGE:
        default:
            return this.imageView;
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public Media getItem(final int position) {
        return this.media.get(position);
    }

    @Override
    protected void bindView(final View view, final Media media) {
        final String thumbnail = media.getMediaThumbnailID();
        final String resource = media.getMediaResourceID();

        // set the thumbnail image
        final View thumbnailView = view.findViewById(R.id.thumbnail);
        if (thumbnailView instanceof ImageView) {
            ((ImageView) thumbnailView).setImageDrawable(null);
            if (thumbnail != null) {
                new LoadImageResourceAsyncTask(this.resourceManager, (ImageView) thumbnailView, this.getManifest()
                        .getCourseId(), thumbnail).execute();
            } else if (media.isImage() && resource != null) {
                new LoadImageResourceAsyncTask(this.resourceManager, (ImageView) thumbnailView, this.getManifest()
                        .getCourseId(), resource).execute();
            }
        } else if (thumbnailView instanceof ResourceImageView) {
            if (thumbnail != null) {
                ((ResourceImageView) thumbnailView).setResource(this.getManifest().getCourseId(), thumbnail);
            } else if (media.isImage() && resource != null) {
                ((ResourceImageView) thumbnailView).setResource(this.getManifest().getCourseId(), resource);
            } else {
                ((ResourceImageView) thumbnailView).setResource(this.getManifest().getCourseId(), null);
            }
        }

        if (media.isAudio()) {
            this.bindAudioView(view, media);
        } else if (media.isImage()) {
            this.bindImageView(view, media);
        } else if (media.isVideo()) {
            this.bindVideoView(view, media);
        }
    }

    private void bindAudioView(final View view, final Media audio) {
    }

    private void bindImageView(final View view, final Media image) {
    }

    private void bindVideoView(final View view, final Media video) {
    }
}
