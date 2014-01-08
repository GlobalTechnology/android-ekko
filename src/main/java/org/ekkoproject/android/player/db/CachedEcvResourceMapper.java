package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.model.Resource.INVALID_VIDEO;

import android.content.ContentValues;
import android.database.Cursor;

import org.ekkoproject.android.player.model.CachedEcvResource;

public class CachedEcvResourceMapper extends CachedResourceMapper<CachedEcvResource> {
    @Override
    public ContentValues toContentValues(final CachedEcvResource resource) {
        return this.toContentValues(resource, Contract.CachedEcvResource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final CachedEcvResource resource) {
        switch (field) {
            case Contract.CachedEcvResource.COLUMN_VIDEO_ID:
                values.put(field, resource.getVideoId());
                break;
            case Contract.CachedEcvResource.COLUMN_THUMBNAIL:
                values.put(field, resource.isThumbnail() ? 1 : 0);
                break;
            default:
                super.mapField(values, field, resource);
                break;
        }
    }

    @Override
    protected CachedEcvResource newObject(final Cursor c) {
        return new CachedEcvResource(this.getLong(c, Contract.CachedResource.COLUMN_COURSE_ID, INVALID_COURSE),
                                     this.getLong(c, Contract.CachedEcvResource.COLUMN_VIDEO_ID, INVALID_VIDEO),
                                     this.getBool(c, Contract.CachedEcvResource.COLUMN_THUMBNAIL, false));
    }
}
