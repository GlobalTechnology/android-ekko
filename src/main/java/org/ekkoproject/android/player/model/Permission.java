package org.ekkoproject.android.player.model;

public class Permission {
    private final long courseId;
    private final String guid;

    private boolean admin = false;
    private boolean enrolled = false;
    private boolean pending = false;
    private boolean contentVisible = false;

    private boolean hidden = false;

    public Permission(final long courseId, final String guid) {
        this.courseId = courseId;
        this.guid = guid != null ? guid.toUpperCase() : "";
    }

    public long getCourseId() {
        return this.courseId;
    }

    public String getGuid() {
        return this.guid;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public void setAdmin(final boolean admin) {
        this.admin = admin;
    }

    public boolean isEnrolled() {
        return this.enrolled;
    }

    public void setEnrolled(final boolean enrolled) {
        this.enrolled = enrolled;
    }

    public boolean isPending() {
        return this.pending;
    }

    public void setPending(final boolean pending) {
        this.pending = pending;
    }

    public boolean isContentVisible() {
        return this.contentVisible;
    }

    public void setContentVisible(final boolean contentVisible) {
        this.contentVisible = contentVisible;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
}
