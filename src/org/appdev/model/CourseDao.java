package org.appdev.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.appdev.entity.Lesson;


public class CourseDao extends DataBaseAdapter {
	
	public CourseDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static final String ID = "_id";
	public static final String NAME = "course_name";
	public static final String VISITED = "visited";
	public static final String CATEGORY = "category";
	public static final String DIR_NAME ="dir_name";
	public static final String COURSE_ICON = "course_icon";
	public static final String COURSE_GUID = "course_guid";
	public static final String COURSE_VERSION = "course_version";
	public static final String TABLE_NAME = "courses";
	
	private int id;
	private String course_name;
	private long visited;
	private String school_name;
	private Drawable course_icon;
	private String dir_name;
	private String course_guid;
	private String course_version;

	
	private ArrayList<Lesson> lessonList= new ArrayList<Lesson>();
	
	public void addLesson(Lesson lesson) {
		this.lessonList.add(lesson);
	}
	
	public ArrayList<Lesson> getLessonList(){
		return this.lessonList;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id){
		this.id =id;
	}
	
	public String getCourseName(){
		return this.course_name;
	}
	
	public void setCourseName(String course_name){
		this.course_name = course_name;
	}
	
	public long getVisited(){
		return visited;
	}
	
	public void setVisited(long visited){
		this.visited = visited;
	}
	
	public String getSchoolName(){
		return this.school_name;
	}
	
	public void setSchoolName(String school_name){
		this.school_name = school_name;
	}

	public void setCourseIcon(Drawable icon) {
		// TODO Auto-generated method stub
		this.course_icon = icon;		
	}
	
	public Drawable getCourseIcon() {
		return course_icon;
	}
	

	public void setDirName(String dir_name) {
		// TODO Auto-generated method stub
		this.dir_name = dir_name;		
	}
	
	public String getDirName() {
		return dir_name;
	}

	public String getCourseGUID() {
		return course_guid;
	}

	public void setCourseGUID(String course_guid) {
		this.course_guid = course_guid;
	}

	public String getCourseVersion() {
		return course_version;
	}

	public void setCourseVersion(String course_version) {
		this.course_version = course_version;
	}	
	
    
    // get course list
    public List<CourseDao> getCourseList(Boolean isSimple)
    {
        List<CourseDao> courseList = new ArrayList<CourseDao>();
        Cursor cursor=dbInstance.query(CourseDao.TABLE_NAME, null, null, null, null, null, ID+" DESC");
        if (cursor.moveToFirst() == false) {
        	return null;
        }
        while(!cursor.isAfterLast()&& (cursor.getString(1)!=null)){
            CourseDao course=new CourseDao(this.context);
            course.setId(cursor.getInt(0));
            course.setCourseName(cursor.getString(cursor.getColumnIndex(CourseDao.NAME)));   
            course.setSchoolName(cursor.getString(cursor.getColumnIndex(CourseDao.CATEGORY)));
            course.setVisited(cursor.getLong(cursor.getColumnIndex(CourseDao.VISITED)));
            course.setCourseGUID(cursor.getString(cursor.getColumnIndex(CourseDao.COURSE_GUID)));
            course.setCourseVersion(cursor.getString(cursor.getColumnIndex(CourseDao.COURSE_VERSION)));
            
       
            if(!isSimple){
            course.setCourseName(cursor.getString(4));
            ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(5)); 
            Drawable icon= Drawable.createFromStream(stream, "image");
            course.setCourseIcon(icon);
            }
            courseList.add(course);
            cursor.moveToNext();
        }
        cursor.close();
        return courseList;
    }
    
    
    public long create(String name, String version, String category, String guid, long visited, Bitmap courseIcon) {
    	ContentValues values = new ContentValues();

        values.put(NAME, name);
        values.put(CATEGORY, category ); 
        values.put(COURSE_GUID, guid);
        values.put(COURSE_VERSION, version);
        values.put(VISITED, visited);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        if (courseIcon != null) {
	        // 将Bitmap压缩成PNG编码，质量为100%存储          
	        courseIcon.compress(Bitmap.CompressFormat.PNG, 100, os);   
	        // 构造SQLite的Content对象，这里也可以使用raw  
	        values.put(CourseDao.COURSE_ICON, os.toByteArray());
        }
    	
    	return dbInstance.insert(TABLE_NAME, null,values);
    }
    //save course to db
    public Long saveCourse(CourseDao course, Bitmap courseIcon)
    {
        ContentValues values = new ContentValues();
        values.put(NAME, course.getCourseName());
        values.put(CATEGORY, course.getSchoolName());
        values.put(DIR_NAME, course.getDirName());
        values.put(COURSE_GUID, course.getCourseGUID());
        values.put(COURSE_VERSION, course.getCourseVersion());
        values.put(VISITED, course.getVisited());
        
        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        if (courseIcon != null) {
	        // 将Bitmap压缩成PNG编码，质量为100%存储          
	        courseIcon.compress(Bitmap.CompressFormat.PNG, 100, os);   
	        // 构造SQLite的Content对象，这里也可以使用raw  
	        values.put(COURSE_ICON, os.toByteArray());
        }
    
        Long course_id = dbInstance.insert(TABLE_NAME, null, values);
        Log.e("SaveCourseInfo",course_id+"");
        return course_id;
    }
    
    public Cursor fetch(long rowId) throws SQLException {
        Cursor mCursor =
            dbInstance.query(true, TABLE_NAME, new String[] {ID,
                   NAME, VISITED, COURSE_ICON, COURSE_GUID, CATEGORY, COURSE_VERSION}, ID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchByGUID(String guid) throws SQLException {
        Cursor mCursor =
            dbInstance.query(true, TABLE_NAME, new String[] {ID,
                   NAME, VISITED, COURSE_ICON, COURSE_GUID, CATEGORY, COURSE_VERSION}, COURSE_GUID + "=\"" + guid + "\"", null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchAll() {
    	return dbInstance.query(TABLE_NAME, new String[] {ID,
                NAME, VISITED, COURSE_ICON, COURSE_GUID, CATEGORY, COURSE_VERSION}, null, null, null, null, null);
    }
    
    public boolean delete(long rowId) {
    	return dbInstance.delete(TABLE_NAME, ID + "=" + rowId, null) > 0;
    }

}
