package org.appdev.entity;

import java.io.Serializable;


public abstract class Base implements Serializable {

	public final static String UTF8 = "UTF-8";
	
	protected Notice notice;

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}

}
