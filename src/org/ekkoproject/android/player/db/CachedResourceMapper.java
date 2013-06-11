package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.model.CachedResource;

import android.content.ContentValues;
import android.database.Cursor;

public class CachedResourceMapper extends AbstractMapper<CachedResource> {
    @Override
    public ContentValues toContentValues(final CachedResource resource) {
        return this.toContentValues(resource, Contract.CachedResource.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final CachedResource resource, final String[] projection) {
        final ContentValues values = new ContentValues();

        // only add values in the projection
        // XXX: I really want to use a switch for readability, but it requires
        // java 1.7 for String switch support
        for (final String field : projection) {
            if (Contract.CachedResource.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.CachedResource.COLUMN_NAME_COURSE_ID, resource.getCourseId());
            } else if (Contract.CachedResource.COLUMN_NAME_SHA1.equals(field)) {
                values.put(Contract.CachedResource.COLUMN_NAME_SHA1, resource.getSha1());
            } else if (Contract.CachedResource.COLUMN_NAME_SIZE.equals(field)) {
                values.put(Contract.CachedResource.COLUMN_NAME_SIZE, resource.getSize());
            } else if (Contract.CachedResource.COLUMN_NAME_PATH.equals(field)) {
                values.put(Contract.CachedResource.COLUMN_NAME_PATH, resource.getPath());
            } else if (Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED.equals(field)) {
                values.put(Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED, resource.getLastAccessed());
            }
        }

        return values;
    }

    @Override
    public CachedResource toObject(final Cursor c) {
        final CachedResource resource = new CachedResource();

        resource.setCourseId(this.getLong(c, Contract.CachedResource.COLUMN_NAME_COURSE_ID, INVALID_COURSE));
        resource.setSha1(this.getString(c, Contract.CachedResource.COLUMN_NAME_SHA1, null));
        resource.setSize(this.getLong(c, Contract.CachedResource.COLUMN_NAME_SIZE, 0));
        resource.setPath(this.getString(c, Contract.CachedResource.COLUMN_NAME_PATH, null));
        resource.setLastAccessed(this.getLong(c, Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED, 0));

        return resource;
    }
}
