package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.model.Resource.INVALID_VIDEO;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Resource;

public final class ResourceMapper extends AbstractMapper<Resource> {
    @Override
    public ContentValues toContentValues(final Resource resource) {
        return this.toContentValues(resource, Contract.Course.Resource.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Resource resource) {
        switch (field) {
            case Contract.Course.Resource.COLUMN_NAME_COURSE_ID:
                values.put(field, resource.getCourseId());
                break;
            case Contract.Course.Resource.COLUMN_NAME_RESOURCE_ID:
                values.put(field, resource.getId());
                break;
            case Contract.Course.Resource.COLUMN_NAME_TYPE:
                values.put(field, resource.getResourceType());
                break;
            case Contract.Course.Resource.COLUMN_NAME_MIMETYPE:
                values.put(field, resource.getResourceMimeType());
                break;
            case Contract.Course.Resource.COLUMN_NAME_SHA1:
                values.put(field, resource.getResourceSha1());
                break;
            case Contract.Course.Resource.COLUMN_NAME_SIZE:
                values.put(field, resource.getResourceSize());
                break;
            case Contract.Course.Resource.COLUMN_NAME_PROVIDER:
                values.put(field, resource.getProviderName());
                break;
            case Contract.Course.Resource.COLUMN_NAME_URI:
                values.put(field, resource.getUri());
                break;
            case Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE:
                values.put(field, resource.getParentId());
                break;
            case Contract.Course.Resource.COLUMN_VIDEO_ID:
                values.put(field, resource.getVideoId());
                break;
            default:
                super.mapField(values, field, resource);
                break;
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
        resource.setParentId(this.getString(c, Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE));
        resource.setResourceType(this.getString(c, Contract.Course.Resource.COLUMN_NAME_TYPE));
        resource.setResourceMimeType(this.getString(c, Contract.Course.Resource.COLUMN_NAME_MIMETYPE));
        resource.setResourceSha1(this.getString(c, Contract.Course.Resource.COLUMN_NAME_SHA1));
        resource.setResourceSize(this.getLong(c, Contract.Course.Resource.COLUMN_NAME_SIZE, 0));
        resource.setProvider(this.getString(c, Contract.Course.Resource.COLUMN_NAME_PROVIDER));
        resource.setUri(this.getString(c, Contract.Course.Resource.COLUMN_NAME_URI));
        resource.setVideoId(this.getLong(c, Contract.Course.Resource.COLUMN_VIDEO_ID, INVALID_VIDEO));
        return resource;
    }
}
