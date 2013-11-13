package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.content.ContentValues;
import android.database.Cursor;

import org.ccci.gto.android.common.db.AbstractMapper;
import org.ekkoproject.android.player.model.Answer;

public class AnswerMapper extends AbstractMapper<Answer> {
    @Override
    public ContentValues toContentValues(final Answer progress) {
        return this.toContentValues(progress, Contract.Answer.PROJECTION_ALL);
    }

    @Override
    protected void mapField(final ContentValues values, final String field, final Answer answer) {
        switch (field) {
            case Contract.Answer.COLUMN_GUID:
                values.put(field, answer.getGuid());
                break;
            case Contract.Answer.COLUMN_COURSE_ID:
                values.put(field, answer.getCourseId());
                break;
            case Contract.Answer.COLUMN_QUESTION_ID:
                values.put(field, answer.getQuestionId());
                break;
            case Contract.Answer.COLUMN_ANSWER_ID:
                values.put(field, answer.getAnswerId());
                break;
            case Contract.Answer.COLUMN_ANSWERED:
                values.put(field, answer.getAnswered());
                break;
            default:
                super.mapField(values, field, answer);
                break;
        }
    }

    @Override
    protected Answer newObject(final Cursor c) {
        return new Answer(this.getString(c, Contract.Answer.COLUMN_GUID, null),
                          this.getLong(c, Contract.Answer.COLUMN_COURSE_ID, INVALID_COURSE),
                          this.getString(c, Contract.Answer.COLUMN_QUESTION_ID, null),
                          this.getString(c, Contract.Answer.COLUMN_ANSWER_ID, null));
    }

    @Override
    public Answer toObject(final Cursor c) {
        final Answer answer = super.toObject(c);
        answer.setAnswered(this.getLong(c, Contract.Answer.COLUMN_ANSWERED, 0));
        return answer;
    }
}
