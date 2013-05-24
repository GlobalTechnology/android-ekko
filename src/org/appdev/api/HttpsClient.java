package org.appdev.api;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;

public class HttpsClient extends DefaultHttpClient{
	
	final Context context;
	
	public HttpsClient(Context context){
		this.context = context;
	}
	
	protected ClientConnectionManager createClientConnectionManager(){
		try{
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			return new SingleClientConnManager(getParams(), registry);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}