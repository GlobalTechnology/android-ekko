package org.appdev.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Media implements Serializable{


	private String media_resource;
	private String media_thumbnail;	

	
	public String getMediaResourceID() {
		return media_resource;
	}
	
	public void setMediaResourcelID(String media_resource) {
		this.media_resource = media_resource;
	}
	
	public String getMediaThumbnailID() {
		return media_thumbnail;
	}
	
	public void setMediaThumbnailID(String media_thumbnail) {
		this.media_thumbnail = media_thumbnail;
	}
	
}
