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
        switch (field) {
            case Contract.Course.COLUMN_NAME_COURSE_ID:
                values.put(field, course.getId());
                break;
            case Contract.Course.COLUMN_NAME_VERSION:
                values.put(field, course.getVersion());
                break;
            case Contract.Course.COLUMN_NAME_TITLE:
                values.put(field, course.getTitle());
                break;
            case Contract.Course.COLUMN_NAME_BANNER_RESOURCE:
                values.put(field, course.getBanner());
                break;
            case Contract.Course.COLUMN_AUTHOR_NAME:
                values.put(field, course.getAuthorName());
                break;
            case Contract.Course.COLUMN_DESCRIPTION:
                values.put(field, course.getDescription());
                break;
            case Contract.Course.COLUMN_COPYRIGHT:
                values.put(field, course.getCopyright());
                break;
            case Contract.Course.COLUMN_ENROLLMENT_TYPE:
                values.put(field, course.getEnrollmentType());
                break;
            case Contract.Course.COLUMN_PUBLIC:
                values.put(field, course.isPublicCourse() ? 1 : 0);
                break;
            case Contract.Course.COLUMN_NAME_MANIFEST_FILE:
                values.put(field, course.getManifestFile());
                break;
            case Contract.Course.COLUMN_NAME_MANIFEST_VERSION:
                values.put(field, course.getManifestVersion());
                break;
            case Contract.Course.COLUMN_NAME_LAST_SYNCED:
                values.put(field, course.getLastSynced());
                break;
            default:
                super.mapField(values, field, course);
                break;
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
        course.setAuthorName(this.getString(c, Contract.Course.COLUMN_AUTHOR_NAME));
        course.setDescription(this.getString(c, Contract.Course.COLUMN_DESCRIPTION));
        course.setCopyright(this.getString(c, Contract.Course.COLUMN_COPYRIGHT));
        course.setEnrollmentType(this.getInt(c, Contract.Course.COLUMN_ENROLLMENT_TYPE, ENROLLMENT_TYPE_UNKNOWN));
        course.setPublicCourse(this.getBool(c, Contract.Course.COLUMN_PUBLIC, false));
        course.setManifestFile(this.getString(c, Contract.Course.COLUMN_NAME_MANIFEST_FILE));
        course.setManifestVersion(this.getInt(c, Contract.Course.COLUMN_NAME_MANIFEST_VERSION));
        course.setLastSynced(this.getLong(c, Contract.Course.COLUMN_NAME_LAST_SYNCED));
        return course;
    }
}
