package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.model.Course.ENROLLMENT_TYPE_UNKNOWN;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Course;

public final class CourseMapper extends AbstractMapper<Course> {
    @Override
    public ContentValues toContentValues(final Course course) {
        return this.toContentValues(course, Contract.Course.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Course course) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.Course.COLUMN_NAME_COURSE_ID.equals(field)) {
            values.put(field, course.getId());
        } else if (Contract.Course.COLUMN_NAME_VERSION.equals(field)) {
            values.put(field, course.getVersion());
        } else if (Contract.Course.COLUMN_NAME_TITLE.equals(field)) {
            values.put(field, course.getTitle());
        } else if (Contract.Course.COLUMN_NAME_BANNER_RESOURCE.equals(field)) {
            values.put(field, course.getBanner());
        } else if (Contract.Course.COLUMN_ENROLLMENT_TYPE.equals(field)) {
            values.put(field, course.getEnrollmentType());
        } else if (Contract.Course.COLUMN_PUBLIC.equals(field)) {
            values.put(field, course.isPublicCourse() ? 1 : 0);
        } else if (Contract.Course.COLUMN_NAME_MANIFEST_FILE.equals(field)) {
            values.put(field, course.getManifestFile());
        } else if (Contract.Course.COLUMN_NAME_MANIFEST_VERSION.equals(field)) {
            values.put(field, course.getManifestVersion());
        } else if (Contract.Course.COLUMN_NAME_LAST_SYNCED.equals(field)) {
            values.put(field, course.getLastSynced());
        } else {
            super.mapField(values, field, course);
        }
    }

    @Override
    protected Course newObject(final Cursor c) {
        return new Course(this.getLong(c, Contract.Course.COLUMN_NAME_COURSE_ID, INVALID_COURSE));
    }

    @Override
    public Course toObject(final Cursor c) {
        final Course course = super.toObject(c);
        course.setVersion(this.getInt(c, Contract.Course.COLUMN_NAME_VERSION));
        course.setTitle(this.getString(c, Contract.Course.COLUMN_NAME_TITLE));
        course.setBanner(this.getString(c, Contract.Course.COLUMN_NAME_BANNER_RESOURCE));
        course.setEnrollmentType(this.getInt(c, Contract.Course.COLUMN_ENROLLMENT_TYPE, ENROLLMENT_TYPE_UNKNOWN));
        course.setPublicCourse(this.getBool(c, Contract.Course.COLUMN_PUBLIC, false));
        course.setManifestFile(this.getString(c, Contract.Course.COLUMN_NAME_MANIFEST_FILE));
        course.setManifestVersion(this.getInt(c, Contract.Course.COLUMN_NAME_MANIFEST_VERSION));
        course.setLastSynced(this.getLong(c, Contract.Course.COLUMN_NAME_LAST_SYNCED));
        return course;
    }
}
