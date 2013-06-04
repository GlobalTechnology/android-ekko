package org.appdev.entity;

import java.io.Serializable;

/**
 * Data model for answer for the question
 */
public class Answer extends Entity {
  private final String answerText;
  private final boolean correct;
  private boolean answered;

  public Answer(String answerText, boolean correct) {
    this.answerText = answerText;
    this.correct = correct;
  }

  public String getAnswerText() {
    return answerText;
  }

  public boolean isCorrect() {
    return correct;
  }


  public boolean isAnswered() {
    return answered;
  }

  public void setAnswered(boolean answered) {
    this.answered = answered;
  }


}
