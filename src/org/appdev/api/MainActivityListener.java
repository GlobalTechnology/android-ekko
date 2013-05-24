package org.appdev.api;

public interface MainActivityListener {
	
	/**
	 * Callback: reload the data/update the UI because the event,such as click in Main Activity
	 */
	public void updateLessonFocus();
	public void updateMediaList();
	public void updateProgressBar();	

}
