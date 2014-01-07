package org.ekkoproject.android.player.model;

import java.util.Locale;

public class CachedFileResource extends CachedResource {
    private final String sha1;

    public CachedFileResource(final long courseId, final String sha1) {
        super(courseId);
        this.sha1 = sha1 != null ? sha1.toLowerCase(Locale.US) : null;
    }

    public String getSha1() {
        return this.sha1;
    }
}
