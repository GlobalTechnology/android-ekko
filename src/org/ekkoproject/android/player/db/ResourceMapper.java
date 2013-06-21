package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.appdev.entity.Resource;

import android.content.ContentValues;
import android.database.Cursor;

public final class ResourceMapper extends AbstractMapper<Resource> {
    @Override
    public ContentValues toContentValues(final Resource resource) {
        return this.toContentValues(resource, Contract.Course.Resource.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final Resource resource, final String[] projection) {
        // generate all possible values
        final ContentValues values = new ContentValues();

        // only add values in the projection
        for (final String field : projection) {
            if (Contract.Course.Resource.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_COURSE_ID, resource.getCourseId());
            } else if (Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID, resource.getId());
            } else if (Contract.Course.Resource.COLUMN_NAME_TYPE.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_TYPE, resource.getResourceType());
            } else if (Contract.Course.Resource.COLUMN_NAME_MIMETYPE.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_MIMETYPE, resource.getResourceMimeType());
            } else if (Contract.Course.Resource.COLUMN_NAME_SHA1.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_SHA1, resource.getResourceSha1());
            } else if (Contract.Course.Resource.COLUMN_NAME_SIZE.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_SIZE, resource.getResourceSize());
            } else if (Contract.Course.Resource.COLUMN_NAME_PROVIDER.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_PROVIDER, resource.getProviderName());
            } else if (Contract.Course.Resource.COLUMN_NAME_URI.equals(field)) {
                values.put(Contract.Course.Resource.COLUMN_NAME_URI, resource.getUri());
            }
        }

        // return the values
        return values;
    }

    @Override
    public Resource toObject(final Cursor c) {
        final Resource resource = new Resource(this.getLong(c, Contract.Course.Resource.COLUMN_NAME_COURSE_ID,
                INVALID_COURSE), this.getString(c, Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID, null));
        resource.setResourceType(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_TYPE)));
        resource.setResourceMimeType(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_MIMETYPE)));
        resource.setResourceSha1(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_SHA1)));
        resource.setResourceSize(c.getLong(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_SIZE)));
        resource.setProvider(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_PROVIDER)));
        resource.setUri(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_URI)));

        return resource;
    }
}
