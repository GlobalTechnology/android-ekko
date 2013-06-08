package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.model.Course;

import android.content.ContentValues;
import android.database.Cursor;

public final class CourseMapper extends AbstractMapper<Course> {
    @Override
    public ContentValues toContentValues(final Course course) {
        return this.toContentValues(course, Contract.Course.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final Course course, final String[] projection) {
        final ContentValues values = new ContentValues();

        // only add values in the projection
        // XXX: I really want to use a switch for readability, but it requires
        // java 1.7 for String switch support
        for (final String field : projection) {
            if (Contract.Course.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_COURSE_ID, course.getId());
            } else if (Contract.Course.COLUMN_NAME_VERSION.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_VERSION, course.getVersion());
            } else if (Contract.Course.COLUMN_NAME_TITLE.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_TITLE, course.getCourseTitle());
            } else if (Contract.Course.COLUMN_NAME_BANNER_RESOURCE.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_BANNER_RESOURCE, course.getCourseBanner());
            } else if (Contract.Course.COLUMN_NAME_MANIFEST_FILE.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_MANIFEST_FILE, course.getManifestFile());
            } else if (Contract.Course.COLUMN_NAME_MANIFEST_VERSION.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_MANIFEST_VERSION, course.getManifestVersion());
            } else if (Contract.Course.COLUMN_NAME_LAST_SYNCED.equals(field)) {
                values.put(Contract.Course.COLUMN_NAME_LAST_SYNCED, course.getLastSynced());
            }
        }

        return values;
    }

    @Override
    public Course toObject(final Cursor c) {
        final Course course = new Course(this.getLong(c, Contract.Course.COLUMN_NAME_COURSE_ID, INVALID_COURSE));

        course.setVersion(this.getInt(c, Contract.Course.COLUMN_NAME_VERSION));
        course.setCourseTitle(this.getString(c, Contract.Course.COLUMN_NAME_TITLE));
        course.setCourseBanner(this.getString(c, Contract.Course.COLUMN_NAME_BANNER_RESOURCE));
        course.setManifestFile(this.getString(c, Contract.Course.COLUMN_NAME_MANIFEST_FILE));
        course.setManifestVersion(this.getInt(c, Contract.Course.COLUMN_NAME_MANIFEST_VERSION));
        course.setLastSynced(this.getLong(c, Contract.Course.COLUMN_NAME_LAST_SYNCED));

        return course;
    }
}
