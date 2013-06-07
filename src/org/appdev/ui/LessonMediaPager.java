package org.appdev.ui;

import java.io.File;
import java.util.List;

import org.appdev.R;
import org.appdev.app.AppContext;
import org.appdev.entity.Course;
import org.appdev.entity.Lesson;
import org.appdev.entity.Media;
import org.appdev.entity.Resource;
import org.appdev.utils.FileUtils;
import org.appdev.utils.StringUtils;
import org.appdev.utils.UIController;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * Lesson Media content pager class
 *
 */
public class LessonMediaPager extends Fragment implements OnTouchListener, android.support.v4.app.FragmentManager.OnBackStackChangedListener{
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "MediaPager";

    /**
     * The fragment's page number, which is set to the argument value .
     */
    private int mPageNumber;
    private static List<Drawable> imageList;
    private boolean mDisplayPicBack = false;
  
    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static Fragment create(int pageNumber, List<Drawable> imageList) {
        LessonMediaPager fragment = new LessonMediaPager();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        LessonMediaPager.imageList = imageList;
        return fragment;
    }

    public LessonMediaPager() {
    	
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
        getActivity().getWindow().clearFlags(WindowManager
    			.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	  
      	
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.lesson_media_pager, container, false);
        
        FrameLayout  layout = (FrameLayout)rootView.findViewById(R.id.lessonMediaContent);
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
        
        //get the Image resource
        ImageView thumbnail = ((ImageView) rootView.findViewById(R.id.lesson_media_thumbnail));
        thumbnail.setImageDrawable(imageList.get(mPageNumber<(imageList.size()-1)?mPageNumber:(imageList.size()-1)));
   
        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
          
        } else {
            mDisplayPicBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        
        thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(AppContext.getInstance(), "clicked:" + mPageNumber, 200).show();
				//zoom in or play video based the resource type of the thumbnail
		        //get the current lesson
		        Course curCourse = AppContext.getInstance().getCurCourse();
		        if(curCourse != null) {
                    Lesson lesson = AppContext.getInstance().getCurLesson();
			        if(mPageNumber> lesson.getLessonMedia().getElements().size()-1) {
			        	mPageNumber = lesson.getLessonMedia().getElements().size()-1;		        	
			        }
			        Media media =  lesson.getLessonMedia().getElements().get(mPageNumber);
					
                    final Resource resource = curCourse.getResource(media.getMediaResourceID());
                    if (resource == null) {
                        UIController.ToastMessage(v.getContext(), "The requested media could not be found", 200);
                    } else if (resource.isSupportedVideoType()) {
							//play video
                        String videoURL = resource.getResourceFile();
                        final String videoFile = FileUtils.EkkoCourseSetRootPath() + Long.toString(curCourse.getId())
                                + "/" + videoURL;
							File file = new File(videoFile);
							if(file.exists()){
								UIController.playVideo(AppContext.getInstance(), videoFile);
							} else if(!StringUtils.isEmpty(resource.getResourceURI(curCourse.getCourseURI()))){//streaming 
								UIController.playVideo(AppContext.getInstance(), resource.getResourceURI(curCourse.getCourseURI()));
							}
							
						}else if(resource.isSupportedImageType()){
							//zoom in picture or flip the picture to show description
							//flipPic();
							//zoom pic
                        String picURL = resource.getResourceFile();
                        UIController.showImageZoomDialog(v.getContext(),
                                FileUtils.EkkoCourseSetRootPath() + Long.toString(curCourse.getId()) + "/" + picURL);
						}else{
							UIController.ToastMessage(v.getContext(), "The media format is not supported", 200);
						}
						
					
				}
				
				

			}
		});
        
      
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
	
	

	@Override
	public void onBackStackChanged() {
		// TODO Auto-generated method stub
		mDisplayPicBack = (getFragmentManager().getBackStackEntryCount() > 0);
	}
	
    /**
     * A fragment representing the back of the media(picture).
     */
    public static class PictureBackFragment extends Fragment {
        public PictureBackFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_picture_back, container, false);
        }
    }
    
    private void flipPic() {
        if (mDisplayPicBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mDisplayPicBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing
                // rotations when switching to the back of the card, as well as animator
                // resources representing rotations when flipping back to the front (e.g. when
                // the system Back button is pressed).
                .setCustomAnimations(
                        R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                        R.anim.card_flip_left_in, R.anim.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment
                // representing the next page (indicated by the just-incremented currentPage
                // variable).
                .replace(R.id.lessonMediaContent, new PictureBackFragment())

                // Add this transaction to the back stack, allowing users to press Back
                // to get to the front of the card.
                //.addToBackStack(null)

                // Commit the transaction.
                .commit();

       
    }
}
