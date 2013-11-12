package org.ekkoproject.android.player.model;

import java.util.Date;

public class Progress {
    private final String guid;
    private final long courseId;
    private final String contentId;
    private Date completed = new Date(0);

    public Progress(final String guid, final long courseId, final String contentId) {
        this.guid = guid;
        this.courseId = courseId;
        this.contentId = contentId;
    }

    public String getGuid() {
        return this.guid;
    }

    public long getCourseId() {
        return this.courseId;
    }

    public String getContentId() {
        return this.contentId;
    }

    public long getCompleted() {
        return this.completed.getTime();
    }

    public Date getCompletedDate() {
        return this.completed;
    }

    public void setCompleted() {
        this.completed = new Date();
    }

    public void setCompleted(final long completed) {
        this.completed = new Date(completed);
    }

    public void setCompleted(final Date completed) {
        this.completed = completed != null ? completed : new Date(0);
    }
}
