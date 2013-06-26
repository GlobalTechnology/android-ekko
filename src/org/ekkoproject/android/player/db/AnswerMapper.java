package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.ekkoproject.android.player.model.Answer;

import android.content.ContentValues;
import android.database.Cursor;

public class AnswerMapper extends AbstractMapper<Answer> {
    @Override
    public ContentValues toContentValues(final Answer progress) {
        return this.toContentValues(progress, Contract.Answer.PROJECTION_ALL);
    }

    @Override
    public ContentValues toContentValues(final Answer answer, final String[] projection) {
        // only add values in the projection
        final ContentValues values = new ContentValues();
        for (final String field : projection) {
            if (Contract.Answer.COLUMN_NAME_COURSE_ID.equals(field)) {
                values.put(Contract.Answer.COLUMN_NAME_COURSE_ID, answer.getCourseId());
            } else if (Contract.Answer.COLUMN_NAME_QUESTION_ID.equals(field)) {
                values.put(Contract.Answer.COLUMN_NAME_QUESTION_ID, answer.getQuestionId());
            } else if (Contract.Answer.COLUMN_NAME_ANSWER_ID.equals(field)) {
                values.put(Contract.Answer.COLUMN_NAME_ANSWER_ID, answer.getAnswerId());
            } else if (Contract.Answer.COLUMN_NAME_ANSWERED.equals(field)) {
                values.put(Contract.Answer.COLUMN_NAME_ANSWERED, answer.getAnswered());
            }
        }
        return values;
    }

    @Override
    public Answer toObject(final Cursor c) {
        final Answer answer = new Answer(this.getLong(c, Contract.Answer.COLUMN_NAME_COURSE_ID, INVALID_COURSE),
                this.getString(c, Contract.Answer.COLUMN_NAME_QUESTION_ID, null), this.getString(c,
                        Contract.Answer.COLUMN_NAME_ANSWER_ID, null));
        answer.setAnswered(this.getLong(c, Contract.Answer.COLUMN_NAME_ANSWERED, 0));
        return answer;
    }
}
