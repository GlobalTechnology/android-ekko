package org.appdev.entity;

import java.io.Serializable;
import java.util.List;

public class Resource implements Serializable{


	private String id;
	private String sha1;
	private long size;
	private String file;
	private String type;
	private String provider;
	private List<Resource> items;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceSha1() {
		return sha1;
	}

	public void setResourceSha1(String sha1) {
		this.sha1 = sha1;
	}

	public long getResourceSize() {
		return size;
	}

	public void setResourceSize(long size) {
		this.size = size;
	}

	public String getResourceFile() {
		return file;
	}

	public void setResourceFile(String file) {
		this.file = file;
	}

	public String getResourceType() {
		return type;
	}

	public void setResourceType(String type) {
		this.type = type;
	}

	public List<Resource> getItems() {
		return items;
	}

	public void setItems(List<Resource> items) {
		this.items = items;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}


	
}
