package org.ekkoproject.android.player.model;

import java.util.Date;

public class CachedUriResource {
    private long courseId;
    private String uri;
    private long size;
    private String path;
    private Date lastAccessed = new Date(0);
    private Date lastModified = new Date(0);
    private Date expires = new Date();

    public long getCourseId() {
        return this.courseId;
    }

    public String getUri() {
        return this.uri;
    }

    public long getSize() {
        return this.size;
    }

    public String getPath() {
        return this.path;
    }

    public long getExpires() {
        return this.expires.getTime();
    }

    public Date getExpiresDate() {
        return this.expires;
    }

    public long getLastAccessed() {
        return this.lastAccessed.getTime();
    }

    public Date getLastAccessedDate() {
        return this.lastAccessed;
    }

    public long getLastModified() {
        return this.lastModified.getTime();
    }

    public Date getLastModifiedDate() {
        return this.lastModified;
    }

    public void setCourseId(final long courseId) {
        this.courseId = courseId;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setPath(final String path) {
        this.path = path;
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

    public void setLastAccessed() {
        this.lastAccessed = new Date();
    }

    public void setLastAccessed(final long time) {
        this.lastAccessed = new Date(time);
    }

    public void setLastAccessed(final Date time) {
        this.lastAccessed = time != null ? time : new Date(0);
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
