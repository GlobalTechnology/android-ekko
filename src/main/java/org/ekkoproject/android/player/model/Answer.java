package org.ekkoproject.android.player.model;

import java.util.Date;

public class Answer {
    private final long courseId;
    private final String questionId;
    private final String answerId;
    private Date answered = new Date(0);

    public Answer(final long courseId, final String questionId, final String answerId) {
        this.courseId = courseId;
        this.questionId = questionId;
        this.answerId = answerId;
    }

    public long getCourseId() {
        return this.courseId;
    }

    public String getQuestionId() {
        return this.questionId;
    }

    public String getAnswerId() {
        return this.answerId;
    }

    public long getAnswered() {
        return this.answered.getTime();
    }

    public Date getAnsweredDate() {
        return this.answered;
    }

    public void setAnswered() {
        this.answered = new Date();
    }

    public void setAnswered(final long answered) {
        this.answered = new Date(answered);
    }

    public void setAnswered(final Date answered) {
        this.answered = answered != null ? answered : new Date(0);
    }
}
