package org.ekkoproject.android.player.db;

import org.appdev.entity.Course;

import android.content.ContentValues;
import android.database.Cursor;

public final class CourseMapper implements Mapper<Course> {
    @Override
    public ContentValues toContentValues(final Course course) {
        return this.toContentValues(course, Contract.Course.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final Course course, final String[] projection) {
        final ContentValues values = new ContentValues();
        values.put(Contract.Course.COLUMN_NAME_COURSE_ID, course.getId());
        values.put(Contract.Course.COLUMN_NAME_VERSION, course.getVersion());
        values.put(Contract.Course.COLUMN_NAME_TITLE, course.getCourseTitle());
        values.put(Contract.Course.COLUMN_NAME_BANNER_RESOURCE, course.getCourseBanner());
        values.put(Contract.Course.COLUMN_NAME_LAST_SYNCED, course.getLastSynced());

        return values;
    }

    @Override
    public Course toObject(final Cursor c) {
        final Course course = new Course(c.getLong(c.getColumnIndex(Contract.Course.COLUMN_NAME_COURSE_ID)));
        course.setVersion(c.getInt(c.getColumnIndex(Contract.Course.COLUMN_NAME_VERSION)));
        course.setCourseTitle(c.getString(c.getColumnIndex(Contract.Course.COLUMN_NAME_TITLE)));
        course.setCourseBanner(c.getString(c.getColumnIndex(Contract.Course.COLUMN_NAME_BANNER_RESOURCE)));
        course.setLastSynced(c.getLong(c.getColumnIndex(Contract.Course.COLUMN_NAME_LAST_SYNCED)));

        return course;
    }

}
