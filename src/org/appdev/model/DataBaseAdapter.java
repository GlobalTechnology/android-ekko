package org.appdev.model;


import org.appdev.entity.Course;
import org.appdev.entity.Lesson;
import org.appdev.entity.Page;
import org.appdev.entity.UserInfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * Create database and store the user info of mLearing application
 *
 */
public abstract class DataBaseAdapter {
	
	protected static final String TAG = "mLearningDatabaseAdapter";
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase dbInstance;
	
	protected final Context context;
	
	protected static final String DATABASE_NAME = "mLearning.db";
	protected static final int DATABASE_VERSION = 1;
	
	protected static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			//create Users table
			db.execSQL("CREATE TABLE IF NOT EXISTS "+
		                UserInfo.TABLE_NAME +"("+
		                UserInfo.ID+" integer primary key,"+
		                UserInfo.USERID+" varchar,"+
		                UserInfo.TOKEN+" varchar,"+
		                UserInfo.TOKENSECRET+" varchar,"+
		                UserInfo.USERNAME+" varchar,"+
		                UserInfo.USERICON+" blob"+
		                ")"
		                );
			
			//Create Course table
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					CourseAdapter.TABLE_NAME +" (" + 
					CourseAdapter.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
					CourseAdapter.NAME + " varchar," +
					CourseAdapter.SCHOOL + " varchar," +
					CourseAdapter.DIR_NAME + " varchar," +
					CourseAdapter.COURSE_GUID + " varchar," +
					CourseAdapter.COURSE_VERSION + " varchar," +
					CourseAdapter.VISITED + " long," +
					CourseAdapter.COURSE_ICON + " blob" +
					 ");"
					);

			//Create Lesson table
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					Lesson.TABLE_NAME +" (" + 
					Lesson.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
					Lesson.COURSE_ID + " INTEGER);"
					);
			
			//Create Page table
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					Page.TABLE_NAME +" (" + 
					Page.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
					Page.COURSE_ID + " INTEGER," +
					Page.LESSON_ID + " INTEGER," +
					Page.LAST + " DATE," +
					Page.TEXT_BOTT + " TEXT," +
					Page.TEXT_TOP + " TEXT, " +
					Page.PIC + " VARCHAR, " +
					Page.VIDEO + " VARCHAR);"
					
					);
		        Log.e(TAG,"onCreate");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + UserInfo.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Course.TABLE_NAME);
			//db.execSQL("DROP TABLE IF EXISTS " + Class.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Lesson.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Page.TABLE_NAME);
			
	        onCreate(db);
	        Log.w(TAG,"Upgrading database from version " + oldVersion + " to " + newVersion +", which will destroy all old data.");
			
		}
		
/*		//更新列
		public void updateColumn(SQLiteDatabase db, String oldColumn, String newColumn, String typeColumn){
	        try{
	            db.execSQL("ALTER TABLE " +
	                    UserInfo.TABLE_NAME + " CHANGE " +
	                    oldColumn + " "+ newColumn +
	                    " " + typeColumn
	            );
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	    }*/
	}

	public DataBaseAdapter(Context context) {
		this.context = context;
	}



    public DataBaseAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        dbInstance = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbInstance.close();
    }


}
