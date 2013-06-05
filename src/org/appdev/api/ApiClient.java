package org.appdev.api;

import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.entity.SOAHUB;
import org.appdev.entity.Update;
import org.appdev.entity.User;
import org.ccci.gto.android.thekey.TheKey;
import org.ccci.gto.android.thekey.TheKeySocketException;

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
			ua.append("/Android");//
			ua.append("/"+android.os.Build.VERSION.RELEASE);
			ua.append("/"+android.os.Build.MODEL); 
			ua.append("/"+appContext.getAppId());
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
		try{
			return Update.parse(retrieveStream(SOAHUB.UPDATE_VERSION));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}

    /**
     * Log in， Auto-process cookie
     * 
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
		if(AppContext.getInstance().isProductionEnv()){
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
        try {
            final TheKey thekey = new TheKey(appContext, THEKEY_CLIENTID);
            st_ticket = thekey.getTicket(service);
        } catch (final TheKeySocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		//step 4: get the session id
		// Post: https://servicesdev.gcx.org/ekko/auth/login?ticket=st-ticket
        HttpPost post = new HttpPost(authURL + "ticket=" + st_ticket);
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
}
