package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Access;

public class AccessMapper extends AbstractMapper<Access> {
    @Override
    public ContentValues toContentValues(final Access access) {
        return this.toContentValues(access, Contract.Access.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Access access) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.Access.COLUMN_COURSE_ID.equals(field)) {
            values.put(field, access.getCourseId());
        } else if (Contract.Access.COLUMN_GUID.equals(field)) {
            values.put(field, access.getGuid());
        } else if (Contract.Access.COLUMN_ADMIN.equals(field)) {
            values.put(field, access.isAdmin() ? 1 : 0);
        } else if (Contract.Access.COLUMN_ENROLLED.equals(field)) {
            values.put(field, access.isEnrolled() ? 1 : 0);
        } else if (Contract.Access.COLUMN_PENDING.equals(field)) {
            values.put(field, access.isPending() ? 1 : 0);
        } else if (Contract.Access.COLUMN_CONTENT_VISIBLE.equals(field)) {
            values.put(field, access.isContentVisible() ? 1 : 0);
        } else if (Contract.Access.COLUMN_VISIBLE.equals(field)) {
            values.put(field, access.isVisible() ? 1 : 0);
        } else {
            super.mapField(values, field, access);
        }
    }

    @Override
    protected Access newObject(final Cursor c) {
        return new Access(this.getLong(c, Contract.Access.COLUMN_COURSE_ID, INVALID_COURSE),
                          this.getString(c, Contract.Access.COLUMN_GUID));
    }

    @Override
    public Access toObject(final Cursor c) {
        final Access access = super.toObject(c);
        access.setAdmin(this.getBool(c, Contract.Access.COLUMN_ADMIN, false));
        access.setEnrolled(this.getBool(c, Contract.Access.COLUMN_ENROLLED, false));
        access.setPending(this.getBool(c, Contract.Access.COLUMN_PENDING, false));
        access.setContentVisible(this.getBool(c, Contract.Access.COLUMN_CONTENT_VISIBLE, false));
        access.setVisible(this.getBool(c, Contract.Access.COLUMN_VISIBLE, false));
        return access;
    }
}
