package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ekkoproject.android.player.model.CachedArclightResource;

public class CachedArclightResourceMapper extends CachedResourceMapper<CachedArclightResource> {
    @Override
    public ContentValues toContentValues(final CachedArclightResource resource) {
        return this.toContentValues(resource, Contract.CachedEcvResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedArclightResource resource) {
        switch (field) {
            case Contract.CachedArclightResource.COLUMN_REF_ID:
                values.put(field, resource.getRefId());
                break;
            case Contract.CachedArclightResource.COLUMN_THUMBNAIL:
                values.put(field, resource.isThumbnail() ? 1 : 0);
                break;
            default:
                super.mapField(values, field, resource);
                break;
        }
    }

    @Override
    protected CachedArclightResource newObject(final Cursor c) {
        return new CachedArclightResource(this.getLong(c, Contract.CachedResource.COLUMN_COURSE_ID, INVALID_COURSE),
                                          this.getString(c, Contract.CachedArclightResource.COLUMN_REF_ID, null),
                                          this.getBool(c, Contract.CachedArclightResource.COLUMN_THUMBNAIL, false));
    }
}
