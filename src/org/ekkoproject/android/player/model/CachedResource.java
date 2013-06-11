package org.ekkoproject.android.player.model;

import java.util.Date;
import java.util.Locale;

public class CachedResource {
    private long courseId;
    private String sha1;
    private long size;
    private String path;
    private Date lastAccessed = new Date(0);

    public long getCourseId() {
        return this.courseId;
    }

    public String getSha1() {
        return this.sha1;
    }

    public long getSize() {
        return this.size;
    }

    public String getPath() {
        return this.path;
    }

    public long getLastAccessed() {
        return this.lastAccessed.getTime();
    }

    public Date getLastAccessedDate() {
        return this.lastAccessed;
    }

    public void setCourseId(final long courseId) {
        this.courseId = courseId;
    }

    public void setSha1(final String sha1) {
        this.sha1 = sha1 != null ? sha1.toLowerCase(Locale.US) : null;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public void setPath(final String path) {
        this.path = path;
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
}
