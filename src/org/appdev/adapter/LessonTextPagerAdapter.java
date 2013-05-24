package org.appdev.adapter;

import org.appdev.ui.LessonTextPager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class LessonTextPagerAdapter extends FragmentStatePagerAdapter{
	private int NUM_PAGES =1;
	
	public void setPageNumber(int pageNumber) {
		this.NUM_PAGES = pageNumber;
	}
	
    public LessonTextPagerAdapter(FragmentManager fm, int pageNumber) {
    	
        super(fm);
        this.NUM_PAGES = pageNumber;
    }

    @Override
    public Fragment getItem(int position) {
    	
        return LessonTextPager.create(position);
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
    
    @Override
    public int getItemPosition(Object item) {
    
         return POSITION_NONE;
        
    }


}
