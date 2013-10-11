package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.model.CachedUriResource;

import android.content.ContentValues;
import android.database.Cursor;

public class CachedUriResourceMapper extends AbstractMapper<CachedUriResource> {
    @Override
    public ContentValues toContentValues(final CachedUriResource resource) {
        return this.toContentValues(resource, Contract.CachedUriResource.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final CachedUriResource resource, final String[] projection) {
        final ContentValues values = new ContentValues();

        // only add values in the projection
        // XXX: I really want to use a switch for readability, but it requires
        // java 1.7 for String switch support
        for (final String field : projection) {
            if (Contract.CachedUriResource.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_COURSE_ID, resource.getCourseId());
            } else if (Contract.CachedUriResource.COLUMN_NAME_URI.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_URI, resource.getUri());
            } else if (Contract.CachedUriResource.COLUMN_NAME_SIZE.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_SIZE, resource.getSize());
            } else if (Contract.CachedUriResource.COLUMN_NAME_PATH.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_PATH, resource.getPath());
            } else if (Contract.CachedUriResource.COLUMN_NAME_EXPIRES.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_EXPIRES, resource.getExpires());
            } else if (Contract.CachedUriResource.COLUMN_NAME_LAST_ACCESSED.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_LAST_ACCESSED, resource.getLastAccessed());
            } else if (Contract.CachedUriResource.COLUMN_NAME_LAST_MODIFIED.equals(field)) {
                values.put(Contract.CachedUriResource.COLUMN_NAME_LAST_MODIFIED, resource.getLastAccessed());
            }
        }

        return values;
    }

    @Override
    public CachedUriResource toObject(final Cursor c) {
        final CachedUriResource resource = new CachedUriResource();

        resource.setCourseId(this.getLong(c, Contract.CachedUriResource.COLUMN_NAME_COURSE_ID, INVALID_COURSE));
        resource.setUri(this.getString(c, Contract.CachedUriResource.COLUMN_NAME_URI, null));
        resource.setSize(this.getLong(c, Contract.CachedUriResource.COLUMN_NAME_SIZE, 0));
        resource.setPath(this.getString(c, Contract.CachedUriResource.COLUMN_NAME_PATH, null));
        resource.setExpires(this.getLong(c, Contract.CachedUriResource.COLUMN_NAME_EXPIRES, 0));
        resource.setLastModified(this.getLong(c, Contract.CachedUriResource.COLUMN_NAME_LAST_MODIFIED, 0));
        resource.setLastAccessed(this.getLong(c, Contract.CachedUriResource.COLUMN_NAME_LAST_ACCESSED, 0));

        return resource;
    }
}
