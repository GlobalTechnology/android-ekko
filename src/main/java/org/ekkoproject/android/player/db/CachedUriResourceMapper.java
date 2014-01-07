package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ekkoproject.android.player.model.CachedUriResource;

public class CachedUriResourceMapper extends CachedResourceMapper<CachedUriResource> {
    @Override
    public ContentValues toContentValues(final CachedUriResource resource) {
        return this.toContentValues(resource, Contract.CachedUriResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedUriResource resource) {
        switch (field) {
            case Contract.CachedUriResource.COLUMN_URI:
                values.put(field, resource.getUri());
                break;
            case Contract.CachedUriResource.COLUMN_EXPIRES:
                values.put(field, resource.getExpires());
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
        return new CachedUriResource(this.getLong(c, Contract.CachedResource.COLUMN_COURSE_ID, INVALID_COURSE),
                                     this.getString(c, Contract.CachedUriResource.COLUMN_URI, null));
    }

    @Override
    public CachedUriResource toObject(final Cursor c) {
        final CachedUriResource resource = super.toObject(c);
        resource.setExpires(this.getLong(c, Contract.CachedUriResource.COLUMN_EXPIRES, 0));
        resource.setLastModified(this.getLong(c, Contract.CachedUriResource.COLUMN_LAST_MODIFIED, 0));
        return resource;
    }
}
