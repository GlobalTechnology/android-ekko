package org.ekkoproject.android.player.model;

public class CachedArclightResource extends CachedResource {
    private final String refId;
    private final boolean thumbnail;

    public CachedArclightResource(final long courseId, final String refId, final boolean thumbnail) {
        super(courseId);
        this.refId = refId;
        this.thumbnail = thumbnail;
    }

    public String getRefId() {
        return this.refId;
    }

    public boolean isThumbnail() {
        return this.thumbnail;
    }
}
