package org.appdev.entity;

public abstract class CourseContent extends Entity {
	public static enum COURSE_CONTENT_TYPE{
		TYPE_LESSON, TYPE_QUIZ
	};
	
	public boolean isLesson(CourseContent content){
	
		return (content instanceof Lesson);
	
	}
	
	public boolean isQuiz(CourseContent content){
		
		return (content instanceof Quiz);
	
	}

}
