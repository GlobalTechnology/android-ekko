package org.appdev.entity;

import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;


public class SOAHUB implements Serializable {
	
	public final static String HOST = "www.ekko.org";
	public final static String URL_EKKO_AUTH_SERVICE = "https://servicesdev.gcx.org/ekko/auth/service";
	
	public final static String URL_EKKO_CAS_LOGIN = "https://casdev.gcx.org/cas/login";	
	public final static String URL_EKKO_ROOT_SERVICE = "https://servicesdev.gcx.org/ekko/";
	public final static String URL_EKKO_LOGIN_URL = "https://servicesdev.gcx.org/ekko/auth/login?";
	
	public final static String URL_EKKO_LOGIN_URL_PRODUCTION = "https://services.gcx.org/ekko/auth/login?";
	public final static String URL_EKKO_ROOT_SERVICE_PRODUCTION = "https://services.gcx.org/ekko/";
	public final static String URL_EKKO_AUTH_SERVICE_PRODUCTION = "https://services.gcx.org/ekko/auth/service";
	
	public final static String URL_REL_COURSELIST = "/courses";
	
	public final static long CLIENT_ID = 85613451684391165L;
	
	public final static String HTTP = "http://";
	public final static String HTTPS = "https://";
	
	public final static String HOST_CAS = "thekey.org";
	
	
	private final static String URL_SPLITTER = "/";
	private final static String URL_UNDERLINE = "_";
	
	private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;
	public final static String LOGIN_VALIDATE_HTTP = HTTP + HOST + URL_SPLITTER + "action/api/login_validate";
	public final static String LOGIN_VALIDATE_HTTPS = HTTPS + HOST + URL_SPLITTER + "action/api/login_validate";

	public final static String PORTRAIT_UPDATE = URL_API_HOST+"action/api/portrait_update";
	public final static String UPDATE_VERSION = "http://www.liudev.com/ekkoupdate.xml";	

	
	public final static int URL_OBJ_TYPE_OTHER = 0x000;
	public final static int URL_OBJ_TYPE_AUTH = 0x001;

	
	private int objId;
	private String objKey = "";
	private int objType;
	
	public int getObjId() {
		return objId;
	}
	public void setObjId(int objId) {
		this.objId = objId;
	}
	public String getObjKey() {
		return objKey;
	}
	public void setObjKey(String objKey) {
		this.objKey = objKey;
	}
	public int getObjType() {
		return objType;
	}
	public void setObjType(int objType) {
		this.objType = objType;
	}
	
	/**

	 */
	public final static SOAHUB parseURL(String path) {

		return null;

	}

	
	/**

	 */
	private final static String formatURL(String path) {
		if(path.startsWith("http://") || path.startsWith("https://"))
			return path;
		return "http://" + URLEncoder.encode(path);
	}
	
	/**
	 * 
	 */
	
	private final static String generateURL(String path, String session) {
		 return null;
	}
}
