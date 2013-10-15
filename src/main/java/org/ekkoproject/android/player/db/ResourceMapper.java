package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.appdev.entity.Resource;
import org.ccci.gto.android.common.db.AbstractMapper;

public final class ResourceMapper extends AbstractMapper<Resource> {
    @Override
    public ContentValues toContentValues(final Resource resource) {
        return this.toContentValues(resource, Contract.Course.Resource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Resource resource) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.Course.Resource.COLUMN_NAME_COURSE_ID.equals(field)) {
            values.put(field, resource.getCourseId());
        } else if (Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID.equals(field)) {
            values.put(field, resource.getId());
        } else if (Contract.Course.Resource.COLUMN_NAME_TYPE.equals(field)) {
            values.put(field, resource.getResourceType());
        } else if (Contract.Course.Resource.COLUMN_NAME_MIMETYPE.equals(field)) {
            values.put(field, resource.getResourceMimeType());
        } else if (Contract.Course.Resource.COLUMN_NAME_SHA1.equals(field)) {
            values.put(field, resource.getResourceSha1());
        } else if (Contract.Course.Resource.COLUMN_NAME_SIZE.equals(field)) {
            values.put(field, resource.getResourceSize());
        } else if (Contract.Course.Resource.COLUMN_NAME_PROVIDER.equals(field)) {
            values.put(field, resource.getProviderName());
        } else if (Contract.Course.Resource.COLUMN_NAME_URI.equals(field)) {
            values.put(field, resource.getUri());
        } else if(Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE.equals(field)) {
            values.put(field, resource.getParentId());
        } else {
            super.mapField(values, field, resource);
        }
    }

    @Override
    protected Resource newObject(final Cursor c) {
        return new Resource(this.getLong(c, Contract.Course.Resource.COLUMN_NAME_COURSE_ID, INVALID_COURSE),
                            this.getString(c, Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID, null));
    }

    @Override
    public Resource toObject(final Cursor c) {
        final Resource resource = super.toObject(c);
        resource.setParentId(c.getString((c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE))));
        resource.setResourceType(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_TYPE)));
        resource.setResourceMimeType(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_MIMETYPE)));
        resource.setResourceSha1(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_SHA1)));
        resource.setResourceSize(c.getLong(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_SIZE)));
        resource.setProvider(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_PROVIDER)));
        resource.setUri(c.getString(c.getColumnIndex(Contract.Course.Resource.COLUMN_NAME_URI)));
        return resource;
    }
}
