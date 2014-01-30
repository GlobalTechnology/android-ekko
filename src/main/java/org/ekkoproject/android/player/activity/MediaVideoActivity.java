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

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.services.ResourceManager;

import java.io.File;

public class MediaVideoActivity extends Activity implements MediaPlayer.OnCompletionListener {
    private static final String EXTRA_RESOURCEID = MediaVideoActivity.class.getName() + ".EXTRA_RESOURCEID";

    private static final String STATE_PLAYING = MediaVideoActivity.class.getName() + ".STATE_PLAYING";

    private long mCourseId = INVALID_COURSE;
    private String mResourceId = null;

    private ResourceManager mResources = null;

    private boolean mPlaying = true;

    // Views
    private VideoView mVideoPlayer = null;

    public static Intent newIntent(final Context context, final long courseId, final String resourceId) {
        final Intent intent = new Intent(context, MediaVideoActivity.class);
        intent.putExtra(EXTRA_COURSEID, courseId);
        intent.putExtra(EXTRA_RESOURCEID, resourceId);
        return intent;
    }

    /** BEGIN lifecycle */

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
        }

        this.setupVideoPlayer();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // the video shouldn't be playing when we restart this activity
        mPlaying = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPlaying && mVideoPlayer != null && !mVideoPlayer.isPlaying()) {
            mVideoPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mVideoPlayer != null && mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle savedState) {
        super.onSaveInstanceState(savedState);

        savedState.putBoolean(STATE_PLAYING, mPlaying);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mVideoPlayer != null) {
            mVideoPlayer.stopPlayback();
        }
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        finish();
    }

    /** END lifecycle */

    private void setupVideoPlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.setMediaController(new MediaController(this));
            mVideoPlayer.setOnCompletionListener(this);

            new LoadVideoAsyncTask().execute();
        }
    }

    private void findViews() {
        mVideoPlayer = findView(VideoView.class, R.id.video);
    }

    private <T extends View> T findView(final Class<T> clazz, final int id) {
        final View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    private class LoadVideoAsyncTask extends AsyncTask<Void, Void, Uri> {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public final AsyncTask<Void, Void, Uri> execute() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return this.executeOnExecutor(THREAD_POOL_EXECUTOR);
            } else {
                return this.execute(new Void[] {});
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
}
