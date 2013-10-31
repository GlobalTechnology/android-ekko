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
        switch (field) {
            case Contract.CachedResource.COLUMN_NAME_COURSE_ID:
                values.put(field, resource.getCourseId());
                break;
            case Contract.CachedResource.COLUMN_NAME_SHA1:
                values.put(field, resource.getSha1());
                break;
            case Contract.CachedResource.COLUMN_NAME_SIZE:
                values.put(field, resource.getSize());
                break;
            case Contract.CachedResource.COLUMN_NAME_PATH:
                values.put(field, resource.getPath());
                break;
            case Contract.CachedResource.COLUMN_NAME_LAST_ACCESSED:
                values.put(field, resource.getLastAccessed());
                break;
            default:
                super.mapField(values, field, resource);
                break;
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
