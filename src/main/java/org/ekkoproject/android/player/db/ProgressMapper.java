package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Progress;

public class ProgressMapper extends AbstractMapper<Progress> {
    @Override
    public ContentValues toContentValues(final Progress progress) {
        return this.toContentValues(progress, Contract.Progress.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Progress progress) {
        // XXX: I really want to use a switch for readability, but String switch support requires java 1.7
        if (Contract.Progress.COLUMN_NAME_COURSE_ID.equals(field)) {
            values.put(field, progress.getCourseId());
        } else if (Contract.Progress.COLUMN_NAME_CONTENT_ID.equals(field)) {
            values.put(field, progress.getContentId());
        } else if (Contract.Progress.COLUMN_NAME_COMPLETED.equals(field)) {
            values.put(field, progress.getCompleted());
        } else {
            super.mapField(values, field, progress);
        }
    }

    @Override
    protected Progress newObject(final Cursor c) {
        return new Progress(this.getLong(c, Contract.Progress.COLUMN_NAME_COURSE_ID, INVALID_COURSE),
                            this.getString(c, Contract.Progress.COLUMN_NAME_CONTENT_ID, null));
    }

    @Override
    public Progress toObject(final Cursor c) {
        final Progress progress = super.toObject(c);
        progress.setCompleted(this.getLong(c, Contract.Progress.COLUMN_NAME_COMPLETED, 0));
        return progress;
    }
}
