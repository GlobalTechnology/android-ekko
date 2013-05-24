package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;

import org.appdev.app.AppException;
import org.appdev.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 
 */
public class User {
		
	private int uid;
	private String location;
	private String name;

	private String face;
	private String account;
	private String pwd;
	private Result validate;
	private boolean isRememberMe;
	
	private String jointime;
	private String gender;

	private String latestonline;
	private String sessionId;
	
	
	public boolean isRememberMe() {
		return isRememberMe;
	}
	public void setRememberMe(boolean isRememberMe) {
		this.isRememberMe = isRememberMe;
	}
	public String getJointime() {
		return jointime;
	}
	public void setJointime(String jointime) {
		this.jointime = jointime;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getLatestonline() {
		return latestonline;
	}
	public void setLatestonline(String latestonline) {
		this.latestonline = latestonline;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public Result getValidate() {
		return validate;
	}
	public void setValidate(Result validate) {
		this.validate = validate;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public static User parse(InputStream stream) throws IOException, AppException {
		User user = new User();
		Result res = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, "UTF-8");
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {

				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("result")) {
						res = new Result();
					} else if (tag.equalsIgnoreCase("errorCode")) {
						res.setErrorCode(StringUtils.toInt(xmlParser.nextText(), -1));
					} else if (tag.equalsIgnoreCase("errorMessage")) {
						res.setErrorMessage(xmlParser.nextText().trim());
					} else if (res != null && res.OK()) {
						if(tag.equalsIgnoreCase("uid")){
							user.uid = StringUtils.toInt(xmlParser.nextText(), 0);
						}else if(tag.equalsIgnoreCase("location")){
							user.setLocation(xmlParser.nextText());
						}else if(tag.equalsIgnoreCase("name")){
							user.setName(xmlParser.nextText());
						}else if(tag.equalsIgnoreCase("portrait")){
							user.setFace(xmlParser.nextText());
						}			     
			      
					}
					break;
				case XmlPullParser.END_TAG:
					//如果遇到标签结束，则把对象添加进集合中
			       	if (tag.equalsIgnoreCase("result") && res != null) { 
			       		user.setValidate(res);
			       	}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}

		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return user;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
