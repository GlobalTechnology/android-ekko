package org.ekkoproject.android.player.model;

import java.util.Date;

public class CachedUriResource extends CachedResource {
    private final String uri;
    private Date lastModified = new Date(0);
    private Date expires = new Date();

    public CachedUriResource(final long courseId, final String uri) {
        super(courseId);
        this.uri = uri;
    }

    public String getUri() {
        return this.uri;
    }

    public long getExpires() {
        return this.expires.getTime();
    }

    public Date getExpiresDate() {
        return this.expires;
    }

    public long getLastModified() {
        return this.lastModified.getTime();
    }

    public Date getLastModifiedDate() {
        return this.lastModified;
    }

    public void setExpires() {
        this.expires = new Date();
    }

    public void setExpires(final long time) {
        this.expires = new Date(time);
    }

    public void setExpires(final Date time) {
        this.expires = time != null ? time : new Date(0);
    }

    public void setLastModified() {
        this.lastModified = new Date();
    }

    public void setLastModified(final long time) {
        this.lastModified = new Date(time);
    }

    public void setLastModified(final Date time) {
        this.lastModified = time != null ? time : new Date(0);
    }
}
