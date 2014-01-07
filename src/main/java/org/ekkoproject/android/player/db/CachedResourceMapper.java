package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.CachedResource;

public abstract class CachedResourceMapper<T extends CachedResource> extends AbstractMapper<T> {
    @Override
    public T toObject(final Cursor c) {
        final T resource = super.toObject(c);
        resource.setCourseId(this.getLong(c, Contract.CachedResource.COLUMN_COURSE_ID, INVALID_COURSE));
        resource.setPath(this.getString(c, Contract.CachedResource.COLUMN_PATH, null));
        resource.setSize(this.getLong(c, Contract.CachedResource.COLUMN_SIZE, 0));
        resource.setLastAccessed(this.getLong(c, Contract.CachedResource.COLUMN_LAST_ACCESSED, 0));
        return resource;
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final T resource) {
        switch (field) {
            case Contract.CachedResource.COLUMN_COURSE_ID:
                values.put(field, resource.getCourseId());
                break;
            case Contract.CachedResource.COLUMN_PATH:
                values.put(field, resource.getPath());
                break;
            case Contract.CachedResource.COLUMN_SIZE:
                values.put(field, resource.getSize());
                break;
            case Contract.CachedResource.COLUMN_LAST_ACCESSED:
                values.put(field, resource.getLastAccessed());
                break;
            default:
                super.mapField(values, field, resource);
                break;
        }
    }
}
