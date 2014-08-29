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
import android.util.Pair;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import org.arclight.eventtracker.EventTracker;
import org.arclight.eventtracker.PlayEventReport;
import org.ccci.gto.android.common.widget.StateAwareVideoView;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.services.ResourceManager;

import java.io.File;

public class MediaVideoActivity extends Activity {
    private static final String EXTRA_RESOURCEID = MediaVideoActivity.class.getName() + ".EXTRA_RESOURCEID";

    private static final String STATE_PLAYING = MediaVideoActivity.class.getName() + ".STATE_PLAYING";
    private static final String STATE_POSITION = MediaVideoActivity.class.getName() + ".STATE_POSITION";

    private long mCourseId = INVALID_COURSE;
    private String mResourceId = null;

    EventTracker mEventTracker = null;
    private ResourceManager mResources = null;

    private boolean mPlaying = true;
    private int mPos = -1;

    // Views
    private View mRoot = null;
    private VideoView mVideoPlayer = null;
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

        mEventTracker = EventTracker.getInstance();
        mResources = ResourceManager.getInstance(this);

        final Intent intent = getIntent();
        mCourseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        mResourceId = intent.getStringExtra(EXTRA_RESOURCEID);

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
        mVideoPlayer = findView(VideoView.class, R.id.video);
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

    private class LoadVideoAsyncTask extends AsyncTask<Void, Void, Pair<Resource, Uri>> {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public final AsyncTask<Void, Void, Pair<Resource, Uri>> execute() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return super.executeOnExecutor(THREAD_POOL_EXECUTOR);
            } else {
                return super.execute();
            }
        }

        @Override
        protected Pair<Resource, Uri> doInBackground(final Void... params) {
            // resolve the resource
            final Resource resource = mResources.resolveResource(mCourseId, mResourceId);

            // check to see if the file has been downloaded already
            File file = mResources.getFile(resource, FLAG_DONT_DOWNLOAD);
            if (file != null) {
                return Pair.create(resource, Uri.parse(file.getAbsolutePath()));
            }

            // try streaming the file directly
            final Uri streamUri = mResources.getStreamUri(resource);
            if (streamUri != null) {
                return Pair.create(resource, streamUri);
            }

            // try downloading the resource now
            file = mResources.getFile(resource);
            if (file != null) {
                return Pair.create(resource, Uri.parse(file.getAbsolutePath()));
            }

            // no video found
            return null;
        }

        @Override
        protected void onPostExecute(final Pair<Resource, Uri> video) {
            super.onPostExecute(video);

            // configure the video player if we have one
            if (mVideoPlayer != null && video.second != null) {
                // set the video file on the player
                mVideoPlayer.setVideoURI(video.second);

                // for Arclight videos we need to monitor playback events (if possible)
                if (video.first != null && video.first.isArclight() && mVideoPlayer instanceof StateAwareVideoView) {
                    ((StateAwareVideoView) mVideoPlayer).setPlaybackListener(
                            new StateAwareVideoView.SimplePlaybackListener() {
                                private final PlayEventReport mReport = mEventTracker
                                        .createPlayEventReport(video.first.getRefId(),
                                                               video.second.getQueryParameter("apiSessionId"), true);
                                private double mTotalMediaLengthInMilliseconds = 0;

                                @Override
                                public void onStart(final StateAwareVideoView view) {
                                    mEventTracker.playStarted(mReport);
                                    mTotalMediaLengthInMilliseconds = mVideoPlayer.getDuration();
                                }

                                @Override
                                public void onPause(final StateAwareVideoView view) {
                                    mEventTracker.playPaused(mReport);
                                }

                                @Override
                                public void onResume(StateAwareVideoView view) {
                                    mEventTracker.playResumed(mReport);
                                }

                                @Override
                                public void onStop(final StateAwareVideoView view) {
                                    mEventTracker.playStopped(mReport, mTotalMediaLengthInMilliseconds);
                                }
                            }
                    );
                }
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
