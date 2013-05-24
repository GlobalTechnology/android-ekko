package org.appdev.utils;

import java.util.ArrayList;
import java.util.List;

import org.appdev.model.CourseAdapter;

import android.content.Context;
import android.database.Cursor;

public class CourseImport {

	//private List<CourseAdapter> courseList;
	private Context context;

	CourseImport(Context context){
		this.context = context;
	}

/*	public boolean addCourse(Cursor cursor) {
		
		if(cursor == null) {
			return false;
		}
	
		CourseAdapter courseDB = new CourseAdapter(context);
		courseDB.open();	
	
		courseDB.create(cursor.getString(cursor.getColumnIndex(CourseAdapter.NAME)), 
				cursor.getString(cursor.getColumnIndex(CourseAdapter.COURSE_VERSION)),
				cursor.getString(cursor.getColumnIndex(CourseAdapter.SCHOOL)), 
				cursor.getString(cursor.getColumnIndex(CourseAdapter.COURSE_GUID)), 
				cursor.getLong(cursor.getColumnIndex(CourseAdapter.VISITED)), 
				null);
		
		courseDB.close();
		
		return true ;
	}*/
	
	

	
	
}
