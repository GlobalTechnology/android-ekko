package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.GUID_GUEST;
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
        switch (field) {
            case Contract.Progress.COLUMN_GUID:
                values.put(field, progress.getGuid());
                break;
            case Contract.Progress.COLUMN_COURSE_ID:
                values.put(field, progress.getCourseId());
                break;
            case Contract.Progress.COLUMN_CONTENT_ID:
                values.put(field, progress.getContentId());
                break;
            case Contract.Progress.COLUMN_COMPLETED:
                values.put(field, progress.getCompleted());
                break;
            default:
                super.mapField(values, field, progress);
                break;
        }
    }

    @Override
    protected Progress newObject(final Cursor c) {
        return new Progress(this.getString(c, Contract.Progress.COLUMN_GUID, GUID_GUEST),
                            this.getLong(c, Contract.Progress.COLUMN_COURSE_ID, INVALID_COURSE),
                            this.getString(c, Contract.Progress.COLUMN_CONTENT_ID, null));
    }

    @Override
    public Progress toObject(final Cursor c) {
        final Progress progress = super.toObject(c);
        progress.setCompleted(this.getLong(c, Contract.Progress.COLUMN_COMPLETED, 0));
        return progress;
    }
}
