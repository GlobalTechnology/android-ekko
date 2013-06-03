package org.appdev.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.appdev.R;
import org.appdev.api.ApiClient;
import org.appdev.entity.Course;
import org.appdev.entity.CourseList;
import org.appdev.entity.CourseManifest;
import org.appdev.entity.Lesson;
import org.appdev.entity.Media;
import org.appdev.entity.Resource;
import org.appdev.entity.TextElements;
import org.appdev.entity.User;
import org.appdev.utils.FileUtils;
import org.appdev.utils.ImageUtils;
import org.appdev.utils.MethodsCompat;
import org.appdev.utils.StringUtils;
import org.appdev.utils.UIController;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Global Application class:
 *  used to save and call global application configuration and access network data
 */
public class AppContext extends Application {
	private static AppContext instance;
	public static boolean production = true;
	
	private static String sessionID;
	
	private static Course curCourse = null;
	private static Course preCourse = null;
	
	public static final String COURSE_MANIFEST_FILE = "manifest.xml";
	public static final String COURSE_OBJECT_FILE = FileUtils.EkkoFilePath() + "curCourse.obj";

	
	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	public static final int PAGE_SIZE = 10;//Default page size
	private static final int CACHE_TIME = 60*60000;//cache time
	
	private boolean login = false;	//login status
	private int loginUid = 0;	//login user ID
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
	
	private Handler unLoginHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == 1){
				UIController.ToastMessage(AppContext.this, getString(R.string.msg_login_error));
				UIController.showLoginDialog(AppContext.this);
			}
		}		
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
        //Register application exception handler
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
	}
	
	public static synchronized AppContext getInstance() {
		if(instance == null){
			instance = new AppContext();
		}
		return instance;
	}

	/**
	 * Check the audio is in normal mode
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE); 
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}
	
	/**
	 * Application is sound
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}
	
	/**
	 * check network is available
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * get the network type
	 * @return 0：no network   1：WIFI   2：WAP    3：NET
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
	
	/**
	 * Judge the current SDK version is compatible with the target version
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}
	
	/**
	 * Get the app package info
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try { 
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
		if(info == null) info = new PackageInfo();
		return info;
	}
	
	/**
	 * Get a unique App ID
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if(StringUtils.isEmpty(uniqueID)){
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}
	
	/**
	 * check if user logged in
	 * @return
	 */
	public boolean isLogin() {
		return login;
	}
	
	/**
	 * Get login user ID
	 * @return
	 */
	public int getLoginUid() {
		return this.loginUid;
	}
	
	/**
	 * Logout
	 */
	public void Logout() {
		ApiClient.cleanCookie();
		AppContext.sessionID = null;
		this.cleanCookie();
		this.login = false;
		this.loginUid = 0;
	}
	
	/**
	 * Handler for unlogin
	 */
	public Handler getUnLoginHandler() {
		return this.unLoginHandler;
	}
	
	/**
	 * Initialize user login info
	 */
	public void initLoginInfo() {
		User loginUser = getLoginInfo();
		if(loginUser!=null && !StringUtils.isEmpty(loginUser.getSessionId())&& loginUser.isRememberMe()){
			this.loginUid = loginUser.getUid();
			this.login = true;
		}else{
			this.Logout();
		}
	}
	
	/**
	 * User login verify
	 * @param account
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	public User loginVerify(String account, String pwd) throws AppException {
		return ApiClient.login(this, account, pwd);
	}

	
	
	/**
	 * Save Login info
	 * @param username
	 * @param pwd
	 */
	public void saveLoginInfo(final User user) {
		this.loginUid = user.getUid();
		this.login = true;
		setProperties(new Properties(){{
			setProperty("user.uid", String.valueOf(user.getUid()));
			setProperty("user.name", user.getName());
			setProperty("user.face", FileUtils.getFileName(user.getFace()));//User Pic-file
			setProperty("user.account", user.getAccount());
			
			setProperty("user.isRememberMe", String.valueOf(user.isRememberMe()));//是否记住我的信息
		}});		
	}
	
	/**
	 * clean login info
	 */
	public void cleanLoginInfo() {
		this.loginUid = 0;
		this.login = false;
		removeProperty("user.uid","user.name","user.face","user.account",
				"user.isRememberMe");
	}
	
	/**
	 * Get Login info
	 * @return
	 */
	public User getLoginInfo() {		
		User lu = new User();		
		lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
		lu.setName(getProperty("user.name"));
		lu.setFace(getProperty("user.face"));
		lu.setAccount(getProperty("user.account"));		

		lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
		return lu;
	}
	
	/**
	 * Save user pic
	 * @param fileName
	 * @param bitmap
	 */
	public void saveUserFace(String fileName,Bitmap bitmap) {
		try {
			ImageUtils.saveImage(this, fileName, bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get user pic
	 * @param key
	 * @return
	 * @throws AppException
	 */
	public Bitmap getUserFace(String key) throws AppException {
		FileInputStream fis = null;
		try{
			fis = openFileInput(key);
			return BitmapFactory.decodeStream(fis);
		}catch(Exception e){
			throw AppException.run(e);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * if displaying picture
	 * @return
	 */
	public boolean isDisplayImage()
	{
		String perf_displayimage = getProperty(AppConfig.CONF_DISPLAY_IMAGE);
		//don't display picture as default
		if(StringUtils.isEmpty(perf_displayimage))
			return false;
		else
			return StringUtils.toBool(perf_displayimage);
	}
	
	/**
	 * set if displaying pic
	 * @param b
	 */
	public void setConfigLoadimage(boolean b)
	{
		setProperty(AppConfig.CONF_DISPLAY_IMAGE, String.valueOf(b));
	}
	
	/**
	 * play promoting sound
	 * @return
	 */
	public boolean isVoice()
	{
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		//play sound as default
		if(StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}
	
	/**
	 * set if play voice
	 * @param b
	 */
	public void setConfigVoice(boolean b)
	{
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}
	
	/**
	 * set if enabling version check up
	 * @return
	 */
	public boolean isCheckUp()
	{
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		//Enabling as default
		if(StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}
	
	/**
	 * set booting version checkup
	 * @param b
	 */
	public void setConfigCheckUp(boolean b)
	{
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}
	
	/**
	 * slide? left-right
	 * @return
	 */
	public boolean isScroll()
	{
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		//disable left-right scroll as default
		if(StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}
	
	/**
	 * Set left-right scroll
	 * @param b
	 */
	public void setConfigScroll(boolean b)
	{
		setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
	}
	
	/**
	 * Used for Production or development
	 * @return
	 */
	public boolean isProductionEnv()
	{
		String perf_production = getProperty(AppConfig.CONF_PRODUCTION_ENV);
		
		if(StringUtils.isEmpty(perf_production))
			return false;
		else
			return StringUtils.toBool(perf_production);
	}
	
	/**
	 * set Production Env
	 * @param b
	 */
	public void setConfigProductionEnv(boolean b)
	{
		setProperty(AppConfig.CONF_PRODUCTION_ENV, String.valueOf(b));
	}
	
	/**
	 * clean cache
	 */
	public void cleanCookie()
	{
		removeProperty(AppConfig.CONF_COOKIE);
	}
	
	/**
	 * determine the cached data is readable
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile)
	{
		return readObject(cachefile) != null;
	}
	
	/**
	 * Determine if the cached file exsits
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile)
	{
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists())
			exist = true;
		return exist;
	}
	
	/**
	 * Determine the cached file is still effective
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile)
	{
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists() && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if(!data.exists())
			failure = true;
		return failure;
	}
	
	/**
	 * clear App Cache
	 */
	public void clearAppCache()
	{
		//clean webview cache
		deleteDatabase("webview.db");  
		deleteDatabase("webview.db-shm");  
		deleteDatabase("webview.db-wal");  
		deleteDatabase("webviewCache.db");  
		deleteDatabase("webviewCache.db-shm");  
		deleteDatabase("webviewCache.db-wal");  
		//clean data cache
		clearCacheFolder(getFilesDir(),System.currentTimeMillis());
		clearCacheFolder(getCacheDir(),System.currentTimeMillis());
		//2.2版本才有将应用缓存转移到sd卡的功能
		if(isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)){
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),System.currentTimeMillis());
		}
		//clean the temp content editor saved
		Properties props = getProperties();
		for(Object key : props.keySet()) {
			String _key = key.toString();
			if(_key.startsWith("temp"))
				removeProperty(_key);
		}
	}	
	
	/**
	 * Clean cache folder
	 * @param dir 
	 * @param numDays
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {          
	    int deletedFiles = 0;         
	    if (dir!= null && dir.isDirectory()) {             
	        try {                
	            for (File child:dir.listFiles()) {    
	                if (child.isDirectory()) {              
	                    deletedFiles += clearCacheFolder(child, curTime);          
	                }  
	                if (child.lastModified() < curTime) {     
	                    if (child.delete()) {                   
	                        deletedFiles++;           
	                    }    
	                }    
	            }             
	        } catch(Exception e) {       
	            e.printStackTrace();    
	        }     
	    }       
	    return deletedFiles;     
	}
	
	/**
	 * Save object to be cached in memory
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}
	
	/**
	 * Get the object from cache memory 
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key){
		return memCacheRegion.get(key);
	}
	
	/**
	 * Set native disk cache
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try{
			fos = openFileOutput("cache_"+key+".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Get cache data from Disk
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try{
			fis = openFileInput("cache_"+key+".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Save object data
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
			} catch (Exception e) {}
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Read Object
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file){
		if(!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
			//If fail to deserialize, delete the cached file 
			if(e instanceof InvalidClassException){
				File data = getFileStreamPath(file);
				data.delete();
			}
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}

	public boolean containsProperty(String key){
		Properties props = getProperties();
		 return props.containsKey(key);
	}
	
	public void setProperties(Properties ps){
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties(){
		return AppConfig.getAppConfig(this).get();
	}
	
	public void setProperty(String key,String value){
		AppConfig.getAppConfig(this).set(key, value);
	}
	
	public String getProperty(String key){
		return AppConfig.getAppConfig(this).get(key);
	}
	public void removeProperty(String...key){
		AppConfig.getAppConfig(this).remove(key);
	}	
	
	public void saveCurCourseObject() {
		this.saveObject(AppContext.curCourse, AppContext.COURSE_OBJECT_FILE);
		
	}
	
	public Course getLastAccessCourseObject() {
		return (Course)this.readObject(AppContext.COURSE_OBJECT_FILE);
	}

	/**
	 * Get course list based on the native course package store directory
	 * @return
	 */
	public Course getNativeCourseList() {
		Course list = null;
				
		return list;
	}
	
	/**
	 * Get Course list by calling web service in the SOA hub
	 * @param catalog (placeholder for future use)
	 * @param pageIndex
	 * @param isRefresh
	 * @return
	 * @throws AppException
	 */
	public CourseList getCourseList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		CourseList list = null;
		String key = "courselist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getCourseList(AppContext.this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){		
					list.setCacheKey(key);
					saveObject(list, key);					
				}
			}catch(AppException e){
				Log.w("AppContext-getCourseList", e.toString());
				list = (CourseList)readObject(key);
				if(list == null)
					throw e;
			}		
		} else {
			list = (CourseList)readObject(key);
			if(list == null){
				//scan the courses folder to get a list
				
				list = null;
			}
		}
		return list;
	}
	
    public int getCourseVer(String courseUrl) throws AppException {
		if(isNetworkConnected()){
			try{
                return ApiClient.getCourseVer(AppContext.this, courseUrl);
			}catch(AppException e){
				Log.w("AppContext-getCourseVer", e.toString());
				
			}
		}
        return 0;
	}
	
	public static String getCourseID( String resUrl )
	{
		if( StringUtils.isEmpty(resUrl) )	return "";
		return resUrl.substring( resUrl.lastIndexOf( File.separator )+1 );
	}
	
	/**
	 * Get a course
	 * @param catalog
	 * @param pageIndex
	 * @param isRefresh
	 * @return
	 * @throws AppException
	 */
	public Course getCourse(String resUrl) throws AppException {
		if(StringUtils.isEmpty(resUrl)) return null;
		
		Course course = null;
		String key = "course_"+getCourseID(resUrl);
		if(isNetworkConnected() && (!isReadDataCache(key))) {
			course = ApiClient.getCourse(AppContext.this, resUrl);
			if(course != null){				
				course.setCacheKey(key);
				saveObject(course, key);					
			}		
		} else {
			course = (Course)readObject(key);		
		}
		return course;
	}


	public static String getSessionID() {
		return sessionID;
	}

	public static void setSessionID(String sessionID) {
		AppContext.sessionID = sessionID;
	}

	/**
	 * Get the current course
	 * @return
	 */
	public Course getCurCourse(){
		if(curCourse == null) {
			try {
				curCourse = instanceCourse(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return curCourse;
	}
	
	public Course instanceCourse(File manifestFile) throws IOException {
						
			InputStream input = null;
			Course course = null;		
			
			try {
				if(manifestFile == null ){
					input = getAssets().open(AppContext.COURSE_MANIFEST_FILE); //user guide course
				}else{
					input = new BufferedInputStream(new FileInputStream(manifestFile));
					
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
			try {
				CourseManifest mManifest = new CourseManifest();
            course = new Course(-1);
				try {
					course = CourseManifest.parse(input);
					if(course != null) {
						AppContext.setCurCourse(course);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("courseManifest", mManifest.toString());
			} catch (AppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(input != null){
					input.close();
				}
			}
					
	
		return course;
	}

	public void setCurrentLessonIndex(int lessonIndex){
		AppContext.curCourse.setLessonIndex(lessonIndex);
		//set the course lesson progress Index
		if(lessonIndex> AppContext.curCourse.getLessonProgressIndex()){
			AppContext.curCourse.setLessonProgressIndex(lessonIndex);
		}
		
	}
	
	public int  getCurrentLessonIndex(){
		
		int lessonSize = AppContext.curCourse.getLessonList().size();
		if(lessonSize<=0){
			curCourse.addLesson(Lesson.getNumbLesson()); //add numb lesson
			return 0;
		}else if(curCourse.getLessonIndex()+1>lessonSize){
			curCourse.setLessonIndex(lessonSize-1);
			return lessonSize-1;
		} else{
		
			return curCourse.getLessonIndex();
		}
		//return curCourse.getLessonIndex();
	}
	
	public static void setCurCourse(Course curCourse) {
		AppContext.curCourse = curCourse;
	}
	
	/**
	 * Get the Text pager count of the current lesson
	 * @return
	 */
	public int getCurLessonTextPagerCount(){
		Lesson lesson = getCurLesson();
		if(lesson != null && lesson.getPagedTextList() != null && lesson.getPagedTextList().getElements() != null){
			return lesson.getPagedTextList().getElements().size();
		}else{
			return -1;
		}
				
	}
	
	public List<Drawable> getCurLessonMediaList(Course course, int lessonIndex){
    	List<Drawable> lessonMedia = new ArrayList<Drawable>();
    	ArrayList<Lesson> lessonList = new ArrayList<Lesson>();
    	lessonList = course.getLessonList();
    	if(lessonList.size()<=0) return null;
    	
    	if(lessonIndex<0) {
    		lessonIndex=0;
    		Log.w("AppContex-getCurlessonMediaList", "Index is out of the boundary");
    	};
    	
    	if(lessonIndex>lessonList.size()-1) {
    		Log.w("AppContex-getCurlessonMediaList", "Index is out of the boundary");
    		lessonIndex=lessonList.size() -1;
    	}
    	    	
    	Lesson curLesson = lessonList.get(lessonIndex);
    	ArrayList<Media> lessonMediaElements= curLesson.getLessonMedia().getElements();
    	for(int i=0; i<lessonMediaElements.size(); i++) {
    		Media media = lessonMediaElements.get(i);
            Resource resource = course.getResource(media.getMediaThumbnailID());
    		if (resource == null ) {
                resource = course.getResource(media.getMediaResourceID());
    		}    		
    		
    		String imageFile = FileUtils.EkkoCourseSetRootPath() + course.getCourseGUID() + "/" +resource.getResourceFile();
    	
    		File img = new File(imageFile);
    		if (img.exists()){
    			Bitmap bitmap = ImageUtils.getBitmapByFile(img);
    			if(bitmap != null){    			    
    				Drawable pic = new BitmapDrawable(getResources(), bitmap);
		    		lessonMedia.add(pic);
		    		
		    		//added to resolve "out of memory" issue on some devices
		    		System.gc();
		    		bitmap = null;
	    		}else{    		
	    			lessonMedia.add(getResources().getDrawable(R.drawable.course_banner));
	    		} 
    			
    		} else{
    			lessonMedia.add(getResources().getDrawable(R.drawable.course_banner));
    		}
    		img = null;
    		
    	} 
    	return lessonMedia;
	}

	public static Course getPreCourse() {
		return preCourse;
	}

	public static void setPreCourse(Course preCourse) {
		AppContext.preCourse = preCourse;
	}
	
	/**
	 * get the progress status of course 
	 * @param course
	 * @return int [0,100]
	 */
	public static int getCourseProgress(Course course){
		if(course == null){
			return -1;
		}
		int progress = 0;
		int totalPageNum=0; //the pages count the course contains
		int curTotalNum=0;
		//get the total page number
		if(course.getLessonList() != null){
			for(int i=0; i< course.getLessonList().size(); i++){
				TextElements element = course.getLessonList().get(i).getPagedTextList();
				if(element != null && element.getElements()!=null){
					totalPageNum += element.getElements().size();
				}
			}
		}
	
		if (totalPageNum<=0) return -1;
		
		//calculate the total page number from the beginning to current viewed page of the lesson 
		for (int i=0; i < course.getLessonProgressIndex(); i++){
			TextElements element = course.getLessonList().get(i).getPagedTextList();
			if(element != null && element.getElements()!=null ) {
				curTotalNum += element.getElements().size();
			}
		}
		//add the page number of the last accessed lesson
		if(course.getLessonList()!=null){
			curTotalNum += course.getLessonList().get(course.getLessonProgressIndex()).getTextPagerProgressIndex() +1;
		}
		
		//calculate the progress
		progress = (int)((curTotalNum * 100.0)/totalPageNum);
		
		Log.v("Appcontext_curtotalNum", Integer.toString(curTotalNum));
		Log.v("Appcontext_totalNum", Integer.toString(totalPageNum));
		
		return progress;
	}
	
	public Lesson getCurLesson(){
		if(curCourse.getLessonList() != null){
			return curCourse.getLessonList().get(getCurrentLessonIndex());
		}else{
			return null;
		}
	}
	public static void updateCourseProgress(Course course){
		
	}

}
