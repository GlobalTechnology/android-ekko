package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ekkoproject.android.player.model.CachedFileResource;

public class CachedFileResourceMapper extends CachedResourceMapper<CachedFileResource> {
    @Override
    public ContentValues toContentValues(final CachedFileResource resource) {
        return this.toContentValues(resource, Contract.CachedFileResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedFileResource resource) {
        switch (field) {
            case Contract.CachedFileResource.COLUMN_SHA1:
                values.put(field, resource.getSha1());
                break;
            default:
                super.mapField(values, field, resource);
                break;
        }
    }

    @Override
    protected CachedFileResource newObject(final Cursor c) {
        return new CachedFileResource(this.getLong(c, Contract.CachedResource.COLUMN_COURSE_ID, INVALID_COURSE),
                                      this.getString(c, Contract.CachedFileResource.COLUMN_SHA1, null));
    }
}
