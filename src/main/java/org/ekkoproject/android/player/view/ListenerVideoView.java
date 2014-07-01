package org.ekkoproject.android.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ListenerVideoView extends VideoView {

    private VideoViewListener mDelegate;

    public ListenerVideoView(Context context) {
        super(context);
    }

    public ListenerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListenerVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setVideoViewListener(VideoViewListener listener) {
        mDelegate = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (mDelegate != null) {
            mDelegate.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mDelegate != null) {
            mDelegate.onPlay();
        }
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        if (mDelegate != null) {
            mDelegate.onStop();
        }
    }

    public interface VideoViewListener {
        void onPlay();

        void onPause();

        void onStop();
    }

}
