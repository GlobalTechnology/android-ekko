package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.CachedUriResource;

public class CachedUriResourceMapper extends AbstractMapper<CachedUriResource> {
    @Override
    public ContentValues toContentValues(final CachedUriResource resource) {
        return this.toContentValues(resource, Contract.CachedUriResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedUriResource resource) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.CachedUriResource.COLUMN_COURSE_ID.equals(field)) {
            values.put(field, resource.getCourseId());
        } else if (Contract.CachedUriResource.COLUMN_URI.equals(field)) {
            values.put(field, resource.getUri());
        } else if (Contract.CachedUriResource.COLUMN_SIZE.equals(field)) {
            values.put(field, resource.getSize());
        } else if (Contract.CachedUriResource.COLUMN_PATH.equals(field)) {
            values.put(field, resource.getPath());
        } else if (Contract.CachedUriResource.COLUMN_EXPIRES.equals(field)) {
            values.put(field, resource.getExpires());
        } else if (Contract.CachedUriResource.COLUMN_LAST_ACCESSED.equals(field)) {
            values.put(field, resource.getLastAccessed());
        } else if (Contract.CachedUriResource.COLUMN_LAST_MODIFIED.equals(field)) {
            values.put(field, resource.getLastAccessed());
        } else {
            super.mapField(values, field, resource);
        }
    }

    @Override
    protected CachedUriResource newObject(final Cursor c) {
        return new CachedUriResource();
    }

    @Override
    public CachedUriResource toObject(final Cursor c) {
        final CachedUriResource resource = super.toObject(c);
        resource.setCourseId(this.getLong(c, Contract.CachedUriResource.COLUMN_COURSE_ID, INVALID_COURSE));
        resource.setUri(this.getString(c, Contract.CachedUriResource.COLUMN_URI, null));
        resource.setSize(this.getLong(c, Contract.CachedUriResource.COLUMN_SIZE, 0));
        resource.setPath(this.getString(c, Contract.CachedUriResource.COLUMN_PATH, null));
        resource.setExpires(this.getLong(c, Contract.CachedUriResource.COLUMN_EXPIRES, 0));
        resource.setLastModified(this.getLong(c, Contract.CachedUriResource.COLUMN_LAST_MODIFIED, 0));
        resource.setLastAccessed(this.getLong(c, Contract.CachedUriResource.COLUMN_LAST_ACCESSED, 0));
        return resource;
    }
}
