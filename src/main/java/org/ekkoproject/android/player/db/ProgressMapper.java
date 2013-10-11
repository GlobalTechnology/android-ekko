package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.model.Progress;

import android.content.ContentValues;
import android.database.Cursor;

public class ProgressMapper extends AbstractMapper<Progress> {
    @Override
    public ContentValues toContentValues(final Progress progress) {
        return this.toContentValues(progress, Contract.Progress.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final Progress progress, final String[] projection) {
        // only add values in the projection
        final ContentValues values = new ContentValues();
        for (final String field : projection) {
            if (Contract.Progress.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.Progress.COLUMN_NAME_COURSE_ID, progress.getCourseId());
            } else if (Contract.Progress.COLUMN_NAME_CONTENT_ID.equals(field)) {
                values.put(Contract.Progress.COLUMN_NAME_CONTENT_ID, progress.getContentId());
            } else if (Contract.Progress.COLUMN_NAME_COMPLETED.equals(field)) {
                values.put(Contract.Progress.COLUMN_NAME_COMPLETED, progress.getCompleted());
            }
        }
        return values;
    }

    @Override
    public Progress toObject(final Cursor c) {
        final Progress progress = new Progress(
                this.getLong(c, Contract.Progress.COLUMN_NAME_COURSE_ID, INVALID_COURSE), this.getString(c,
                        Contract.Progress.COLUMN_NAME_CONTENT_ID, null));
        progress.setCompleted(this.getLong(c, Contract.Progress.COLUMN_NAME_COMPLETED, 0));
        return progress;
    }
}
