package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.CachedResource;

public class CachedResourceMapper extends AbstractMapper<CachedResource> {
    @Override
    public ContentValues toContentValues(final CachedResource resource) {
        return this.toContentValues(resource, Contract.CachedResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedResource resource) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.CachedResource.COLUMN_NAME_COURSE_ID.equals(field)) {
            values.put(field, resource.getCourseId());
        } else if (Contract.CachedResource.COLUMN_NAME_SHA1.equals(field)) {
            values.put(field, resource.getSha1());
        } else if (Contract.CachedResource.COLUMN_NAME_SIZE.equals(field)) {
            values.put(field, resource.getSize());
        } else if (Contract.CachedResource.COLUMN_NAME_PATH.equals(field)) {
            values.put(field, resource.getPath());
        } else if (Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED.equals(field)) {
            values.put(field, resource.getLastAccessed());
        } else {
            super.mapField(values, field, resource);
        }
    }

    @Override
    protected CachedResource newObject(final Cursor c) {
        return new CachedResource();
    }

    @Override
    public CachedResource toObject(final Cursor c) {
        final CachedResource resource = super.toObject(c);
        resource.setCourseId(this.getLong(c, Contract.CachedResource.COLUMN_NAME_COURSE_ID, INVALID_COURSE));
        resource.setSha1(this.getString(c, Contract.CachedResource.COLUMN_NAME_SHA1, null));
        resource.setSize(this.getLong(c, Contract.CachedResource.COLUMN_NAME_SIZE, 0));
        resource.setPath(this.getString(c, Contract.CachedResource.COLUMN_NAME_PATH, null));
        resource.setLastAccessed(this.getLong(c, Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED, 0));
        return resource;
    }
}
