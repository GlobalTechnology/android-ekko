package org.appdev.ui;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.content.Context;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.appdev.adapter.MediaGridAdapter;
import org.appdev.api.MainActivityListener;
import org.appdev.api.SlideMenuListener;
import org.appdev.app.AppContext;
import org.appdev.entity.Lesson;

import org.appdev.R;

public class LessonListSlidingMenu extends ListFragment implements MainActivityListener {

	private SlideMenuListener listener;
	
	private MenuAdapter lessonListAdapter = null;
	private MediaGridAdapter mediaGridAdapter = null;
	private GridView gridview = null;
	
	public LessonListSlidingMenu(SlideMenuListener listener) {
		this.listener = listener;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.lesson_list_menu, null);
		
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setSelected(true);
		
		//set lesson adapter
		lessonListAdapter = new MenuAdapter(getActivity());
		ArrayList<Lesson> lessonList = AppContext.getInstance().getCurCourse().getLessonList();
		if(lessonList == null)  return;
		
		for(int i=0; i<lessonList.size(); i++){
	
			lessonListAdapter.add(new LessonListMenuItem(lessonList.get(i).getLesson_title(), android.R.drawable.ic_menu_view));
	
		}

		setListAdapter(lessonListAdapter);

		//set the media grid adapter 
		mediaGridAdapter = new MediaGridAdapter(AppContext.getInstance(), getMediaList());
		gridview = (GridView) getActivity().findViewById(R.id.lessonMediaGridview);
	    gridview.setAdapter(mediaGridAdapter);

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
		
	}

	private List<Drawable> getMediaList() {
		// TODO Auto-generated method stub
		List<Drawable> mediaList = null;
		int curLessonIndex = AppContext.getInstance().getCurrentLessonIndex();
		mediaList = AppContext.getInstance().getCurLessonMediaList(AppContext.getInstance().getCurCourse(), curLessonIndex);
		return mediaList;
	}

	private class LessonListMenuItem {
		public String tag;
		public int iconRes;
		public LessonListMenuItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	
	public class MenuAdapter extends ArrayAdapter<LessonListMenuItem> {

		public MenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {								
			 
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			
			
			if(AppContext.getInstance().getCurrentLessonIndex() == position){
				
				//convertView.setBackgroundResource(android.R.color.darker_gray);
				convertView.setSelected(true);
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
		//load the lesson text and media data and navigate to the clicked lesson
		AppContext.getInstance().setCurrentLessonIndex(position);
		
		//update the media list of the current UI
		updateMediaList();
		
		//navigate to the new lesson in the lesson text pager view
		listener.reload();	

	}
	

	@Override
	public void updateLessonFocus() {
		// TODO Auto-generated method stub
		getListView().setFocusable(true);
				
		getListView().setItemChecked(AppContext.getInstance().getCurrentLessonIndex(), true);	

	}

	@Override
	public void updateMediaList() {
		// TODO Auto-generated method stub
		mediaGridAdapter = new MediaGridAdapter(AppContext.getInstance(), getMediaList());
		gridview.setAdapter(mediaGridAdapter);		
	}

	@Override
	public void updateProgressBar() {
		// TODO Auto-generated method stub
		ProgressBar courseProgress =(ProgressBar) getActivity().findViewById(R.id.lesson_progressbar);
		ArrayList<Lesson> lessonList = AppContext.getInstance().getCurCourse().getLessonList();
		
		//todo: need to find a better way to decide how a user finish a lesson.
		
		if(lessonList.size()>0 && courseProgress != null){
			Log.i("LessonListSlidingMenu_progressBar",Integer.toString(((AppContext.getInstance().getCurrentLessonIndex()+1)/lessonList.size())*100 ));
			
			courseProgress.setProgress(((AppContext.getInstance().getCurrentLessonIndex()+1)*100/lessonList.size()));
		} 
	}

}
