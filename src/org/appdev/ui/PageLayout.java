package org.appdev.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.appdev.entity.Page;

public class PageLayout {
	
	protected LinearLayout layout;
	protected Page page;
	
	protected Context context;
	
	public void init(Context context, Page page){
		
		this.context = context;
		layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		this.page = new Page();
		this.page = page;
		
		HashMap<String, String> mapElements = new HashMap<String, String>();
		
		mapElements = page.getElements();
		for (Map.Entry<String, String> entry : mapElements.entrySet()){
			
			String elementType = entry.getKey();
			
			if (elementType.contains("text")) {
				//create TextView and add it to the page
				addTextElement(entry);
				
				
			} else if (elementType.contains("pic") ){
				//dynamically create picture 
				addPicElement(entry);
			} else if (elementType.contains("video")) {
				addVideoElement(entry);
				
			} else if (elementType.contains("comment")) {
				
			}						
		}
	
	}
	
	public LinearLayout getLayout(){
		
		return this.layout;
	}
	
	private void addTextElement(Map.Entry<String, String> entry) {
		
		String valueString = entry.getValue();
		
		String keyString = entry.getKey();
		
		//Just based on the order of putting the elements to the HashMap to create element for now
		//
		//String subKey[] = keyString.split("_"); //TO-Do: change the separator to ":"
		//String orderString = subKey[1];
		
		TextView textView = new TextView(this.context);
		textView.setText(valueString);
	    textView.setLayoutParams(new LayoutParams(
	    		LayoutParams.MATCH_PARENT,
	    		LayoutParams.MATCH_PARENT));
	    this.layout.addView(textView);	    	
		
	}
	
	private void addPicElement(Map.Entry<String, String> entry) {
		String urlString = entry.getValue();
		
		//String keyString = entry.getKey();
		
		//Just based on the order of putting the elements to the HashMap to create element for now
		//
		//String subKey[] = keyString.split("_"); //TO-Do: change the separator to ":"
		//String orderString = subKey[1];
		
		ImageView imageView = new ImageView(this.context);
		File imageFile = new File(Environment.getExternalStorageDirectory(), urlString);
		imageView.setImageURI(Uri.fromFile(imageFile));
		
	    imageView.setLayoutParams(new LayoutParams(
	    		LayoutParams.MATCH_PARENT,
	    		LayoutParams.MATCH_PARENT));
	    this.layout.addView(imageView);	
	}
	
	private void addVideoElement(Map.Entry<String, String> entry) {
		String urlString = entry.getValue();
		
		//String keyString = entry.getKey();
		
		//Just based on the order of putting the elements to the HashMap to create element for now
		//
		//String subKey[] = keyString.split("_"); //TO-Do: change the separator to ":"
		//String orderString = subKey[1];
		
		VideoView videoView = new VideoView(this.context);
		File videoFile = new File(Environment.getExternalStorageDirectory(), urlString);
		videoView.setVideoURI(Uri.fromFile(videoFile));
		
	    videoView.setLayoutParams(new LayoutParams(
	    		LayoutParams.MATCH_PARENT,
	    		LayoutParams.MATCH_PARENT));
	    this.layout.addView(videoView);	
	}
	


}
