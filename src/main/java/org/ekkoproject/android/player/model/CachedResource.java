package org.ekkoproject.android.player.model;

import java.util.Date;

public abstract class CachedResource {
    private long courseId;
    private String path;
    private long size;
    private Date lastAccessed = new Date(0);

    public long getCourseId() {
        return this.courseId;
    }

    public void setCourseId(final long courseId) {
        this.courseId = courseId;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getLastAccessed() {
        return this.lastAccessed.getTime();
    }

    public Date getLastAccessedDate() {
        return this.lastAccessed;
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
