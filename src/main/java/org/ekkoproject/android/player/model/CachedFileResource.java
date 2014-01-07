package org.ekkoproject.android.player.model;

import java.util.Locale;

public class CachedFileResource extends CachedResource {
    private String sha1;

    public String getSha1() {
        return this.sha1;
    }

    public void setSha1(final String sha1) {
        this.sha1 = sha1 != null ? sha1.toLowerCase(Locale.US) : null;
    }
}
