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
        switch (field) {
            case Contract.Permission.COLUMN_COURSE_ID:
                values.put(field, permission.getCourseId());
                break;
            case Contract.Permission.COLUMN_GUID:
                values.put(field, permission.getGuid());
                break;
            case Contract.Permission.COLUMN_ADMIN:
                values.put(field, permission.isAdmin() ? 1 : 0);
                break;
            case Contract.Permission.COLUMN_ENROLLED:
                values.put(field, permission.isEnrolled() ? 1 : 0);
                break;
            case Contract.Permission.COLUMN_PENDING:
                values.put(field, permission.isPending() ? 1 : 0);
                break;
            case Contract.Permission.COLUMN_CONTENT_VISIBLE:
                values.put(field, permission.isContentVisible() ? 1 : 0);
                break;
            case Contract.Permission.COLUMN_HIDDEN:
                values.put(field, permission.isHidden() ? 1 : 0);
                break;
            default:
                super.mapField(values, field, permission);
                break;
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
        permission.setHidden(this.getBool(c, Contract.Permission.COLUMN_HIDDEN, false));
        return permission;
    }
}
