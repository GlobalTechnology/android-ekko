package org.appdev.entity;

import java.util.HashMap;

public class Page extends Entity{
	
	public static final String ID = "_id";
	public static final String TITLE = "page_title";
	public static final String VISITED = "visited";
	public static final String LAST	= "last_visited";
	public static final String LESSON_ID = "lesson_id";
	public static final String COURSE_ID = "course_id";
	public static final String TEXT_TOP = "text_top";
	public static final String PIC = "picture";
	public static final String VIDEO = "video";
	public static final String TEXT_BOTT = "text_bottom";
	public static final String TABLE_NAME = "pages";	
	
	private int id;
	private String page_title;
	private String visited;
	private String last_visited;
	private int lesson_id;
	private int course_id;
	private String text_top;
	private String text_bott;
	private String video_url;
	private String pic_url;
	
	private HashMap<String, String> elementsMap = new HashMap<String,String>();
	
	public void addElement(String key, String element){
		elementsMap.put(key, element);
	}	
	
	public HashMap<String, String> getElements(){		
		return this.elementsMap;
	}
	
	public int getId(){
		return id;		
	}
	
	public void setId(int id){
		this.id=id;
	}
	
	public int getLessonId(){
		return lesson_id;
		
	}
	
	public void setLessonId(int id){
		this.lesson_id=id;
	}
	
	public int getCourseId(){
		return course_id;
		
	}
	
	public void setCourseId(int id){
		this.course_id=id;
	}
	
	
	public String getPageTitle(){
		return this.page_title;
	}
	
	public void setPageTitle(String page_title){
		this.page_title = page_title;
	}
	
	public String getVisited(){
		return this.visited;
	}
	
	public void setVisited(String visited){
		this.visited = visited;
	}
	
	public void setLastVisited(String visited){
		this.last_visited = visited;
	}
	
	public String getLastVisited(){
		return this.last_visited;
	}
	
	public void setTextTop(String text){
		this.text_top = text;
	}
	public String getTextTop(){
		return this.text_top;
	}
	
	public void setTextBott(String text){
		this.text_bott = text;
	}
	public String getTextBott(){
		return this.text_bott;
	}
	
	public void setPicURL(String path){
		this.pic_url = path;
	}
	public String getPicURL(){
		return this.pic_url;
	}
	
	public void setVideoURL(String path){
		this.video_url = path;
	}
	public String getVideoURL(){
		return this.video_url;
	}
	

}
