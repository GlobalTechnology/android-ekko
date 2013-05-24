package org.appdev.CustomVideoPlayer;

public interface CustomVideoViewOnStopCallback {
	public void OnStop(int currentPlayPosition);
	public void OnPlay(int currentPlayPosition);
	public void OnVideoPaused(int currentPlayPosition);
}
