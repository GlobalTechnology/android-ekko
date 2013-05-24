package org.appdev.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Course extends Entity {
	
	public static final String ID = "_id";
	public static final String NAME = "course_name";
	public static final String VISITED = "visited";

	public static final String DIR_NAME ="dir_name";
	public static final String COURSE_BANNER = "course_banner";
	public static final String TABLE_NAME = "courses";
	
	private int id;
	private String course_title;
	private String course_version;
	private String course_banner;
	private String course_description;
	private String course_copyright;
	private String course_uri;
	private String course_zipuri;
	
	private String author_name;
	private String author_email;
	private String author_url;
	

	private String visited;
	private String dir_name;
	private int lessonIndex=0;
	
	private String course_guid;
	private HashMap<String, Resource> resourceMap;
	
	private ArrayList<Lesson> lessonList;
	
	public void addLesson(Lesson lesson) {
		this.lessonList.add(lesson);
	}
	
	public ArrayList<Lesson> getLessonList(){
		return this.lessonList;
	}
	
	public void setLessonList(ArrayList<Lesson> lessonList) {
		this.lessonList = lessonList;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id){
		this.id =id;
	}
	

	public Resource getResource(String resourceId) {
		return resourceMap.get(resourceId);
	}
	public String getVisited(){
		return visited;
	}
	
	public void setVisited(String visited){
		this.visited = visited;
	}	


	public void setDirName(String dir_name) {
		// TODO Auto-generated method stub
		this.dir_name = dir_name;		
	}
	
	public String getDirName() {
		return dir_name;
	}

	public String getCourseTitle() {
		return course_title;
	}

	public void setCourseTitle(String course_title) {
		this.course_title = course_title;
	}

	public String getCourseVersion() {
		return course_version;
	}

	public void setCourseVersion(String course_version) {
		this.course_version = course_version;
	}

	public String getCourseBanner() {
		return course_banner;
	}

	public void setCourseBanner(String course_banner) {
		this.course_banner = course_banner;
	}

	public String getCourseDescription() {
		return course_description;
	}

	public void setCourseDescription(String course_description) {
		this.course_description = course_description;
	}

	public String getCourseCopyright() {
		return course_copyright;
	}

	public void setCourseCopyright(String course_copyright) {
		this.course_copyright = course_copyright;
	}

	public String getAuthorName() {
		return author_name;
	}

	public void setAuthorName(String author_name) {
		this.author_name = author_name;
	}

	public String getAuthorEmail() {
		return author_email;
	}

	public void setAuthorEmail(String author_email) {
		this.author_email = author_email;
	}

	public String getAuthorUrl() {
		return author_url;
	}

	public void setAuthorUrl(String author_url) {
		this.author_url = author_url;
	}

	public String getCourseGUID() {
		return course_guid;
	}

	public void setCourseGUID(String course_guid) {
		this.course_guid = course_guid;
	}

	public HashMap<String, Resource> getResourceMap() {
		return resourceMap;
	}

	public void setResourceMap(HashMap<String, Resource> resourceMap) {
		this.resourceMap = resourceMap;
	}

	public int getLessonIndex() {
		return lessonIndex;
	}

	public void setLessonIndex(int lessonIndex) {
		this.lessonIndex = lessonIndex;
	}

	public String getCourseURI() {
		return course_uri;
	}

	public void setCourseURI(String course_uri) {
		this.course_uri = course_uri;
	}

	public String getCourseZipUri() {
		return course_zipuri;
	}

	public void setCourseZipUri(String course_zipuri) {
		this.course_zipuri = course_zipuri;
	}

}
