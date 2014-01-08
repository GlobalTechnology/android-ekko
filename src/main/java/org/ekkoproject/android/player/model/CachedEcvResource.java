package org.ekkoproject.android.player.model;

public class CachedEcvResource extends CachedResource {
    private final long videoId;
    private final boolean thumbnail;

    public CachedEcvResource(final long courseId, final long videoId, final boolean thumbnail) {
        super(courseId);
        this.videoId = videoId;
        this.thumbnail = thumbnail;
    }

    public long getVideoId() {
        return this.videoId;
    }

    public boolean isThumbnail() {
        return this.thumbnail;
    }
}
