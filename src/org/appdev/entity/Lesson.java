package org.appdev.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.appdev.app.AppContext;

public class Lesson extends Entity{
	
	/**
	 * Lesson class
	 */

	public static final String ID = "_id";
	public static final String NAME = "lesson_name";
	public static final String VISITED = "visited";
	public static final String LAST	= "last_visited";
	public static final String COURSE_ID = "course_id";
	public static final String TABLE_NAME = "lessons";	
	
	private String guid;
	private String lesson_title;
	private int visited;
	private String last_visited;
	private String course_id;
	private int textPagerIndex = 0;
	private int textPagerProgressIndex =0;
	
	private TextElements pagedTextList;
	
	private MediaElements lessonMedia;
	
	public Lesson(){
		
	}
	
	public static Lesson getNumbLesson(){
		Lesson lesson = null;
		lesson = new Lesson();
		lesson.setLesson_title("no content");
		
		//add the text elements
		TextElements textElement = new TextElements();
		textElement.addElement("Empty content!");
		lesson.setPagedTextList(textElement);
		
		//add default media element
		MediaElements mediaElement = new MediaElements();
		Media media = new Media();
		media.setMediaThumbnailID(""); //to do: add a default media later
		mediaElement.addElement(media);
		return lesson;
	}
	
	
	public Lesson(TextElements pagedText, MediaElements lessonMedia){
		this.pagedTextList = pagedText;
		this.setLessonMedia(lessonMedia);		
		
	}
	
	public void addText(String text){
		
		this.pagedTextList.addElement(text);
	}
	
	public void addMedia(Media resource) {
		this.lessonMedia.addElement(resource);
	}
	
	
	public String getCourseId(){
		return this.course_id;
	}
	
	public void setCourseId(String course_id){
		this.course_id = course_id;
	}
	
	public int getVisited(){
		return visited;
	}
	
	public void setVisited(int visited){
		this.visited = visited;
	}
	
	public String getLastVisited(){
		return this.last_visited;
	}
	
	public void setLastVisited(String last_visited){
		this.last_visited = last_visited;
	}


	public TextElements getPagedTextList() {
		return pagedTextList;
	}

	public void setPagedTextList(TextElements pagedTextList) {
		this.pagedTextList = pagedTextList;
	}

	public String getLesson_title() {
		return lesson_title;
	}

	public void setLesson_title(String lesson_title) {
		this.lesson_title = lesson_title;
	}

	public MediaElements getLessonMedia() {
		return lessonMedia;
	}

	public void setLessonMedia(MediaElements lessonMedia) {
		this.lessonMedia = lessonMedia;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public int getTextPagerIndex() {
		return textPagerIndex;
	}

	public void setTextPagerIndex(int textPagerIndex) {
		this.textPagerIndex = textPagerIndex;
	}

	public int getTextPagerProgressIndex() {
		return textPagerProgressIndex;
	}

	public void setTextPagerProgressIndex(int textPagerProgressIndex) {
		this.textPagerProgressIndex = textPagerProgressIndex;
	}

}
