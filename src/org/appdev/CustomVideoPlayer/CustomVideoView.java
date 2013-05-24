package org.appdev.CustomVideoPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

//	private String referenceId;

	private CustomVideoViewOnStopCallback callback;
	
	public CustomVideoView(Context context) {
		super(context);
    
	}
	
	public CustomVideoView(Context context, AttributeSet attrs){
		super(context,attrs);
	}

	public CustomVideoView(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs, defStyle);
	}
	
	public void setCallback(CustomVideoViewOnStopCallback callback){
		this.callback = callback;
	}
	
	@Override
	public void pause() {
		super.pause();
		
		if(callback != null){
			callback.OnVideoPaused(getCurrentPosition());
		}
	}
	

	@Override
	public void start() {
		super.start();
		if(callback != null){
			callback.OnPlay(getCurrentPosition());
		}
		
	}
	

	@Override
	public void stopPlayback() {
		
		if(callback != null){
			callback.OnStop(getCurrentPosition());
		}
		
		super.stopPlayback();
	}

//	public void setRefferenceID(String refID){
//		referenceId = refID;
//	}
	
	
}
