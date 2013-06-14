package org.appdev.CustomVideoPlayer;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.appdev.view.BaseActivity;
import org.appdev.widget.AlertDialogStatic;
import org.ekkoproject.android.player.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class VideoPlayer extends BaseActivity implements OnPreparedListener, OnCompletionListener,
														 CustomVideoViewOnStopCallback, OnSeekBarChangeListener,
														 OnBufferingUpdateListener {

	private CustomVideoView videoHolder;
	private MediaController mediaControllerHolder;
	private Uri videoUri;
//	private GoogleAnalyticsTracker tracker;
	private String refID;
	private String playerCode;
	private String apiSessionId;
	private int currentPosition;
	private int duration;
	private int seekToPosition = -1;
	private int bufferPercent = -1;
	private long totalPlayTime = 0;
	private long timePlayStarted = 0;
	private Timer loadingTimeout = null;
	private PhoneStateReceiver mPhoneStateReceiver = null;
	private boolean isInitialGABeenTriggered = false;
	private boolean mIsPhoneStateReceiverRegistered = false;
	private boolean mPlayMode = false;
	private SeekBar seekBar = null;

	private static final int LOADING_TIMEOUT_SEC = 30;
	
	public class PhoneStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
	        	if (videoHolder != null && videoHolder.isPlaying()) {
		        	TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		        	int state = tm.getCallState();
		        	if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
		        		videoHolder.pause();
		        	}
	        	}
	        }
		}
	}
	
	private class AnalyticsActions {
		public static final String FRAME_WIDTH     = "renditionFrameWidth";
		public static final String FRAME_HEIGHT    = "renditionFrameHeight";
		public static final String ENCODING_RATE   = "renditionEncodingRate";
		public static final String MEDIA_PLAY      = "mediaPlay";
		public static final String MEDIA_STOP      = "mediaStop";
		public static final String MEDIA_VIEW_TIME = "mediaViewTime";
		public static final String MEDIA_OVER_75   = "mediaEngagementOver75Percent";
		public static final String MEDIA_COMPLETE  = "mediaComplete";
		public static final String IS_STREAMING    = "isStreaming";
		public static final String DEVICE_OS       = "deviceOS";
		public static final String DEVICE_NAME     = "deviceName";
		public static final String DEVICE_FAMILY   = "deviceFamily";
		public static final String DOMAIN          = "Domain";
		public static final String LATITUDE        = "latitude";
		public static final String LONGITUDE       = "longitude";
	}
	
	public static final int PLAYCOMPLETE = 123456;
	protected static final int TIMEOUT_MESSAGE = 342;
	
	public static Intent createIntent(Context context,String refID, String URL){
		Intent intent = new Intent(context, VideoPlayer.class);
		intent.putExtra("REFID", refID);
		intent.putExtra("URL", URL);	
		
		return intent;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videoplayerview);

		registerReceivers();
		
		Log.d("Analytics", "======= NEW VIDEO EVENT =======");
		
		playerCode = getIntent().getStringExtra("URL");
		
		getWindow().setFormat(PixelFormat.UNKNOWN);
		
		//the VideoView will hold the video
	    videoHolder = (CustomVideoView)findViewById(R.id.CustomVideoView1);
	    
	    refID = getIntent().getStringExtra("REFID");
	    Log.i("Arthur", "refID = "+refID);
	    
	    apiSessionId = getIntent().getStringExtra("SESSIONID");
	    
	    //MediaController is the ui control hovering above the video (just like in the default youtube player).
	    mediaControllerHolder = new MediaController(this);
	    mediaControllerHolder.setAnchorView(videoHolder);
	    videoHolder.setMediaController(mediaControllerHolder);
	    
	    boolean is_streaming = true;
	    boolean isDownloadHigh = false;
	    boolean isDownloadLow = false;
	    
	    //First we check if the video has been downloaded locally, if so, just play the file

	    if(playerCode.startsWith("http")){
	    	videoUri = Uri.parse(playerCode);
	    }else{
	    	File videoFile = new File(playerCode);
	    	if (videoFile.exists()){
	    		is_streaming = false;
	    		videoUri = Uri.fromFile(videoFile);
	    	}
	    }
	    
 /*   	if (isDownloadHigh || isDownloadLow) {
    	
    		
    		is_streaming = false;
    	}
    	else {
    		videoUri = Uri.parse(playerCode);
    	}*/
    	
	    //Initialize Google Analytics
//	    tracker = GoogleAnalyticsTracker.getInstance();
	    
		// Start the tracker in manual dispatch mode...
//        tracker.startNewSession(Constants.getAnalyticsId(), Constants.GA_DISPATCH_INTERVAL, this);
        
	    videoHolder.setOnPreparedListener(this);
	    videoHolder.setOnCompletionListener(this);
	    videoHolder.setCallback(this);
	    
	    //get focus, before playing the video.
	    videoHolder.requestFocus();
	    
		showLoadingProgress(true);
		
		if (videoHolder != null && videoUri != null) {
			trackEvent(AnalyticsActions.IS_STREAMING, String.valueOf(is_streaming), 0);
		    videoHolder.setVideoURI(videoUri);
		    loadingTimeout = new Timer();
		    TimerTask task = new TimerTask() {
	
				@Override
				public void run() {
					Message msg = Message.obtain();
					msg.what = TIMEOUT_MESSAGE;
					mHandler.sendMessage(msg);
				}
		    	
		    };
		    loadingTimeout.schedule(task, LOADING_TIMEOUT_SEC * 1000);
		}
	}
	
	private void registerReceivers() {
		if (!mIsPhoneStateReceiverRegistered) {
			mPhoneStateReceiver = new PhoneStateReceiver();
			IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
			this.registerReceiver(mPhoneStateReceiver, filter);
			mIsPhoneStateReceiverRegistered = true;
		}
	}
	
	private void unregisterReceivers() {
		if (mIsPhoneStateReceiverRegistered) {
			this.unregisterReceiver(mPhoneStateReceiver);
			mPhoneStateReceiver = null;
			mIsPhoneStateReceiverRegistered = false;
		}
	}
	
    private void getSeekBar() {
	    int seekbar_id = getResources().getIdentifier("mediacontroller_progress", "id", "android");
	    seekBar  = (SeekBar)mediaControllerHolder.findViewById(seekbar_id);
    }
    
    private String getTimestampString() {
    	String timestamp = "";
    	
    	double time_s = (double)System.currentTimeMillis() / 1000;
    	long rounded_time = Math.round(time_s);
    	timestamp = String.valueOf(rounded_time);
    	
    	return timestamp;
    }
    
    private void trackEvent(String action, String label, int value) {

    }
    
	private void recordPlayTime() {
		if (videoHolder != null) {
			String timestamp = getTimestampString();
			trackEvent(AnalyticsActions.MEDIA_COMPLETE, timestamp, 0);
			
			int total_play_time_sec = Math.round((float)totalPlayTime / 1000);
			trackEvent(AnalyticsActions.MEDIA_VIEW_TIME, String.valueOf(total_play_time_sec), 0);
			
			float percentage = ((float)totalPlayTime /(float)duration)*100f;
			
			trackEvent(AnalyticsActions.MEDIA_OVER_75, String.valueOf(percentage >= 75f), 0);
		}
	}
	
	@Override
	public void onBackPressed() {
		currentPosition = videoHolder.getCurrentPosition();
		videoHolder.stopPlayback();
		
		Intent intent = new Intent();
		setResult(PLAYCOMPLETE,intent);
		
		finish();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (videoHolder != null) {
			videoHolder.pause();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		unregisterReceivers();
		
		recordPlayTime();
		
		// Stop the tracker when it is no longer needed.
	 //   tracker.stopSession();
	}

	private void showLoadingProgress(boolean show) {
		RelativeLayout progress_layout = (RelativeLayout)findViewById(R.id.progress_layout);
		if (progress_layout != null) {
			progress_layout.setVisibility(show ? View.VISIBLE : View.GONE);
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if (loadingTimeout != null) {
			loadingTimeout.cancel();
			loadingTimeout = null;
		}
		showLoadingProgress(false);
		mp.setOnBufferingUpdateListener(this);
		mp.start();
		getSeekBar();
		
		OnPlay(videoHolder.getCurrentPosition());
		
	    if (seekBar != null) {
	    	seekBar.setOnSeekBarChangeListener(this);
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(currentPosition > 0) {
			videoHolder.seekTo(currentPosition);
		}
		
		if(!isInitialGABeenTriggered){
	        	//Google Analytics
				Display display = getWindowManager().getDefaultDisplay();
				int screen_width = display.getWidth();
				int screen_height = display.getHeight();
				
/*	        	trackEvent(AnalyticsActions.FRAME_WIDTH, String.valueOf(screen_width), 0);
	        	trackEvent(AnalyticsActions.FRAME_HEIGHT, String.valueOf(screen_height), 0);
	        	trackEvent(AnalyticsActions.DEVICE_OS, "Android " + Build.VERSION.RELEASE, 0);
	        	trackEvent(AnalyticsActions.DEVICE_NAME, android.os.Build.MODEL, 0);
	        	trackEvent(AnalyticsActions.DEVICE_FAMILY, android.os.Build.MANUFACTURER, 0);
	        	trackEvent(AnalyticsActions.DOMAIN, "JesusFilm Mobile", 0);*/



	        	isInitialGABeenTriggered = true;
	        }
		
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Intent intent = new Intent();
		setResult(PLAYCOMPLETE,intent);
		
		finish();
	}

	@Override
	public void OnStop(int currentPlayPosition) {
		mPlayMode = false;
		currentPosition = currentPlayPosition;
	}

	@Override
	public void OnPlay(int currentPlayPosition) {
		
		duration = videoHolder.getDuration();
		if (!mPlayMode) {
			String timestamp = getTimestampString();
			timePlayStarted = currentPlayPosition;
			int position_sec = Math.round((float)currentPlayPosition / 1000);
		//	trackEvent(AnalyticsActions.MEDIA_PLAY, timestamp, position_sec);
			
			// For some reason, OnPlay() is being called twice when the video starts. This variable
			// prevents the mediaPlay GA event from being triggered twice.
			mPlayMode = true;
			
			showLoadingProgress(false);
		}
	}

	private void checkIfBuffering() {
		if (videoHolder != null) {
			int buffer_percentage = bufferPercent;
			int current_position = videoHolder.getCurrentPosition();
			int duration = videoHolder.getDuration();
			int percentage_played = Math.round(100 * (float)current_position / (float)duration);
			int delta = buffer_percentage - percentage_played;
			
			showLoadingProgress(!videoHolder.isPlaying() && delta < 1);
		}
	}
	
	@Override
	public void OnVideoPaused(int currentPlayPosition) {
		checkIfBuffering();
		
		if (currentPlayPosition > 0) {
			currentPosition = currentPlayPosition;
		}
		
		totalPlayTime += currentPosition - timePlayStarted;
		
		String timestamp = getTimestampString();
		int play_position_sec = Math.round((float)currentPosition / 1000);
		if (mPlayMode) {
			trackEvent(AnalyticsActions.MEDIA_STOP, timestamp, play_position_sec);
		}
		mPlayMode = false;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
		currentPosition = savedInstanceState.getInt("CURRENT_POS");
		isInitialGABeenTriggered = savedInstanceState.getBoolean("isInitialGABeenTriggered");
		
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		outState.putInt("CURRENT_POS", videoHolder.getCurrentPosition());
		outState.putBoolean("isInitialGABeenTriggered", isInitialGABeenTriggered);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		seekToPosition = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		videoHolder.pause();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekToPosition >= 0) {
			int max = seekBar.getMax();
			float percentage = (float)seekToPosition / (float)max;
			int position = Math.round(percentage * (float)duration);
			videoHolder.seekTo(position);
			seekToPosition = -1;
		}
		videoHolder.start();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (percent - bufferPercent > 0) {
			bufferPercent = percent;
			Log.d("Video", "onBufferingUpdate(" + percent + ")");
		    if (seekBar != null) {
		    	seekBar.setSecondaryProgress(percent);
		    }
			checkIfBuffering();
		}
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage (Message msg) {
			Bundle data = msg.getData();
			if (data != null) {
				switch (msg.what) {
				case TIMEOUT_MESSAGE:
					DialogInterface.OnClickListener positive_callback = new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
						
					};
					DialogInterface.OnClickListener negative_callback = new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
						
					};
					AlertDialogStatic.showTwoButtonAlertDialogWithCallback(VideoPlayer.this,
																		   getString(R.string.warning),
																		   getString(R.string.video_loading_timeout_message),
																		   getString(R.string.lbl_continue),
																		   getString(R.string.cancel),
																		   positive_callback,
																		   negative_callback);
					break;
				}
			}
		}
	};
}
