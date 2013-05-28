package org.appdev.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.appdev.app.AppConfig;
import org.appdev.entity.AccessInfo;
import org.appdev.utils.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 */
public class AppConfig{
	
	private final static String APP_CONFIG = "config";

	public final static String CONF_APP_UNIQUEID = "APP_";
	public final static String CONF_COOKIE = "cookie";
	public final static String CONF_ACCESSTOKEN = "accessToken";
	public final static String CONF_ACCESSSECRET = "accessSecret";
	public final static String CONF_EXPIRESIN = "expiresIn";
	public final static String CONF_DISPLAY_IMAGE = "perf_loadimage";
	public final static String CONF_SCROLL = "perf_scroll";
	public final static String CONF_PRODUCTION_ENV = "perf_production";
	public final static String CONF_VOICE = "perf_voice";
	public final static String CONF_CHECKUP = "perf_checkup";
	public final static String CONF_COURSES_DIR = "perf_coursedir";
	
	
	private Context mContext;
	private AccessInfo accessInfo = null;
	private static AppConfig appConfig;
	
	public static AppConfig getAppConfig(Context context)
	{
		if(appConfig == null){
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}
	
	/**
	 * Get Preference Setting
	 */
	public static SharedPreferences getSharedPreferences(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 *  Get Image Displaying setting
	 */
	public static boolean isLoadImage(Context context)
	{
		return getSharedPreferences(context)
				.getBoolean(CONF_DISPLAY_IMAGE, true);
	}	
	
	public String getCookie(){
		return get(CONF_COOKIE);
	}

	public void setAccessToken(String accessToken){
		set(CONF_ACCESSTOKEN, accessToken);
	}
	
	public String getAccessToken(){
		return get(CONF_ACCESSTOKEN);
	}
	
	public void setAccessSecret(String accessSecret){
		set(CONF_ACCESSSECRET, accessSecret);
	}
	
	public String getAccessSecret(){
		return get(CONF_ACCESSSECRET);
	}
	
	public void setExpiresIn(long expiresIn){
		set(CONF_EXPIRESIN, String.valueOf(expiresIn));
	}
	
	public long getExpiresIn(){
		return StringUtils.toLong(get(CONF_EXPIRESIN));
	}
	
	public void setAccessInfo(String accessToken, String accessSecret, long expiresIn)
	{
		if(accessInfo == null)
			accessInfo = new AccessInfo();
		accessInfo.setAccessToken(accessToken);
		accessInfo.setAccessSecret(accessSecret);
		accessInfo.setExpiresIn(expiresIn);
		//save
		this.setAccessToken(accessToken);
		this.setAccessSecret(accessSecret);
		this.setExpiresIn(expiresIn);
	}
	
	public AccessInfo getAccessInfo()
	{
		if(accessInfo == null && !StringUtils.isEmpty(getAccessToken()) && !StringUtils.isEmpty(getAccessSecret()))
		{
			accessInfo = new AccessInfo();
			accessInfo.setAccessToken(getAccessToken());
			accessInfo.setAccessSecret(getAccessSecret());
			accessInfo.setExpiresIn(getExpiresIn());
		}
		return accessInfo;
	}
	
	public String get(String key)
	{
		Properties props = get();
		return (props!=null)?props.getProperty(key):null;
	}
	
	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try{
			
			//fis = activity.openFileInput(APP_CONFIG);
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator + APP_CONFIG);
			
			props.load(fis);
		}catch(Exception e){
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return props;
	}
	
	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try{
			//fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);
			
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);
			
			p.store(fos, null);
			fos.flush();
		}catch(Exception e){	
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}

	public void set(Properties ps)
	{
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}
	
	public void set(String key,String value)
	{
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}
	
	public void remove(String...key)
	{
		Properties props = get();
		for(String k : key)
			props.remove(k);
		setProps(props);
	}
}
