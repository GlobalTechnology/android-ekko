package org.appdev.ui;


import java.io.File;
import java.io.IOException;

import org.appdev.R;
import org.appdev.api.SlideMenuListener;
import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.entity.Course;
import org.appdev.entity.CourseList;
import org.appdev.utils.FileUtils;
import org.ekkoproject.android.player.api.InvalidSessionApiException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class CourseListSlidingMenu extends ListFragment {
	private MenuAdapter courseListAdapter = null;

	private CourseList courseList = null;
	
	private SlideMenuListener listener; //used to update main-course details UI
	
    public CourseListSlidingMenu(SlideMenuListener listener) {
		this.setListener(listener);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.course_list_menu, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		courseListAdapter = new MenuAdapter(getActivity());
	
		courseListAdapter = new MenuAdapter(getActivity());
		
		try {
			courseList = AppContext.getInstance().getCourseList(0, 0, false);
		} catch (AppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } catch (final InvalidSessionApiException e) {
            // TODO should we attempt a login?
        }
		
		if(courseList == null)  return;
		
		for(int i=0; i<courseList.getCourselist().size(); i++){
	
			courseListAdapter.add(new AppMenuItem(courseList.getCourselist().get(i).getCourseTitle(), android.R.drawable.ic_menu_view));
	
		}

		setListAdapter(courseListAdapter);
/*		adapter.add(new AppMenuItem("Course 1", android.R.drawable.ic_menu_view));
		adapter.add(new AppMenuItem("course 2", android.R.drawable.ic_menu_view));
		adapter.add(new AppMenuItem("course 3", android.R.drawable.ic_menu_view));
		adapter.add(new AppMenuItem("course 4", android.R.drawable.ic_menu_view));	*/

		//setListAdapter(adapter);
		
	}
	
	public void refresh(){
		courseListAdapter.notifyDataSetChanged();
	}

	private class AppMenuItem {
		public String tag;
		public int iconRes;
		public AppMenuItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	public class MenuAdapter extends ArrayAdapter<AppMenuItem> {

		public MenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {

		AppContext appContext=AppContext.getInstance();
		//set the curCourse to the new one
		Course course = null;
		course = courseList.getCourselist().get(position);
		String courseGUID = course.getCourseGUID();
		File courseManifestFile = new File(FileUtils.getEkkoCourseManifestFile(courseGUID));
		if(courseManifestFile.exists()){ //need to add the version checking later
        	
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
    				if(AppContext.getPreCourse().getCourseGUID() != appContext.getCurCourse().getCourseGUID()){
    					AppContext.setPreCourse(AppContext.getInstance().getCurCourse());
    				}
    				//save the courses state. may use the sha1 of the course package  
    				appContext.saveObject(appContext.getCurCourse(),appContext.getCurCourse().getCourseGUID());
    			}
    			AppContext.setCurCourse(courseNew);
			}
		}
		
		//update the main activity UI and update the LessonLists slide menu UI, such as media grid, progress bar state, lesson list
		this.listener.reload();
	

	}
	public SlideMenuListener getListener() {
		return listener;
	}
	public void setListener(SlideMenuListener listener) {
		this.listener = listener;
	}


}
