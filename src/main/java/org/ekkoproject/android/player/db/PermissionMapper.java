package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Permission;

public class PermissionMapper extends AbstractMapper<Permission> {
    @Override
    public ContentValues toContentValues(final Permission permission) {
        return this.toContentValues(permission, Contract.Permission.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Permission permission) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.Permission.COLUMN_COURSE_ID.equals(field)) {
            values.put(field, permission.getCourseId());
        } else if (Contract.Permission.COLUMN_GUID.equals(field)) {
            values.put(field, permission.getGuid());
        } else if (Contract.Permission.COLUMN_ADMIN.equals(field)) {
            values.put(field, permission.isAdmin() ? 1 : 0);
        } else if (Contract.Permission.COLUMN_ENROLLED.equals(field)) {
            values.put(field, permission.isEnrolled() ? 1 : 0);
        } else if (Contract.Permission.COLUMN_PENDING.equals(field)) {
            values.put(field, permission.isPending() ? 1 : 0);
        } else if (Contract.Permission.COLUMN_CONTENT_VISIBLE.equals(field)) {
            values.put(field, permission.isContentVisible() ? 1 : 0);
        } else {
            super.mapField(values, field, permission);
        }
    }

    @Override
    protected Permission newObject(final Cursor c) {
        return new Permission(this.getLong(c, Contract.Permission.COLUMN_COURSE_ID, INVALID_COURSE),
                          this.getString(c, Contract.Permission.COLUMN_GUID));
    }

    @Override
    public Permission toObject(final Cursor c) {
        final Permission permission = super.toObject(c);
        permission.setAdmin(this.getBool(c, Contract.Permission.COLUMN_ADMIN, false));
        permission.setEnrolled(this.getBool(c, Contract.Permission.COLUMN_ENROLLED, false));
        permission.setPending(this.getBool(c, Contract.Permission.COLUMN_PENDING, false));
        permission.setContentVisible(this.getBool(c, Contract.Permission.COLUMN_CONTENT_VISIBLE, false));
        return permission;
    }
}
