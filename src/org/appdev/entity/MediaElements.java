package org.appdev.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class MediaElements extends Entity{

	private String lesson_id;
	private String course_id;
	private int visited;
	private String last_visited;
	private String resource_thumbnail;
	
	private ArrayList<Media> elementsList = new ArrayList<Media>();
	
	public void addElement(Media element){
		elementsList.add(element);
	}	
	
	public ArrayList<Media> getElements(){		
		return this.elementsList;
	}
	
	
	public String getLessonId(){
		return lesson_id;		
	}
	
	public void setLessonId(String id){
		this.lesson_id=id;
	}
	
	public String getCourseId(){
		return course_id;
		
	}
	
	public void setCourseId(String id){
		this.course_id=id;
	}
	
	
	public int getVisited(){
		return this.visited;
	}
	
	public void setVisited(int visited){
		this.visited = visited;
	}
	
	public void setLastVisited(String visited){
		this.last_visited = visited;
	}
	
	public String getLastVisited(){
		return this.last_visited;
	}

	public String getThumbnailResource() {
		return resource_thumbnail;
	}

	public void setThumbnailResource(String resource_thumbnail) {
		this.resource_thumbnail = resource_thumbnail;
	}
	
}
