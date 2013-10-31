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
        switch (field) {
            case Contract.CachedUriResource.COLUMN_COURSE_ID:
                values.put(field, resource.getCourseId());
                break;
            case Contract.CachedUriResource.COLUMN_URI:
                values.put(field, resource.getUri());
                break;
            case Contract.CachedUriResource.COLUMN_SIZE:
                values.put(field, resource.getSize());
                break;
            case Contract.CachedUriResource.COLUMN_PATH:
                values.put(field, resource.getPath());
                break;
            case Contract.CachedUriResource.COLUMN_EXPIRES:
                values.put(field, resource.getExpires());
                break;
            case Contract.CachedUriResource.COLUMN_LAST_ACCESSED:
                values.put(field, resource.getLastAccessed());
                break;
            case Contract.CachedUriResource.COLUMN_LAST_MODIFIED:
                values.put(field, resource.getLastAccessed());
                break;
            default:
                super.mapField(values, field, resource);
                break;
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
