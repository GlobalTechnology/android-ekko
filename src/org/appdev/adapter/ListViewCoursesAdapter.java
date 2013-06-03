package org.appdev.adapter;

import java.io.File;
import java.util.List;

import org.appdev.R;
import org.appdev.entity.Course;
import org.appdev.utils.FileUtils;
import org.appdev.utils.ImageUtils;
import org.appdev.utils.StringUtils;
import org.appdev.utils.UIController;
import org.appdev.widget.RoundedDrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListViewCoursesAdapter extends BaseAdapter {
	private Context 					context;
	private List<Course> 					listItems;
	private LayoutInflater 				listContainer;
	private int 						itemViewResource;
	static class ListItemView{				
	        public TextView title;  
		    public TextView author;
		    public TextView date;  
		    public TextView count;
		    public ImageView thumbnail;
		    public ImageView flag;
		    public ProgressBar progressbar;
	 }  

	/**
	 * Course list adapter
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewCoursesAdapter(Context context, List<Course> data,int resource) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	
		this.itemViewResource = resource;
		this.listItems = data;
	}
	
	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}
	
	/**
	 * ListView Item
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");
		
		ListItemView  listItemView = null;
		
		if (convertView == null) {
			
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			
			listItemView.title = (TextView)convertView.findViewById(R.id.course_listitem_title);
			listItemView.thumbnail = (ImageView)convertView.findViewById(R.id.course_listitem_thumbnail);
			listItemView.progressbar = (ProgressBar)convertView.findViewById(R.id.course_listitem_progressBar);

			//listItemView.flag= (ImageView)convertView.findViewById(R.id.course_listitem_flag);			
			
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}	
		
		
		Course course = listItems.get(position);
		
		listItemView.title.setText(course.getCourseTitle());
		listItemView.title.setTag(course);
		String bannerKey = course.getCourseBanner();
		if(bannerKey != null || !StringUtils.isEmpty(bannerKey)){
			String res = course.getResourceMap().get(bannerKey).getResourceFile();
			File resFile = new File(FileUtils.EkkoCourseSetRootPath() + course.getCourseGUID() +"/" + res);
			if(resFile.exists()){
				Bitmap bitmap = BitmapFactory.decodeFile(resFile.getAbsolutePath());
				//get rounded corner bitmap
				//RoundedDrawable drawable = ImageUtils.getRoundedCornerBitmap(context, bitmap, 10);
				bitmap = ImageUtils.getRoundedCornerBitmap(context, bitmap, 20);
				//listItemView.thumbnail.setImageDrawable(drawable);
				listItemView.thumbnail.setImageBitmap(bitmap);
				
				
				//listItemView.thumbnail.setImageURI(Uri.fromFile(resFile));
			} else {
				//TODO: get course URI on-line
				String uriString = course.getCourseURI()+"/resources/resource/" + course.getResourceMap().get(bannerKey).getResourceSha1();
				UIController.showLoadImage(listItemView.thumbnail, uriString,  null);
			}
		}else{
			UIController.showLoadImage(listItemView.thumbnail, "",  null);
		}
			
		
		//set the progress of the course
		listItemView.progressbar.setProgress(course.getProgress());
		
		
/*		listItemView.date.setText(StringUtils.friendly_time(course.getPubDate()));
		
		if(StringUtils.isToday(course.getPubDate()))
			listItemView.flag.setVisibility(View.VISIBLE);
		else
			listItemView.flag.setVisibility(View.GONE);*/
		
		return convertView;
	}
}