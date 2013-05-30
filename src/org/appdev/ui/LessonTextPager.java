package org.appdev.ui;

import java.util.List;

import org.appdev.R;
import org.appdev.api.TextViewZoomListener;
import org.appdev.app.AppContext;
import org.appdev.entity.Course;
import org.appdev.entity.Lesson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;



/**
 * Lesson Text content pager class
 *
 */
public class LessonTextPager extends Fragment implements OnTouchListener {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "TEXTPager";

    /**
     * The fragment's page number, which is set to the argument value .
     */
    private int mPageNumber;
  
    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static Fragment create(int pageNumber) {
        LessonTextPager fragment = new LessonTextPager();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LessonTextPager() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        getActivity().getWindow().clearFlags(WindowManager
    			.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	  
      	
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.lesson_text_pager, container, false);
        
   
        ScrollView  layout = (ScrollView)rootView.findViewById(R.id.lessonTextContent);
        layout.requestDisallowInterceptTouchEvent(true);
        layout.setOnTouchListener(new OnTouchListener() {
        	
        	@Override
        	public boolean onTouch(View v, MotionEvent event) {
        		switch (event.getAction()){
        			case MotionEvent.ACTION_MOVE:
        				//next page
        				
        				break;
    				default:
    					break;
        		}
        		return false;
        	}
        });
        
        //get the current lesson
        Course curCourse = AppContext.getInstance().getCurCourse();
        int curLesson = AppContext.getInstance().getCurrentLessonIndex();
        
        Lesson lesson = curCourse.getLessonList().get(curLesson);        

        // Set the title view to show the page number.
        ((TextView) rootView.findViewById(R.id.lessonTitle)).setText(
        		lesson.getLesson_title());
        final List<String> lessonText = lesson.getText();
        if (mPageNumber > lessonText.size() - 1) {
            mPageNumber = lessonText.size() - 1;
        }
        
       TextView textview = (TextView) rootView.findViewById(R.id.lessonPageText);
        textview.setText(Html.fromHtml(lessonText.get(mPageNumber)));
       textview.setOnTouchListener(new TextViewZoomListener());
        
       
//        linearLayout.addView(imageView);
        
/*        TextView tView = new TextView(rootView.getContext());
        tView.setText("dynamic layout text");
        
        linearLayout.addView(tView);*/
        
        //add text dynamcally
 
    
        return rootView;
    }


    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
