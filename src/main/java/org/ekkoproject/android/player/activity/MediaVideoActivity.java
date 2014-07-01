package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.services.ResourceManager.FLAG_DONT_DOWNLOAD;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jesusfilmmedia.eventtracker.EventTracker;
import com.jesusfilmmedia.eventtracker.PlayEventReport;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.services.ResourceManager;
import org.ekkoproject.android.player.view.ListenerVideoView;

import java.io.File;

public class MediaVideoActivity extends Activity {
    private static final String EXTRA_RESOURCEID = MediaVideoActivity.class.getName() + ".EXTRA_RESOURCEID";

    private static final String STATE_PLAYING = MediaVideoActivity.class.getName() + ".STATE_PLAYING";
    private static final String STATE_POSITION = MediaVideoActivity.class.getName() + ".STATE_POSITION";

    private long mCourseId = INVALID_COURSE;
    private String mResourceId = null;

    private ResourceManager mResources = null;

    private boolean mPlaying = true;
    private int mPos = -1;

    private PlayEventReport mPlayEventReport = null;
    private double mTotalMediaLengthInMilliseconds;

    // Views
    private View mRoot = null;
    private ListenerVideoView mVideoPlayer = null;
    private MediaController mController = null;

    public static Intent newIntent(final Context context, final long courseId, final String resourceId) {
        final Intent intent = new Intent(context, MediaVideoActivity.class);
        intent.putExtra(EXTRA_COURSEID, courseId);
        intent.putExtra(EXTRA_RESOURCEID, resourceId);
        return intent;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        this.setContentView(R.layout.activity_media_video);
        this.findViews();

        final Intent intent = getIntent();
        mCourseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        mResourceId = intent.getStringExtra(EXTRA_RESOURCEID);

        mResources = ResourceManager.getInstance(this);

        // process saved state
        if (savedState != null) {
            mPlaying = savedState.getBoolean(STATE_PLAYING, mPlaying);
            mPos = savedState.getInt(STATE_POSITION, mPos);
        }

        this.setupVideoPlayer();

        new LoadVideoAsyncTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // seek back to the position we last stopped at
        this.seekToPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start the video player if needed
        if (mPlaying && mVideoPlayer != null && !mVideoPlayer.isPlaying()) {
            mVideoPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mVideoPlayer != null) {
            mPlaying = mVideoPlayer.isPlaying();
            mVideoPlayer.pause();
        }
        this.getPosition();
    }

    @Override
    protected void onSaveInstanceState(final Bundle savedState) {
        super.onSaveInstanceState(savedState);

        // store save state data
        savedState.putBoolean(STATE_PLAYING, mPlaying);
        savedState.putInt(STATE_POSITION, mPos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stop the video player
        if (mVideoPlayer != null) {
            mVideoPlayer.stopPlayback();
        }

        this.clearViews();
    }

    /* END lifecycle */

    private void setupVideoPlayer() {
        if (mVideoPlayer != null) {
            // MediaPlayer callback listener
            final MediaPlayerListener listener = new MediaPlayerListener();
            mVideoPlayer.setOnCompletionListener(listener);

            mVideoPlayer.setVideoViewListener(new ListenerVideoView.VideoViewListener() {
                @Override
                public void onPlay() {
                    if(mPlayEventReport != null) {
                        EventTracker.getInstance().playStarted(mPlayEventReport);
                        mTotalMediaLengthInMilliseconds = mVideoPlayer.getDuration();
                    }
                }

                @Override
                public void onPause() {
                    if(mPlayEventReport != null) {
                        EventTracker.getInstance().playPaused(mPlayEventReport);
                    }
                }

                @Override
                public void onStop() {
                    if(mPlayEventReport != null) {
                        EventTracker.getInstance().playStopped(mPlayEventReport, mTotalMediaLengthInMilliseconds);
                    }
                }
            });

            // MediaPlayer controller
            mController = new MediaController(this);
            mVideoPlayer.setMediaController(mController);

            // show the MediaPlayer controller on any click event
            if (mRoot != null) {
                mRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if (mController != null) {
                            mController.show();
                        }
                    }
                });
            }
        }
    }

    private void findViews() {
        mRoot = findView(View.class, android.R.id.content);
        mVideoPlayer = findView(ListenerVideoView.class, R.id.video);
    }

    private void clearViews() {
        mRoot = null;
        mVideoPlayer = null;
        mController = null;
    }

    private <T extends View> T findView(final Class<T> clazz, final int id) {
        final View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    private void getPosition() {
        if (mVideoPlayer != null) {
            mPos = mVideoPlayer.getDuration() > -1 ? mVideoPlayer.getCurrentPosition() : -1;
        }
    }

    private void seekToPosition() {
        // only seek if we have a video player, it's not playing, and we have a valid position different
        // than the current position
        if (mVideoPlayer != null && !mVideoPlayer.isPlaying() && mPos != -1 &&
                mPos != mVideoPlayer.getCurrentPosition()) {
            mVideoPlayer.seekTo(mPos);
        }
    }

    private class LoadVideoAsyncTask extends AsyncTask<Void, Void, Uri> {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public final AsyncTask<Void, Void, Uri> execute() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return super.executeOnExecutor(THREAD_POOL_EXECUTOR);
            } else {
                return super.execute();
            }
        }

        @Override
        protected Uri doInBackground(final Void... params) {
            // resolve the resource
            final Resource resource = mResources.resolveResource(mCourseId, mResourceId);

            // check to see if the file has been downloaded already
            File file = mResources.getFile(resource, FLAG_DONT_DOWNLOAD);
            if (file != null) {
                return Uri.parse(file.getAbsolutePath());
            }

            // try streaming the file directly
            final Uri streamUri = mResources.getStreamUri(resource);
            if (streamUri != null) {
                if(resource.isArclight()) {
                    String sessionId = streamUri.getQueryParameter("apiSessionId");
                    mPlayEventReport = EventTracker.getInstance().createPlayEventReport(resource.getRefId(), sessionId, true);
                }
                return streamUri;
            }

            // try downloading the resource now
            file = mResources.getFile(resource);
            if (file != null) {
                return Uri.parse(file.getAbsolutePath());
            }

            // no video uri found
            return null;
        }

        @Override
        protected void onPostExecute(final Uri uri) {
            super.onPostExecute(uri);

            // set the video file on the player
            if (mVideoPlayer != null) {
                mVideoPlayer.setVideoURI(uri);
            }
        }
    }

    private class MediaPlayerListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(final MediaPlayer mp) {
            finish();
        }
    }
}
