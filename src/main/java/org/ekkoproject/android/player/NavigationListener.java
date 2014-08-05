package org.ekkoproject.android.player;

public interface NavigationListener {
    void showCourseList(boolean showAll);

    /**
     * Show the specified Course. This should be called from the UI thread
     *
     * @param courseId the id of the course to show
     */
    void showCourse(long courseId);
}
