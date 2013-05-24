package org.appdev.adapter;

import java.util.List;

import org.appdev.ui.LessonMediaPager;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
;


public class LessonMediaPagerAdapter extends FragmentStatePagerAdapter {  
    private List<Drawable> views = null;  

  
    public LessonMediaPagerAdapter(FragmentManager fm,  List<Drawable> views) { 
    	super(fm);
        this.views = views;  
    }  
     
    public void setImageList(List<Drawable> views){
    	this.views = views;
    }

    @Override  
    public int getCount() {  
        return views.size();  
    }  
    
    @Override
    public int getItemPosition(Object item) {
    	
         return POSITION_NONE;
        
    }

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		 return LessonMediaPager.create(position, views);
	}

} 

