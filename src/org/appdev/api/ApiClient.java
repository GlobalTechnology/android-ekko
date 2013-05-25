package org.appdev.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URI;

import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.entity.CourseList;

import org.appdev.entity.Course;
import org.appdev.entity.CourseManifest;
import org.appdev.entity.Update;
import org.appdev.entity.User;
import org.appdev.entity.SOAHUB;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


/**
 * Client API for accessing network
 */
public class ApiClient {

	public static final String UTF_8 = "UTF-8";
	
	private final static int TIMEOUT_CONNECTION = 20000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;

	private static String appCookie;
	private static String appUserAgent;
	static DefaultHttpClient client;

	public static synchronized HttpClient getHttpClient(AppContext appContext) {

		if (client == null ) {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			//HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebkit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1" );
			HttpProtocolParams.setUserAgent(params, getUserAgent(appContext));
					
			ConnManagerParams.setTimeout(params, 1000);
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 10000);

			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

			client = new DefaultHttpClient(conMgr, params);


		}

		return client;

	}
	
	
	public static void cleanCookie() {
		appCookie = "";
	}
	
	private static String getCookie(AppContext appContext) {
		if(appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}
	
	private static String getUserAgent(AppContext appContext) {
		if(appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder("EKKO");
			ua.append('/'+appContext.getPackageInfo().versionName+'_'+appContext.getPackageInfo().versionCode);//App version
			ua.append("/Android");//手机系统平台
			ua.append("/"+android.os.Build.VERSION.RELEASE);//手机系统版本
			ua.append("/"+android.os.Build.MODEL); //手机型号
			ua.append("/"+appContext.getAppId());//客户端唯一标识
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}
	
    public static String post(String url, Map<String, String> params) {  
        DefaultHttpClient httpclient = new DefaultHttpClient();  
        String body = null;  
          
        Log.i("Apiclient_post", "create httppost:" + url);  
        HttpPost post = postForm(url, params);  
          
        body = invoke(httpclient, post);  
          
        httpclient.getConnectionManager().shutdown();  
          
        return body;  
    }  
      
    public static String get(String url) {  
        DefaultHttpClient httpclient = new DefaultHttpClient();  
        String body = null;  
          
        Log.i("ApiClient-get", "create httppost:" + url);  
        HttpGet get = new HttpGet(url);  
        body = invoke(httpclient, get);  
          
        httpclient.getConnectionManager().shutdown();  
          
        return body;  
    }  
          
      
    private static String invoke(DefaultHttpClient httpclient,  
            HttpUriRequest httpost) {  
          
        HttpResponse response = sendRequest(httpclient, httpost);  
        String body = paseResponse(response);  
          
        return body;  
    }  
  
    private static String paseResponse(HttpResponse response) {  
        Log.i("ApiClient-paseResponse"," get response from http server..");  
        HttpEntity entity = response.getEntity();  
          
        Log.i("response status: ", response.getStatusLine().toString());  
        //String charset = EntityUtils.getContentCharSet(entity);  
      
          
        String body = null;  
        try {  
            body = EntityUtils.toString(entity);  
            
        } catch (ParseException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
          
        return body;  
    }  
  
    private static HttpResponse sendRequest(DefaultHttpClient httpclient,  
            HttpUriRequest httpost) {  
        Log.i("ApiClient-sendRequest", "execute post...");  
        HttpResponse response = null;  
          
        try {  
            response = httpclient.execute(httpost);  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return response;  
    }  
  
    private static HttpPost postForm(String url, Map<String, String> params){  
          
        HttpPost httpost = new HttpPost(url);  
        List<NameValuePair> nvps = new ArrayList <NameValuePair>();  
          
        Set<String> keySet = params.keySet();  
        for(String key : keySet) {  
            nvps.add(new BasicNameValuePair(key, params.get(key)));  
        }  
          
        try {  
            Log.i("ApiClient_postForm", "set utf-8 form entity to httppost");  
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));  
        } catch (UnsupportedEncodingException e) {  
            e.printStackTrace();  
        }  
          
        return httpost;  
    }  

	
	/**
	 * get request URL
	 * @param url
	 * @throws AppException 
	 */
	
	public static InputStream getInputStreamFromUrl(String url) {
		  InputStream content = null;
		  try {
		    DefaultHttpClient httpclient = new DefaultHttpClient();
		    HttpResponse response = httpclient.execute(new HttpGet(url));
		    content = response.getEntity().getContent();
		  } catch (Exception e) {
		    Log.i("[GET REQUEST]", "Network exception", e);
		  }
		    return content;
	}
	

	/**
	 * get Bitmap from URL
	 * @param context
	 * @param targetUrl
	 * @return
	 * @throws Exception
	 */
	public static Bitmap getBitmapfromURL(Context context, String targetUrl) throws AppException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		StringBuilder urlBuilder = new StringBuilder(targetUrl);
		HttpGet request = new HttpGet(urlBuilder.toString());
		Bitmap thumbnailBmp = null;
		int time = 0;
		do{
			try {
				thumbnailBmp = httpClient.execute(request, new ResponseHandler<Bitmap>() {
	 
					public Bitmap handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						switch(response.getStatusLine().getStatusCode()) {
							case HttpStatus.SC_OK:
								return BitmapFactory.decodeStream(response.getEntity().getContent());
							case HttpStatus.SC_NOT_FOUND:
								throw new IOException("Data Not Found");
							}
						return null;
					}
				});
				
				return thumbnailBmp;
			} catch (ClientProtocolException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				httpClient.getConnectionManager().shutdown();
				httpClient = null;
			}
		}while(time<RETRY_TIME);
		return thumbnailBmp;
	}
	
	
	
	/**
	 * Check version update
	 * @param url
	 * @return
	 */
	public static Update checkVersion(AppContext appContext) throws AppException {

		return null;
	}
	
	/**
	 * Get the url of SOA Hub auth service
	 * @param appContext
	 * @param url_authService
	 * @return
	 */
	public static void getAuthService(AppContext appContext, String url_authService){

	}	

	
	 
	private static HttpPost prepareLogin(HttpGet getForm, String sLogName,String sPassword){
		try{
			HttpResponse response = client.execute(getForm);
			HttpEntity entity = response.getEntity();
			
			
			Document doc = Jsoup.parse(EntityUtils.toString(entity));
			 Element ltElement = doc.select("input[name=lt]").first();
             String loginTicket = ltElement.attr("value");
			Element form = doc.getElementById("login_form");

			String action = form.attr("action");
			action = "/cas/login";

			
			ArrayList<NameValuePair> formParams = new ArrayList<NameValuePair>();
			formParams.add(new BasicNameValuePair("username", sLogName));
			formParams.add(new BasicNameValuePair("password", sPassword));
			formParams.add(new BasicNameValuePair("lt", loginTicket));
			formParams.add(new BasicNameValuePair("execution", "e1s1"));
			formParams.add(new BasicNameValuePair("_eventId", "submit"));
			UrlEncodedFormEntity urlencodeentity = new UrlEncodedFormEntity(formParams, HTTP.UTF_8);
			
			URI getURI = getForm.getURI();
			URI postURI = new URI(getURI.getScheme(), null,getURI.getHost(), 443, action,null,null);

			HttpPost preparedRequest = new HttpPost(postURI);
			preparedRequest.setEntity(urlencodeentity);
			return preparedRequest;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Log in， Auto-process cookie
	 * @param url
	 * @param username
	 * @param pwd
	 * @return
	 * @throws AppException
	 * 
	 */
	
	public static User login(AppContext appContext, String username, String pwd) throws AppException {
		//todo: add conditions checking for the response status code 
		User user = null;

		user = new User();
		user.setSessionId(null);
				
		//String casURL="https://casdev.gcx.org/cas/login";
		String casURL = SOAHUB.URL_EKKO_CAS_LOGIN;
		
		String serviceURL = null;
		String authURL = null;
		if(AppContext.production){
			//serviceURL="https://services.gcx.org/ekko/auth/service";
			serviceURL = SOAHUB.URL_EKKO_AUTH_SERVICE_PRODUCTION;
			//authURL = "https://services.gcx.org/ekko/auth/login?";
			authURL = SOAHUB.URL_EKKO_LOGIN_URL_PRODUCTION;
		}else{
			//serviceURL="https://servicesdev.gcx.org/ekko/auth/service";
			serviceURL = SOAHUB.URL_EKKO_AUTH_SERVICE;
			//authURL = "https://servicesdev.gcx.org/ekko/auth/login?";
			authURL = SOAHUB.URL_EKKO_LOGIN_URL;
		}

		String ticket = null;                           
		String service = null;
		String sessionId = null;
		String st_ticket = null;
		String jsessionid = null;

		//Step 1: get service
		client = new HttpsClient(appContext);

		HttpGet getService = new HttpGet(serviceURL);

		try {
			HttpResponse res = client.execute(getService);
			HttpEntity entity = res.getEntity();	
			service = EntityUtils.toString(entity);
			Log.i("EKKOAuth", service);


		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		// Step 2: get the ticket   


		// HTTP parameters stores header etc.
		HttpParams params = new BasicHttpParams();
		// params.setParameter("http.protocol.handle-redirects",false);
		params.setParameter("http.protocol.reject-relative-redirect", true);
		params.setParameter("http.protocol.allow-circular-redirects", true);


		try{
			HttpGet getForm = new HttpGet(casURL+"?service=" + service );

			HttpPost postForm = prepareLogin(getForm, username , pwd);
			postForm.setParams(params);

			HttpResponse response = client.execute(postForm);
			HttpEntity entity = response.getEntity();
			entity.consumeContent();
			//Log.i("EkKOCAS", EntityUtils.toString(entity));

			CookieStore cookies = client.getCookieStore();
			for(org.apache.http.cookie.Cookie c: cookies.getCookies()) {
				Log.i("Cookie", c.getName() ); 
				if(c.getName().equalsIgnoreCase("CASTGC")){
					ticket = c.getValue();
					Log.i("CASTGC", c.getValue());
				} else if(c.getName().equalsIgnoreCase("JSESSIONID")) {
					jsessionid = c.getValue();
				}

			}

			//Step 3: get the ST
			HttpParams paramsGet = getForm.getParams();
			paramsGet.setParameter("service", service);
			paramsGet.setParameter("http.protocol.handle-redirects",false);
			//paramsGet.setParameter("http.protocol.allow-circular-redirects", true);

			getForm.setParams(paramsGet);


			HttpResponse res = client.execute(getForm);
			// HttpEntity entityGet = res.getEntity();

			Header[] location = res.getHeaders("Location");
			if(location.length<=0){
				user.setSessionId(null);
				return user;
			}
			String newUrl = location[0].getValue();
			Log.i("Location:", newUrl);

			if(newUrl.contains("?")) {
				st_ticket = newUrl.split("\\?")[1];
			}
			entity = res.getEntity();
			Document doc = Jsoup.parse(EntityUtils.toString(entity));

			Log.i("EKKOCAS", doc.toString());

		} catch (Exception e){
			e.printStackTrace();
		}

		//step 4: get the session id
		// Post: https://servicesdev.gcx.org/ekko/auth/login?ticket=st-ticket
		HttpPost post = new HttpPost(authURL + st_ticket);
		try {
			HttpResponse res=client.execute(post);
			HttpEntity entity = res.getEntity();
			sessionId = EntityUtils.toString(entity);
			Log.i("SessionID", sessionId);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if(sessionId != null) {
			
			user.setName(username);
			user.setSessionId(sessionId);
			user.setAccount(username);
			
			AppContext.setSessionID(sessionId);
			
		
			//user.setLatestonline()
		} else{
			user.setSessionId(null);
		}
		//step 5: testing get course list
		//XML courses = GET https://servicesdev.gcx.org/ekko/{sessionId}/courses

/*		HttpGet httpGet = new HttpGet(service + sessionId +"/courses");
		HttpResponse resCourseList;
		try {
			resCourseList = client.execute(httpGet);
			HttpEntity entity = resCourseList.getEntity();
			Log.i("CourseList", EntityUtils.toString(entity));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		client.getConnectionManager().shutdown();
		return user;
	
	}


	private static InputStream retrieveStream(String url) {

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet httpRequest = new HttpGet(url);

		try {

			HttpResponse httpResponse = client.execute(httpRequest);
			final int statusCode = httpResponse.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w("AppClient-retrieveStream",
						"Error => " + statusCode + " => for URL " + url);
				return null;
			}

			HttpEntity httpEntity = httpResponse.getEntity();
			return httpEntity.getContent();

		}
		catch (IOException e) {
			httpRequest.abort();
			Log.w("AppClient-retrieveStream", "Error for URL =>" + url, e);
		}

		return null;

	}
	
	   /**
	    * Get the Course List
	    * @param appContext
	    * @param catalog
	    * @param pageIndex
	    * @param pageSize
	    * @return
	    * @throws AppException
	    */
	public static CourseList getCourseList(AppContext appContext, final int catalog, final int pageIndex, final int pageSize) throws AppException {
		//for testing only
		//if not log in yet, redirect to the log in dialog
		if(AppContext.getSessionID() == null){
			//redirect to login dialog
			//UIController.ToastMessage(appContext, "Please log in first!");
			appContext.Logout();
			appContext.getUnLoginHandler().sendEmptyMessage(1);
			
			return null;
		}
		else{
			String newUrl = null;
			if(AppContext.production){
				newUrl = SOAHUB.URL_EKKO_ROOT_SERVICE_PRODUCTION  +  AppContext.getSessionID() + SOAHUB.URL_REL_COURSELIST;
			}else{
				newUrl = SOAHUB.URL_EKKO_ROOT_SERVICE  +  AppContext.getSessionID() + SOAHUB.URL_REL_COURSELIST;
			}
			try{
				InputStream inputStream = retrieveStream( newUrl);
				return CourseList.parse(inputStream);		
			}catch(Exception e){
				if(e instanceof AppException)
					throw (AppException)e;
				throw AppException.network(e);
			}
		}
	}
	
	/**
	 * Get course zipped package
	 * @param appContext
	 * @param url
	 * @return
	 * @throws AppException
	 */
	
	public static InputStream getCourseZipFile(AppContext appContext, String url )throws AppException {
		InputStream inputStream = null;
		
		if(AppContext.getSessionID() == null){
			//redirect to login dialog
			//UIController.ToastMessage(appContext, "Please log in first!");
			appContext.Logout();
			appContext.getUnLoginHandler().sendEmptyMessage(1);
			
			return null;
		}
		try{
			inputStream = retrieveStream( url);
					
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
		return inputStream;
	}
	
	
	/**
	 * Get a course
	 * @param context
	 * @param resUrl
	 * @return
	 * @throws AppException
	 */
	public static Course getCourse(AppContext context, String resUrl) throws AppException{
		Course course = null;
		if(AppContext.getSessionID() == null){
			//redirect to login dialog
			//UIController.ToastMessage(appContext, "Please log in first!");
			context.Logout();
			context.getUnLoginHandler().sendEmptyMessage(1);
			
			return null;
		}
		
		try{
			InputStream inputStream = retrieveStream( resUrl);
			course = CourseManifest.parse(inputStream);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
		return course;
	}
	
	

}
