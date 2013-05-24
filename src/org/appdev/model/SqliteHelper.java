package org.appdev.model;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.appdev.entity.Course;
import org.appdev.entity.Lesson;
import org.appdev.entity.Page;
import org.appdev.entity.UserInfo;

/**
 * Create database and store the user info of mLearing application
 *
 */
public class SqliteHelper extends SQLiteOpenHelper {
	

	public SqliteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	//创建表
	@Override
	public void onCreate(SQLiteDatabase db) {
		
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
				CourseAdapter.VISITED + " DATE," +
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
	        Log.e("Database","onCreate");
	}
	//更新表
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + UserInfo.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Course.TABLE_NAME);
		//db.execSQL("DROP TABLE IF EXISTS " + Class.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Lesson.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Page.TABLE_NAME);
		
        onCreate(db);
        Log.w("Database","Upgrading database from version " + oldVersion + " to " + newVersion +", which will destroy all old data.");
	}
	//更新列
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
    }

}
