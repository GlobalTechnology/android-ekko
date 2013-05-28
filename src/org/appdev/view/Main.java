package org.appdev.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.appdev.widget.NewDataToast;
import org.appdev.entity.Course;
import org.appdev.entity.CourseList;

import org.appdev.entity.Notice;
import org.appdev.utils.CoursePackageDownloaderThread;

import org.appdev.widget.PullToRefreshListView;

import org.appdev.adapter.LessonMediaPagerAdapter;
import org.appdev.adapter.LessonTextPagerAdapter;
import org.appdev.adapter.ListViewCoursesAdapter;

import org.appdev.api.SlideMenuListener;
import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.app.AppManager;


import org.appdev.R;

import org.appdev.ui.AppSlidingMenu;
import org.appdev.ui.CourseListSlidingMenu;
import org.appdev.ui.LessonListSlidingMenu;

import org.appdev.utils.FileUtils;
import org.appdev.utils.StringUtils;
import org.appdev.utils.UIController;
import org.appdev.utils.UpdateManager;
import org.appdev.widget.ScrollLayout;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import greendroid.widget.CustomizedQuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.LinePageIndicator;

/**
 * Application Main UI
 * @version 1.0
 * @created 2013-4-5
 */
public class Main extends SlidingFragmentActivity {
	
	// Used to communicate state changes in the CoursePackage DownloaderThread
	public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
	public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
	public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
	public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
	public static final int MESSAGE_CONNECTING_STARTED = 1004;
	public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
	

    public static final int QUICKACTION_LOGIN_OR_LOGOUT = 0;
    public static final int QUICKACTION_SETTING = 1;
    public static final int QUICKACTION_EXIT = 2;
    
	private Thread downloaderThread;
	private ProgressDialog progressDialog;

	private ScrollLayout mScrollLayout;
	private RadioButton[] mButtons;
	private String[] mHeadTitles;
	private int mViewCount;
	private int mCurSel;
	
	private ImageView mHeadLogo;
	private TextView mHeadTitle;
	private ProgressBar mHeadProgress;
	private ImageButton mHead_search;
	private ImageButton mHead_lessons;
	
	private int curCoursesCatalog = 0;
	
	private Handler lvCoursesHandler;	
	private ListViewCoursesAdapter lvCoursesAdapter;	
	private int lvCoursesSumData;
	
	private RadioButton fbCourses;
	private RadioButton fbCourseDetail;	
	
	private ImageView fbSetting;	
	private PullToRefreshListView lvCourses;
	
	private ViewPager vpLessonMedia;
	private ViewPager vpLessonTextPager;	
	
    private PagerAdapter mTextPagerAdapter;    
    private PagerAdapter mMediaPagerAdapter;   
    
    private LinePageIndicator mTextPagerIndicator;	
	private CirclePageIndicator mMediaPagerIndicator;
	
	private Button mLesson_next;
	private Button mLesson_prev;
	private Button mLesson_title;	

	private List<Course> lvCoursesData = new ArrayList<Course>();
	
	private View lvCourses_footer;
	private TextView lvCourses_foot_more;
	private ProgressBar lvCourses_foot_progress;
	
    private QuickActionWidget mGrid;//shortcut menu
	
	private AppContext appContext;//Global Context
		
	protected ListFragment mLessonListMenuFrag;
	protected ListFragment mCourseListMenuFrag;
	protected ListFragment mAppSettingListMenuFrag;
	
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		//Add Activity to stack
		AppManager.getAppManager().addActivity(this);
          
        appContext = (AppContext)getApplication();
        //Network connection type
        if(!appContext.isNetworkConnected())
        	UIController.ToastMessage(this, R.string.network_not_connected);
        //Init login
        appContext.initLoginInfo();
		
		this.initHeadView();
        this.initFootBar();
        this.initPageScroll();
        this.initQuickActionGrid();
        this.initFrameView();
        this.initSlidingMenu();
        
        //check new version
        if(appContext.isCheckUp()){
        	UpdateManager.getUpdateManager().checkAppUpdate(this, false);
        }        

    }    


	private void initSlidingMenu() {
		// TODO Auto-generated method stub
		 //////***********************************************
		// set the Behind View
		setBehindContentView(R.layout.menu_frame_app_setting);
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		mAppSettingListMenuFrag = new AppSlidingMenu(new SlideMenuListener(){
			
			@Override
			public void reload(){

			}

			@Override
			public void showcontent() {
				// TODO Auto-generated method stub
				showContent();
			}
		});
		t.replace(R.id.menu_frame_setting, mAppSettingListMenuFrag);
		t.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.LEFT);

		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// set the Above View
/*		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new SampleListFragment())
		.commit();
		*/
		setSlidingActionBarEnabled(false);
        
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
    @Override    
    protected void onResume() {
    	super.onResume();
    	
    	if(mViewCount == 0) mViewCount = 2;
    	if(mCurSel == 0 && !fbCourses.isChecked()) {
    		fbCourses.setChecked(true);
    		fbCourseDetail.setChecked(false);   
    	}
    	//read left-right sliding configuration
    	mScrollLayout.setIsScroll(appContext.isScroll());   
    	
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Activity finished, move it from the stack
		AppManager.getAppManager().finishActivity(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);		

	}
    
    /**
     * Init quick action menu
     */
    private void initQuickActionGrid() {
        mGrid = new QuickActionGrid(this);
        mGrid.addQuickAction(new CustomizedQuickAction(this, R.drawable.ic_menu_login, R.string.main_menu_login));
      
 //       mGrid.addQuickAction(new CustomizedQuickAction(this, R.drawable.ic_menu_search, R.string.main_menu_search));
        mGrid.addQuickAction(new CustomizedQuickAction(this, R.drawable.ic_menu_setting, R.string.main_menu_setting));
        mGrid.addQuickAction(new CustomizedQuickAction(this, R.drawable.ic_menu_exit, R.string.main_menu_exit));
        
        mGrid.setOnQuickActionClickListener(mActionListener);
    }
    
    /**
     * Quick action menu item listener
     */
    private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
    		switch (position) {
    		case QUICKACTION_LOGIN_OR_LOGOUT://User Login or logout
    			UIController.loginOrLogout(Main.this);
    			break;

    		case QUICKACTION_SETTING://Setting
    			UIController.showSetting(Main.this);
    			break;
    		case QUICKACTION_EXIT://Exit
    			UIController.Exit(Main.this);
    			break;
    		}
        }
    };
    
    /**
     * Init all the ListView
     */
    private void initFrameView()
    {
    	
    	//Init listView widget
    	this.initCoursesListView();
    	
    	//Load listview data
    	this.initFrameViewData();
    	
    	this.initCourseDetailsView();
    }

    private void initFrameViewData()
    {
        //Init Handler
        lvCoursesHandler = this.getLvHandler(lvCourses, lvCoursesAdapter, lvCourses_foot_more, lvCourses_foot_progress, AppContext.PAGE_SIZE);
        
        //Load course list
		if(lvCoursesData.isEmpty()) {
			loadLvCoursesData(curCoursesCatalog, 0, lvCoursesHandler, UIController.LISTVIEW_ACTION_INIT);
		}
    }
    /**
     * Init Courses list view
     */
    private void initCoursesListView()
    {
    	//Get the courselist from the native storage directory
    	//if native course package is empty
    	//open the login dialog 
    	
    
    	/////////////////////////
    	
        lvCoursesAdapter = new ListViewCoursesAdapter(this, lvCoursesData, R.layout.course_listitem);        
        lvCourses_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvCourses_foot_more = (TextView)lvCourses_footer.findViewById(R.id.listview_foot_more);
        lvCourses_foot_progress = (ProgressBar)lvCourses_footer.findViewById(R.id.listview_foot_progress);
        lvCourses = (PullToRefreshListView)findViewById(R.id.frame_listview_course);
        
        lvCourses.addFooterView(lvCourses_footer);//Add the bottom view, and it must be set before setAdapter()
        lvCourses.setAdapter(lvCoursesAdapter); 
        lvCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//invalid to click the top and bottom bar
        		if(position == 0 || view == lvCourses_footer) return;
        		        		
        		Course course = null;        		
        		//is it TextView
        		if(view instanceof TextView){
        			course = (Course)view.getTag();
        		}else{
        			TextView tv = (TextView)view.findViewById(R.id.course_listitem_title);
        			course = (Course)tv.getTag();
        		}
        		if(course == null) return;
        		
        		
        		String courseGUID = course.getCourseGUID();
        		String courseZipURI = course.getCourseZipUri();
        		
        		
        		//Check if it has been downloaded
        		File courseManifestFile = new File(FileUtils.EkkoCourseSetRootPath() + courseGUID + "/manifest.xml");
        		
        		if(courseManifestFile.exists()){ //need to add the version checking later
        			//if network is connected, check if there is a new version of a course
        			if(appContext.isNetworkConnected()){
        				String courseURI = course.getCourseURI();;
        				if(StringUtils.isEmpty(courseURI)){
        					
        				}
        			}
        					
        			//exist and just load the native manifest.xml file
        			//set the current course to the new one
        			Course courseNew = (Course) appContext.readObject(courseGUID);
					try {
						if(courseNew == null){
							
							courseNew = AppContext.getInstance().instanceCourse(courseManifestFile);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						courseManifestFile=null;
					}
					
					if(courseNew !=null ){
	        			
	        			if(AppContext.getPreCourse() == null){
	        				AppContext.setPreCourse(courseNew);
	        			} else{
	        				if(AppContext.getPreCourse().getCourseGUID() != AppContext.getInstance().getCurCourse().getCourseGUID()){
	        					AppContext.setPreCourse(appContext.getCurCourse());
	        				}
	        				//save the courses state. may use the sha1 of the course package  
	        				appContext.saveObject(appContext.getCurCourse(), appContext.getCurCourse().getCourseGUID());
	        			}
	        			AppContext.setCurCourse(courseNew);
	        		        			        			
	        			//update the course details frame
	        			NavigateToNextLesson(0);
	        			
	        			mScrollLayout.snapToScreen(1); 
					}
        			
        		}else {
        			//download course zip package
        			//ApiClient.getCourseZipFile(appContext, courseZipURI);
        			downloaderThread = new CoursePackageDownloaderThread(Main.this, courseZipURI, FileUtils.EkkoCourseSetRootPath()+courseGUID +"/");
        			
        			downloaderThread.start();
        		        			        			
        		}
        	
        		//Navigate to the course details
        		//UIController.showCourseRedirect(view.getContext(), course);
        	}        	
		});
        lvCourses.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				lvCourses.onScrollStateChanged(view, scrollState);
				
				//Data empty, just return
				if(lvCoursesData.isEmpty()) return;
				
				//has scrolled to the bottom?
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(lvCourses_footer) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(lvCourses.getTag());
				if(scrollEnd && lvDataState==UIController.LISTVIEW_DATA_MORE)
				{
					lvCourses.setTag(UIController.LISTVIEW_DATA_LOADING);
					lvCourses_foot_more.setText(R.string.load_ing);
					lvCourses_foot_progress.setVisibility(View.VISIBLE);
					//current pageIndex
					int pageIndex = lvCoursesSumData/AppContext.PAGE_SIZE;
					loadLvCoursesData(curCoursesCatalog, pageIndex, lvCoursesHandler, UIController.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				lvCourses.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
        lvCourses.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadLvCoursesData(curCoursesCatalog, 0, lvCoursesHandler, UIController.LISTVIEW_ACTION_REFRESH);
            }
        });					
    }

    
	public void slideToTop(View view){
/*		TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
		animate.setDuration(600);
		animate.setFillAfter(true);
		view.startAnimation(animate);*/
		
		view.setVisibility(View.GONE);
	}
	
	
	public void slideToNormal(View view){
/*		TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
		animate.setDuration(600);
		animate.setFillAfter(true);
		view.startAnimation(animate);*/
		view.setVisibility(View.VISIBLE);
	}
	

	private boolean normalPosition =true;
    /**
     * Init Course details view
     */
    private void initCourseDetailsView()
    {
    	try {
			AppContext.getInstance().instanceCourse(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// init course data
    	Course course = AppContext.getInstance().getCurCourse();
         //init the lesson text pager  
    	vpLessonTextPager = (ViewPager)findViewById(R.id.frame_lesson_text_pager);
    	int lessonIndex = AppContext.getInstance().getCurrentLessonIndex();
    
    	if(lessonIndex == -1){
    		Log.w("Main", "no lesson list");
    	}
    	int pageNum = course.getLessonList().get(lessonIndex).getPagedTextList().getElements().size();
    	
    	mTextPagerAdapter = new LessonTextPagerAdapter(getSupportFragmentManager(), pageNum);
    	
    	// restore the last accessed lesson    	
    	
    	vpLessonTextPager.setAdapter(mTextPagerAdapter);
    	
    	
      	mLesson_title = (Button)findViewById(R.id.frame_btn_lesson_title);
      	mLesson_next = (Button) findViewById(R.id.frame_btn_next_lesson);
      	mLesson_prev = (Button) findViewById(R.id.frame_btn_previous_lesson);
      	
      	
    	mLesson_title.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			if(normalPosition){
	    			//slideToTop((View)findViewById(R.id.lesson_media_container));
	    			slideToTop((View)findViewById(R.id.lesson_media_container));
	    			//slideToTop2((View)findViewById(R.id.frame_course_navigator_container), (View)findViewById(R.id.frame_lesson_media_slideshow));
	    			normalPosition = false;
    			} else
    			{
    				//slideToNormal((View)findViewById(R.id.lesson_media_container));
    				slideToNormal((View)findViewById(R.id.lesson_media_container));
    				//slideToNormal2((View)findViewById(R.id.frame_course_navigator_container),(View)findViewById(R.id.frame_lesson_media_slideshow));
    				normalPosition = true;
    			}
    			
    		}
    	});
    	
    	mLesson_next.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			//navigate to the next lesson
    			
    			NavigateToNextLesson(1);
    		}
    	});
    	
    	mLesson_prev.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			//navigate to the next lesson
    			NavigateToNextLesson(-1);
    		}
    	});
    	
    	
    	vpLessonTextPager.setOnTouchListener(new OnTouchListener() {  
            
            public boolean onTouch(View v, MotionEvent event) { 
            	
                switch (event.getAction()) {  
                case MotionEvent.ACTION_DOWN:  
                case MotionEvent.ACTION_MOVE: 
                	//Toast.makeText(appContext, "lesson text pager touched", 200).show();
   
                    break;  
                case MotionEvent.ACTION_UP:  
                  
                    break;  
                default:  
                    
                    break;  
                }  
                return false;  
            }  
        }); 
    	mTextPagerIndicator = (LinePageIndicator)findViewById(R.id.frame_lesson_text_indicator);
    	mTextPagerIndicator.setViewPager(vpLessonTextPager);

    	vpLessonMedia = (ViewPager)findViewById(R.id.frame_lesson_media_slideshow);
    	mMediaPagerIndicator = (CirclePageIndicator)findViewById(R.id.frame_lesson_media_indicator);   	
        
    	// To animate view slide out from bottom to top

    	
    	//get the media list of a lesson
    	List<Drawable> lessonMedia = null;
    	//get the last access lesson
    	//To do:
    	int lastAccessLesson = 0;
    	lessonMedia = AppContext.getInstance().getCurLessonMediaList(course, lastAccessLesson);
    	
    	mMediaPagerAdapter = new LessonMediaPagerAdapter(getSupportFragmentManager(), lessonMedia); 	    	
    	vpLessonMedia.setAdapter(mMediaPagerAdapter);
    	
    	mMediaPagerIndicator = (CirclePageIndicator)findViewById(R.id.frame_lesson_media_indicator);
    	mMediaPagerIndicator.setViewPager(vpLessonMedia);

    	
    	mMediaPagerIndicator = (CirclePageIndicator)findViewById(R.id.frame_lesson_media_indicator);
    	
    	vpLessonMedia.setOnTouchListener(new OnTouchListener() {  
    		
          public boolean onTouch(View v, MotionEvent event) {  
        	  
              switch (event.getAction()) {  
              case MotionEvent.ACTION_DOWN:  
              case MotionEvent.ACTION_MOVE: 
            	  //Toast.makeText(appContext, "Slideshow touched", 200).show();
            	 //  vpLessonTextPager.setCurrentItem(6);
                //   mScrollLayout.setIsScroll(false);
                   //test to play video
                 //  final String TEST_STREAM_REMOTE = "http://www.denivip.ru/sites/default/files/trailer2.mp4"; 
                  // Intent intent = VideoPlayer.createIntent(appContext, null, TEST_STREAM_REMOTE );
                  // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                  // appContext.startActivity(intent);              
                  break;  
              case MotionEvent.ACTION_UP:  
                   
                  break;  
              default:  
                  
                  break;  
              }  
              return false;  
          }  
      });  

    	   				
    }

    private void UpdateLessonTitleButtonText(){
    	int curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
    	
    	//init pager number
    	((LessonTextPagerAdapter) mTextPagerAdapter).setPageNumber(AppContext.getInstance().getCurLessonTextPagerCount());
    	this.mTextPagerAdapter.notifyDataSetChanged();
    	String progressBuilder= "Lesson [" +(curLessonIndex +1) + "/" +AppContext.getInstance().getCurCourse().getLessonList().size() +"]";
    	
    	mLesson_title.setText( progressBuilder );
    }
    
    /**
     * Lesson Navigator
     * @param delta
     */
    private void NavigateToNextLesson(int delta){
    	//reload    	
    	
    	int curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
    	int lessonSize = AppContext.getInstance().getCurCourse().getLessonList().size();
    	if (lessonSize <=0) return;
    	
    
    	AppContext.getInstance().setCurrentLessonIndex(((curLessonIndex+delta)+lessonSize)%(lessonSize));
    	

    	curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
    	//update lesson_title button text
    	UpdateLessonTitleButtonText();
    	
    	//reload the media slide viewpager
    
    	List<Drawable> lessonMedia = null;
    	//get the last access lesson
    	//To do:    	
    	lessonMedia = AppContext.getInstance().getCurLessonMediaList(AppContext.getInstance().getCurCourse(), curLessonIndex);
    	((LessonMediaPagerAdapter)mMediaPagerAdapter).setImageList(lessonMedia);
    	
    	mMediaPagerAdapter.notifyDataSetChanged();
    	vpLessonMedia.setAdapter(mMediaPagerAdapter);
    	
    	//update indicator for textpager and media pager
    	mTextPagerIndicator.notifyDataSetChanged();
    	mMediaPagerIndicator.notifyDataSetChanged();
    	//lessonMedia=null;
    	
    	if(mLessonListMenuFrag!=null){
    		//set the value of  progressbar
	    	((LessonListSlidingMenu)mLessonListMenuFrag).updateProgressBar();
	    	//set the focus of current lesson
	    	((LessonListSlidingMenu)mLessonListMenuFrag).updateLessonFocus();
	    	//update the medialist of slidemenu of lesson list
	    	((LessonListSlidingMenu)mLessonListMenuFrag).updateMediaList();
    	}
    }

    
    /**
     * Init Head view 
     */
    private void initHeadView()
    {
    	mHeadLogo = (ImageView)findViewById(R.id.main_head_logo);
    	mHeadTitle = (TextView)findViewById(R.id.main_head_title);
    	
    	mHeadProgress = (ProgressBar)findViewById(R.id.main_head_progress);
    	mHead_search = (ImageButton)findViewById(R.id.main_head_search);
    	mHead_lessons = (ImageButton)findViewById(R.id.main_head_course_lessonlist);
    	
       	
    	mHeadLogo.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			//show course list or the app setting menu based on the context
    			toggle();
    		}
    	});
    	
    	mHead_lessons.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			//show lesson list and the media thumbnails based on the context
    			showSecondaryMenu();
    			
    		}
    	});
   
 
    	
  //  	mHead_search.setOnClickListener(new View.OnClickListener() {
		//	public void onClick(View v) {
	//			UIController.showSearch(v.getContext());
		//	}
	//	});

    }
    /**
     * Init the foot view
     */
    private void initFootBar()
    {
    	fbCourses = (RadioButton)findViewById(R.id.main_footbar_courselist);
    	fbCourseDetail = (RadioButton)findViewById(R.id.main_footbar_coursedetail);
    	
    	fbSetting = (ImageView)findViewById(R.id.main_footbar_setting);
    	fbSetting.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {    			
    			//Display Quick bar
    			UIController.showSettingLoginOrLogout(Main.this, mGrid.getQuickAction(0));
    			mGrid.show(v);
    		}
    	});    	
    }
 
	
	/**
     * Init Horizontal Page Scroll 
     */
    private void initPageScroll()
    {
    	mScrollLayout = (ScrollLayout) findViewById(R.id.main_scrolllayout);
    	
    	LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linearlayout_footer);
    	mHeadTitles = getResources().getStringArray(R.array.head_titles);
    	mViewCount = mScrollLayout.getChildCount();
    	mButtons = new RadioButton[mViewCount];
    	
    	for(int i = 0; i < mViewCount; i++)
    	{
    		mButtons[i] = (RadioButton) linearLayout.getChildAt(i*2);
    		mButtons[i].setTag(i);
    		mButtons[i].setChecked(false);
    		mButtons[i].setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int pos = (Integer)(v.getTag());
					//click the frame and refresh
	    			if(mCurSel == pos) {
		    			switch (pos) {
						case 0:							
							lvCourses.clickRefresh();						
							break;	
						case 1:							
							//refresh the course details
							
							break;	
					
						
						}
	    			}
					mScrollLayout.snapToScreen(pos);
				}
			});
    	}
    	
    	//Set to the first screen
    	mCurSel = 0;
    	mButtons[mCurSel].setChecked(true);
    	

    	mScrollLayout.SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
			public void OnViewChange(int viewIndex) {
				//Switch list view and if the listview data is empty: load data
				switch (viewIndex) {
				case 0://Course list
				
					//set the sliding menu
					getSlidingMenu().setMode(SlidingMenu.LEFT);
					setBehindContentView(R.layout.menu_frame_app_setting);
					FragmentTransaction t = getSupportFragmentManager().beginTransaction();
					mAppSettingListMenuFrag = new AppSlidingMenu(new SlideMenuListener(){
						
						@Override
						public void reload(){
							//refresh the lesson text viewpager content 
							((LessonTextPagerAdapter) mTextPagerAdapter).setPageNumber(AppContext.getInstance().getCurLessonTextPagerCount());
							mTextPagerAdapter.notifyDataSetChanged();
							
					    	List<Drawable> lessonMedia = null;
					    	//get the last access lesson
					    	//To do:
					    	//update he media viewpager content
					    	int curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
					    	lessonMedia = AppContext.getInstance().getCurLessonMediaList(AppContext.getInstance().getCurCourse(), curLessonIndex);
					    	((LessonMediaPagerAdapter)mMediaPagerAdapter).setImageList(lessonMedia);				    	
					    	
					    	vpLessonMedia.setAdapter(mMediaPagerAdapter);
					    	
					    	//update indicator for textpager and media pager
					    	mTextPagerIndicator.notifyDataSetChanged();
					    	mMediaPagerIndicator.notifyDataSetChanged();
					    	
					    	//update the text of course progress button
					    	UpdateLessonTitleButtonText();
					    	
							//toggle the lesson list sliding menu
							//showSecondaryMenu();	
							showContent();
						}

						@Override
						public void showcontent() {
							// TODO Auto-generated method stub
							showContent();
						}
					});
					t.replace(R.id.menu_frame_setting, mAppSettingListMenuFrag);
					t.commit();

					// customize the SlidingMenu
					SlidingMenu sm = getSlidingMenu();
					sm.setShadowWidthRes(R.dimen.shadow_width);
					sm.setShadowDrawable(R.drawable.shadow);
					sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
					sm.setFadeDegree(0.35f);
					sm.setTouchModeAbove(SlidingMenu.LEFT);
					
					if(lvCoursesData.isEmpty()) {
						loadLvCoursesData(1, 0, lvCoursesHandler, UIController.LISTVIEW_ACTION_INIT);
					}			
					break;	
				case 1://Course details and lessons
					
					mCourseListMenuFrag = new CourseListSlidingMenu(new SlideMenuListener(){
						
						@Override
						public void reload(){

							updateTextAndMediaView();
							
						}

						

						@Override
						public void showcontent() {
							// TODO Auto-generated method stub
							showContent();
						}
					});
					getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);					
					
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.menu_frame_setting, mCourseListMenuFrag)
					.commit();
					
					mLessonListMenuFrag = new LessonListSlidingMenu(new SlideMenuListener(){
						
						@Override
						public void reload(){
							updateTextAndMediaView();
						}
						@Override
						public void showcontent(){
							showContent();
						}
					
					});
					getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_lesson_list);
					//getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);
					getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.menu_frame_lesson_list, mLessonListMenuFrag)
					.commit();
 
					break;			
				
				}
				setCurPoint(viewIndex);
			}
		});
    }
    /**
     * Set the focus of the bottom bar
     * @param index
     */
    private void setCurPoint(int index)
    {
    	if (index < 0 || index > mViewCount - 1 || mCurSel == index)
    		return;
   	
    	mButtons[mCurSel].setChecked(false);
    	mButtons[index].setChecked(true);    	
    	mHeadTitle.setText(mHeadTitles[index]);    	
    	mCurSel = index;
    	
    	mHead_search.setVisibility(View.GONE);
    	mHead_lessons.setVisibility(View.GONE);
    
		//the header logo and title
    	if(index == 0){
    		mHeadLogo.setImageResource(R.drawable.frame_logo_courselist);
    	
    		mHead_search.setVisibility(View.VISIBLE);
    	}
    	else if(index == 1){
    		mHeadLogo.setImageResource(R.drawable.frame_logo_lessonlist);    		
    		mHead_lessons.setVisibility(View.VISIBLE);
    	}

    }
	
		
	/**
	 * Listen
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			//exit application
			UIController.Exit(this);
		}else if(keyCode == KeyEvent.KEYCODE_MENU){
			//display popup menu
			UIController.showSettingLoginOrLogout(Main.this, mGrid.getQuickAction(0));
			mGrid.show(fbSetting, true);
		}else if(keyCode == KeyEvent.KEYCODE_SEARCH){
			//display search dialog
			//UIHelper.showSearch(Main.this);
		}else{
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;
	}
	
    /**
     * thread to load course list data
     * @param catalog 
     * @param pageIndex 
     * @param handler
     * @param action 
     */
	private void loadLvCoursesData(final int catalog,final int pageIndex,final Handler handler,final int action){ 
		mHeadProgress.setVisibility(ProgressBar.VISIBLE);	
		new Thread(){
			public void run() {				
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIController.LISTVIEW_ACTION_REFRESH || action == UIController.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {					
					CourseList list = appContext.getCourseList(catalog, pageIndex, isRefresh);	
					if(list != null && list.getCourselist() != null ) {
						msg.what = list.getCourselist().size(); //Get the page retrieved size 
						//msg.what = list.getStart();
						msg.obj = list;
					} else {
						msg.what = -1;
						msg.obj = "failed to load courselist";
					}
	            } catch (AppException e) {
	            	Log.e("Main-loadlvCourseData",e.toString());
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;
				msg.arg2 = UIController.LISTVIEW_DATATYPE_COURSE;
              
                handler.sendMessage(msg);
			}
		}.start();
	}
	
    /**
     * get the initialized Handler of listview
     * @param lv
     * @param adapter
     * @return
     */
    private Handler getLvHandler(final PullToRefreshListView lv,final BaseAdapter adapter,final TextView more,final ProgressBar progress,final int pageSize){
    	return new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what >= 0){
					//listview data processing
					Notice notice = handleLvData(msg.what, msg.obj, msg.arg2, msg.arg1);
					
					if(msg.what < pageSize){
						lv.setTag(UIController.LISTVIEW_DATA_FULL);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_full);
					}else if(msg.what == pageSize){
						lv.setTag(UIController.LISTVIEW_DATA_MORE);
						adapter.notifyDataSetChanged();
						more.setText(R.string.load_more);
					}

				}
				else if(msg.what == -1){
					lv.setTag(UIController.LISTVIEW_DATA_MORE);
					more.setText(R.string.load_error);
					Log.e("Main-getLvHandler", msg.obj.toString());
					//((AppException)msg.obj).makeToast(Main.this);
				}
				if(adapter.getCount()==0){
					lv.setTag(UIController.LISTVIEW_DATA_EMPTY);
					more.setText(R.string.load_empty);
				}
				progress.setVisibility(ProgressBar.GONE);
				mHeadProgress.setVisibility(ProgressBar.GONE);
				if(msg.arg1 == UIController.LISTVIEW_ACTION_REFRESH){
					lv.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
					lv.setSelection(0);
				}else if(msg.arg1 == UIController.LISTVIEW_ACTION_CHANGE_CATALOG){
					lv.onRefreshComplete();
					lv.setSelection(0);
				}
			}
		};
    }
    
    private Notice handleLvData(int what,Object obj,int objtype,int actiontype){
    	Notice notice = null;
		switch (actiontype) {
			case UIController.LISTVIEW_ACTION_INIT:
			case UIController.LISTVIEW_ACTION_REFRESH:
			case UIController.LISTVIEW_ACTION_CHANGE_CATALOG:
				int newdata = 0;//new loading data-used only when refreshed
				switch (objtype) {
					case UIController.LISTVIEW_DATATYPE_COURSE:
						CourseList clist = (CourseList)obj;
						notice = clist.getNotice();
						lvCoursesSumData = what;
						if(actiontype == UIController.LISTVIEW_ACTION_REFRESH){
							if(lvCoursesData.size() > 0){
								for(Course course1 : clist.getCourselist()){
									boolean b = false;
									for(Course course2 : lvCoursesData){
										if(course1.getCourseGUID() == course2.getCourseGUID()){
											b = true;
											break;
										}
									}
									if(!b) newdata++;
								}
							}else{
								newdata = what;
							}
						}
						lvCoursesData.clear();//clear original data
						lvCoursesData.addAll(clist.getCourselist());
						break;

				}
				if(actiontype == UIController.LISTVIEW_ACTION_REFRESH){
					//Promote new added course
					if(newdata >0){
						NewDataToast.makeText(this, getString(R.string.new_data_toast_message, newdata), appContext.isAppSound()).show();
					}else{
						NewDataToast.makeText(this, getString(R.string.new_data_toast_none), false).show();
					}
				}
				break;
			case UIController.LISTVIEW_ACTION_SCROLL:
				switch (objtype) {
					case UIController.LISTVIEW_DATATYPE_COURSE:
						CourseList list = (CourseList)obj;
						notice = list.getNotice();
						lvCoursesSumData += what;
						if(lvCoursesData.size() > 0){
							for(Course course1 : list.getCourselist()){
								boolean b = false;
								for(Course course2 : lvCoursesData){
									if(course1.getCourseGUID() == course2.getCourseGUID()){
										b = true;
										break;
									}
								}
								if(!b) lvCoursesData.add(course1);
							}
						}else{
							lvCoursesData.addAll(list.getCourselist());
						}
						break;

				}
				break;
		}
		return notice;
    } 
    
    private void updateTextAndMediaView() {
		// TODO Auto-generated method stub
		//refresh the lesson text viewpager content 
		((LessonTextPagerAdapter) mTextPagerAdapter).setPageNumber(AppContext.getInstance().getCurLessonTextPagerCount());
		mTextPagerAdapter.notifyDataSetChanged();
		
    	List<Drawable> lessonMedia = null;
    	//get the last access lesson
    	//To do:
    	//update he media viewpager content
    	int curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
    	lessonMedia = AppContext.getInstance().getCurLessonMediaList(AppContext.getInstance().getCurCourse(), curLessonIndex);
    	((LessonMediaPagerAdapter)mMediaPagerAdapter).setImageList(lessonMedia);				    	
    	
    	vpLessonMedia.setAdapter(mMediaPagerAdapter);
    	
    	//update indicator for textpager and media pager
    	mTextPagerIndicator.notifyDataSetChanged();
    	mMediaPagerIndicator.notifyDataSetChanged();
    	
    	//update the text of course progress button
    	UpdateLessonTitleButtonText();
    	
		//toggle the lesson list sliding menu
		//showSecondaryMenu();	
		showContent();
	}
    
	/**
	 * This is the Handler for this activity. It will receive messages from the
	 * CoursePackageDownloaderThread and make the necessary updates to the UI.
	 */
	public Handler zipDownloadHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				/*
				 * Handling MESSAGE_UPDATE_PROGRESS_BAR:
				 * 1. Get the current progress, as indicated in the arg1 field
				 *    of the Message.
				 * 2. Update the progress bar.
				 */
				case MESSAGE_UPDATE_PROGRESS_BAR:
					if(progressDialog != null)
					{
						int currentProgress = msg.arg1;
						progressDialog.setProgress(currentProgress);
					}
					break;
				
				/*
				 * Handling MESSAGE_CONNECTING_STARTED:
				 * 1. Get the URL of the file being downloaded. This is stored
				 *    in the obj field of the Message.
				 * 2. Create an indeterminate progress bar.
				 * 3. Set the message that should be sent if user cancels.
				 * 4. Show the progress bar.
				 */
				case MESSAGE_CONNECTING_STARTED:
					if(msg.obj != null && msg.obj instanceof String)
					{
						String url = (String) msg.obj;
						// truncate the url
						if(url.length() > 16)
						{
							String tUrl = url.substring(0, 15);
							tUrl += "...";
							url = tUrl;
						}
						String pdTitle = Main.this.getString(R.string.progress_dialog_title_connecting);
						String pdMsg = Main.this.getString(R.string.progress_dialog_message_prefix_connecting);
						pdMsg += " " + url;
						
						progressDialog = new ProgressDialog(Main.this);
						progressDialog.setTitle(pdTitle);
						progressDialog.setMessage(pdMsg);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setIndeterminate(true);
						// set the message to be sent when this dialog is canceled
						Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
						progressDialog.setCancelMessage(newMsg);
						progressDialog.show();
					}
					break;
					
				/*
				 * Handling MESSAGE_DOWNLOAD_STARTED:
				 * 1. Create a progress bar with specified max value and current
				 *    value 0; assign it to progressDialog. The arg1 field will
				 *    contain the max value.
				 * 2. Set the title and text for the progress bar. The obj
				 *    field of the Message will contain a String that
				 *    represents the name of the file being downloaded.
				 * 3. Set the message that should be sent if dialog is canceled.
				 * 4. Make the progress bar visible.
				 */
				case MESSAGE_DOWNLOAD_STARTED:
					// obj will contain a String representing the file name
					if(msg.obj != null && msg.obj instanceof String)
					{
						int maxValue = msg.arg1;
						String fileName = (String) msg.obj;
						String pdTitle = Main.this.getString(R.string.progress_dialog_title_downloading);
						String pdMsg = Main.this.getString(R.string.progress_dialog_message_prefix_downloading);
						pdMsg += " " + fileName;
						
						dismissCurrentProgressDialog();
						progressDialog = new ProgressDialog(Main.this);
						progressDialog.setTitle(pdTitle);
						progressDialog.setMessage(pdMsg);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setProgress(0);
						progressDialog.setMax(maxValue);
						// set the message to be sent when this dialog is canceled
						Message newMsg = Message.obtain(this, MESSAGE_DOWNLOAD_CANCELED);
						progressDialog.setCancelMessage(newMsg);
						progressDialog.setCancelable(true);
						progressDialog.show();
					}
					break;
				
				/*
				 * Handling MESSAGE_DOWNLOAD_COMPLETE:
				 * 1. Remove the progress bar from the screen.
				 * 2. Display Toast that says download is complete.
				 */
				case MESSAGE_DOWNLOAD_COMPLETE:
					dismissCurrentProgressDialog();
					displayMessage(getString(R.string.user_message_download_complete));
					
					//reset the current course to the new download course
					//navigate to the new course detail page
					
					break;
					
				/*
				 * Handling MESSAGE_DOWNLOAD_CANCELLED:
				 * 1. Interrupt the downloader thread.
				 * 2. Remove the progress bar from the screen.
				 * 3. Display Toast that says download is complete.
				 */
				case MESSAGE_DOWNLOAD_CANCELED:
					if(downloaderThread != null)
					{
						downloaderThread.interrupt();
					}
					dismissCurrentProgressDialog();
					displayMessage(getString(R.string.user_message_download_canceled));
					break;
				
				/*
				 * Handling MESSAGE_ENCOUNTERED_ERROR:
				 * 1. Check the obj field of the message for the actual error
				 *    message that will be displayed to the user.
				 * 2. Remove any progress bars from the screen.
				 * 3. Display a Toast with the error message.
				 */
				case MESSAGE_ENCOUNTERED_ERROR:
					// obj will contain a string representing the error message
					if(msg.obj != null && msg.obj instanceof String)
					{
						String errorMessage = (String) msg.obj;
						dismissCurrentProgressDialog();
						displayMessage(errorMessage);
					}
					break;
					
				default:
					// nothing to do here
					break;
			}
		}
	};
	
	/**
	 * If there is a progress dialog, dismiss it and set progressDialog to
	 * null.
	 */
	public void dismissCurrentProgressDialog()
	{
		if(progressDialog != null)
		{
			progressDialog.hide();
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	/**
	 * Displays a message to the user, in the form of a Toast.
	 * @param message Message to be displayed.
	 */
	public void displayMessage(String message)
	{
		if(message != null)
		{
			Toast.makeText(Main.this, message, Toast.LENGTH_SHORT).show();
		}
	}
	
	
  
  }

