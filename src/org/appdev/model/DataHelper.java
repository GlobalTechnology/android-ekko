package org.appdev.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.appdev.entity.Course;
import org.appdev.entity.UserInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DataHelper {
    //���ݿ�����
    private static String DB_NAME = "myLearning.db";
    //���ݿ�汾
    private static int DB_VERSION = 2;
    private SQLiteDatabase db;
    private SqliteHelper dbHelper;
    
    
    public DataHelper(Context context){
        dbHelper=new SqliteHelper(context,DB_NAME, null, DB_VERSION);
        db= dbHelper.getWritableDatabase();
    }
    
    public void Close()
    {
        db.close();
        dbHelper.close();
    }
    //get the UserID��Access Token��Access Secret record from the users table
    public List<UserInfo> GetUserList(Boolean isSimple)
    {
        List<UserInfo> userList = new ArrayList<UserInfo>();
        Cursor cursor=db.query(UserInfo.TABLE_NAME, null, null, null, null, null, UserInfo.ID+" DESC");
        cursor.moveToFirst();
        while(!cursor.isAfterLast()&& (cursor.getString(1)!=null)){
            UserInfo user=new UserInfo();
            user.setId(cursor.getString(0));
            user.setUserId(cursor.getString(1));
            user.setToken(cursor.getString(2));
            user.setTokenSecret(cursor.getString(3));
            if(!isSimple){
            user.setUserName(cursor.getString(4));
            ByteArrayInputStream stream = new ByteArrayInputStream(cursor.getBlob(5)); 
            Drawable icon= Drawable.createFromStream(stream, "image");
            user.setUserIcon(icon);
            }
            userList.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        return userList;
    }
    
    //�ж�users���е��Ƿ����ĳ��UserID�ļ�¼
    public Boolean HaveUserInfo(String UserId)
    {
        Boolean b=false;
        Cursor cursor=db.query(UserInfo.TABLE_NAME, null, UserInfo.USERID + "=" + UserId, null, null, null,null);
        b=cursor.moveToFirst();
       
        Log.e("HaveUserInfo",b.toString());
       
        cursor.close();
        return b;
    }
    
    //����users��ļ�¼������UserId�����û��ǳƺ��û�ͼ��
    public int UpdateUserInfo(String userName,Bitmap userIcon,String UserId)
    {
        ContentValues values = new ContentValues();
        values.put(UserInfo.USERNAME, userName);
        // BLOB����  
        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        // ��Bitmapѹ����PNG���룬����Ϊ100%�洢          
        userIcon.compress(Bitmap.CompressFormat.PNG, 100, os);   
        // ����SQLite��Content��������Ҳ����ʹ��raw  
        values.put(UserInfo.USERICON, os.toByteArray());
        int id= db.update(UserInfo.TABLE_NAME, values, UserInfo.USERID + "=" + UserId, null);
        Log.e("UpdateUserInfo2",id+"");
        return id;
    }
    
    //����users��ļ�¼
    public int UpdateUserInfo(UserInfo user)
    {
        ContentValues values = new ContentValues();
        values.put(UserInfo.USERID, user.getUserId());
        values.put(UserInfo.TOKEN, user.getToken());
        values.put(UserInfo.TOKENSECRET, user.getTokenSecret());
        int id= db.update(UserInfo.TABLE_NAME, values, UserInfo.USERID + "=" + user.getUserId(), null);
        Log.e("UpdateUserInfo",id+"");
       
        return id;
    }
    
    //���users��ļ�¼
    public Long SaveUserInfo(UserInfo user)
    {
        ContentValues values = new ContentValues();
        values.put(UserInfo.USERID, user.getUserId());
        values.put(UserInfo.TOKEN, user.getToken());
        values.put(UserInfo.TOKENSECRET, user.getTokenSecret());
        Long uid = db.insert(UserInfo.TABLE_NAME, UserInfo.ID, values);
        Log.e("SaveUserInfo",uid+"");
        return uid;
    }
    
    //ɾ��users��ļ�¼
    public int DelUserInfo(String UserId){
        int id=  db.delete(UserInfo.TABLE_NAME, UserInfo.USERID +"="+UserId, null);
        Log.e("DelUserInfo",id+"");
        return id;
    }
    
    // get course list
    public List<CourseAdapter> GetCourseList(Boolean isSimple)
    {
        List<CourseAdapter> courseList = new ArrayList<CourseAdapter>();
        Cursor cursor=db.query(Course.TABLE_NAME, null, null, null, null, null, Course.ID+" DESC");
        cursor.moveToFirst();
        while(!cursor.isAfterLast()&& (cursor.getString(1)!=null)){
            CourseAdapter course=new CourseAdapter(null);
            course.setId(cursor.getInt(0));
            course.setCourseName(cursor.getString(1));   
            course.setSchoolName(cursor.getString(2));
           // course.setVisited(cursor.getString(3));
       
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
    
    //save course to db
    public Long SaveCourse(CourseAdapter course, Bitmap courseIcon)
    {
        ContentValues values = new ContentValues();
        values.put(CourseAdapter.NAME, course.getCourseName());
        values.put(CourseAdapter.SCHOOL, course.getSchoolName());
        values.put(Course.DIR_NAME, course.getDirName());
        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        if (courseIcon != null) {
	        // ��Bitmapѹ����PNG���룬����Ϊ100%�洢          
	        courseIcon.compress(Bitmap.CompressFormat.PNG, 100, os);   
	        // ����SQLite��Content��������Ҳ����ʹ��raw  
	        values.put(CourseAdapter.COURSE_ICON, os.toByteArray());
        }
    
        Long course_id = db.insert(Course.TABLE_NAME, null, values);
        Log.e("SaveUserInfo",course_id+"");
        return course_id;
    }
}

