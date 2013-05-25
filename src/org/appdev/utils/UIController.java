package org.appdev.utils;

import java.io.File;
import java.io.IOException;

import greendroid.widget.CustomizedQuickAction;
import greendroid.widget.QuickAction;

import org.appdev.view.ImageZoomDialog;

import org.appdev.CustomVideoPlayer.VideoPlayer;
import org.appdev.api.ApiClient;
import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.app.AppManager;
import org.appdev.entity.Course;
import org.appdev.entity.SOAHUB;
import org.appdev.view.About;
import org.appdev.view.LoginDialog;
import org.appdev.view.Main;
import org.appdev.view.Setting;

import org.appdev.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;


public class UIController {

	public final static int LISTVIEW_ACTION_INIT = 0x01;
	public final static int LISTVIEW_ACTION_REFRESH = 0x02;
	public final static int LISTVIEW_ACTION_SCROLL = 0x03;
	public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;
	
	public final static int LISTVIEW_DATA_MORE = 0x01;
	public final static int LISTVIEW_DATA_LOADING = 0x02;
	public final static int LISTVIEW_DATA_FULL = 0x03;
	public final static int LISTVIEW_DATA_EMPTY = 0x04;
	
	public final static int LISTVIEW_DATATYPE_COURSE = 0x01;
	public final static int LISTVIEW_DATATYPE_COURSE_LIST = 0x02;

	
	public final static int REQUEST_CODE_FOR_RESULT = 0x01;
	public final static int REQUEST_CODE_FOR_REPLY = 0x02;
	
	
	/**
	 * Show home page
	 * @param activity
	 */
	public static void showHome(Activity activity)
	{
		Intent intent = new Intent(activity,Main.class);
		activity.startActivity(intent);
		activity.finish();
	}
	
	/**
	 * show login dialog
	 * @param activity
	 */
	public static void showLoginDialog(Context context)
	{
		Intent intent = new Intent(context,LoginDialog.class);
		if(context instanceof Main)
			intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_MAIN);
		else if(context instanceof Setting)
			intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_SETTING);
		else
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	

	
	/**
	 * show setting page
	 * @param context
	 */
	public static void showSetting(Context context)
	{
		Intent intent = new Intent(context, Setting.class);
		context.startActivity(intent);
	}	
		

	
	/**
	 * Load and display image
	 * @param imgeView
	 * @param imageURL
	 * @param errMsg
	 */
	public static void showLoadImage(final ImageView imgView,final String imgURL, final String errMsg)
	{
		//default file
		if(StringUtils.isEmpty(imgURL) ){
			Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(), R.drawable.widget_dface);
			imgView.setImageBitmap(bmp);
			return;
		}
			
		//cache file
    	final String filename = FileUtils.getFileName(imgURL);
    	//Environment.getExternalStorageDirectory();返回/sdcard
    	String filepath = imgView.getContext().getFilesDir() + File.separator + filename;
    	File file = new File(filepath);
		if(file.exists()){
			Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);			
			imgView.setImageBitmap(bmp);
			return;
    	}
		
		//从网络获取&写入图片缓存
		String _errMsg = imgView.getContext().getString(R.string.msg_load_image_fail);
		if(!StringUtils.isEmpty(errMsg))
			_errMsg = errMsg;
		final String ErrMsg = _errMsg;
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==1 && msg.obj != null){
					imgView.setImageBitmap((Bitmap)msg.obj);
					try {
                    	//写图片缓存
						ImageUtils.saveImage(imgView.getContext(), filename, (Bitmap)msg.obj);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					ToastMessage(imgView.getContext(), ErrMsg);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {
					Bitmap bmp = ApiClient.getBitmapfromURL(imgView.getContext(),imgURL);
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * url redirect
	 * @param context
	 * @param url
	 */
	public static void showUrlRedirect(Context context, String url){
		SOAHUB urls = SOAHUB.parseURL(url);
		if(urls != null){
			showLinkRedirect(context, urls.getObjType(), urls.getObjId(), urls.getObjKey());
		}else{
			openBrowser(context, url);
		}
	}
	
	public static void showLinkRedirect(Context context, int objType, int objId, String objKey){
		switch (objType) {

			case SOAHUB.URL_OBJ_TYPE_OTHER:
				openBrowser(context, objKey);
				break;
		}
	}
	
	/**
	 * 打开浏览器
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url){
		try {
			Uri uri = Uri.parse(url);  
			Intent it = new Intent(Intent.ACTION_VIEW, uri);  
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
			ToastMessage(context, "can not visit the page", 500);
		} 
	}
		
	/**
	 * 获取webviewClient对象
	 * @return
	 */
	public static WebViewClient getWebViewClient(){
		return new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,String url) {
				showUrlRedirect(view.getContext(), url);
				return true;
			}
		};
	}
	
	/**
	 * 获取TextWatcher对象
	 * @param context
	 * @param tmlKey
	 * @return
	 */
	public static TextWatcher getTextWatcher(final Activity context, final String temlKey) {
		return new TextWatcher() {		
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//保存当前EditText正在编辑的内容
				((AppContext)context.getApplication()).setProperty(temlKey, s.toString());
			}		
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}		
			public void afterTextChanged(Editable s) {}
		};
	}
	
	
	/**
	 * 组合动态的回复文本
	 * @param name
	 * @param body
	 * @return
	 */
	public static SpannableString parseActiveReply(String name,String body){
		SpannableString sp = new SpannableString(name+"："+body);
		//设置用户名字体加粗、高亮 
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
	}
	
	/**
	 * 组合消息文本
	 * @param name
	 * @param body
	 * @return
	 */
	public static SpannableString parseMessageSpan(String name,String body,String action){
		SpannableString sp = null;
		int start = 0;
		int end = 0;
		if(StringUtils.isEmpty(action)){
			sp = new SpannableString(name + "：" + body);
			end = name.length();
		}else{
			sp = new SpannableString(action + name + "：" + body);
			start = action.length();
			end = start + name.length();
		}
		//设置用户名字体加粗、高亮 
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
	}
	
	/**
	 * 组合回复引用文本
	 * @param name
	 * @param body
	 * @return
	 */
	public static SpannableString parseQuoteSpan(String name,String body){
		SpannableString sp = new SpannableString("回复："+name+"\n"+body);
		//设置用户名字体加粗、高亮 
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 3, 3+name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 3, 3+name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
	}
	
	/**
	 * 弹出Toast消息
	 * @param msg
	 */
	public static void ToastMessage(Context cont,String msg)
	{
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}
	public static void ToastMessage(Context cont,int msg)
	{
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}
	public static void ToastMessage(Context cont,String msg,int time)
	{
		Toast.makeText(cont, msg, time).show();
	}
	
	/**
	 * 点击返回监听事件
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity)
	{
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}	
	
	/**
	 * Show about us dialog
	 * @param context
	 */
	public static void showAbout(Context context)
	{
		Intent intent = new Intent(context, About.class);
		context.startActivity(intent);
	}
	
	
	/**
	 * Show login or logout in the menu
	 * @param activity
	 * @param menu
	 */
	public static void showMenuLoginOrLogout(Activity activity,Menu menu)
	{
		if(((AppContext)activity.getApplication()).isLogin()){
			menu.findItem(R.id.main_menu_user).setTitle(R.string.main_menu_logout);
			menu.findItem(R.id.main_menu_user).setIcon(R.drawable.ic_menu_logout);
		}else{
			menu.findItem(R.id.main_menu_user).setTitle(R.string.main_menu_login);
			menu.findItem(R.id.main_menu_user).setIcon(R.drawable.ic_menu_login);
		}
	}
	
	/**
	 * 快捷栏显示登录与登出
	 * @param activity
	 * @param qa
	 */
	public static void showSettingLoginOrLogout(Activity activity,QuickAction qa)
	{
		if(((AppContext)activity.getApplication()).isLogin()){
			qa.setIcon(CustomizedQuickAction.buildDrawable(activity, R.drawable.ic_menu_logout));
			qa.setTitle(activity.getString(R.string.main_menu_logout));
		}else{
			qa.setIcon(CustomizedQuickAction.buildDrawable(activity, R.drawable.ic_menu_login));
			qa.setTitle(activity.getString(R.string.main_menu_login));
		}
	}
	
	/**
	 * 快捷栏是否显示文章图片
	 * @param activity
	 * @param qa
	 */
	public static void showSettingIsLoadImage(Activity activity,QuickAction qa)
	{
		if(((AppContext)activity.getApplication()).isDisplayImage()){
			qa.setIcon(CustomizedQuickAction.buildDrawable(activity, R.drawable.ic_menu_picnoshow));
			qa.setTitle(activity.getString(R.string.main_menu_picnoshow));
		}else{
			qa.setIcon(CustomizedQuickAction.buildDrawable(activity, R.drawable.ic_menu_picshow));
			qa.setTitle(activity.getString(R.string.main_menu_picshow));
		}
	}
	
	/**
	 * User login and logout
	 * @param activity
	 */
	public static void loginOrLogout(Activity activity)
	{
		AppContext ac = (AppContext)activity.getApplication();
		if(ac.isLogin()){
			ac.Logout();
			ToastMessage(activity, "Has exited");
		}else{
			showLoginDialog(activity);
		}
	}
	
	/**
	 * Display image or not for Media slide
	 * @param activity
	 */
	public static void changeSettingIsLoadImage(Activity activity)
	{
		AppContext ac = (AppContext)activity.getApplication();
		if(ac.isDisplayImage()){
			ac.setConfigLoadimage(false);
			ToastMessage(activity, "Do not display Image");
		}else{
			ac.setConfigLoadimage(true);
			ToastMessage(activity, "Display Image");
		}
	}
	public static void changeSettingIsLoadImage(Activity activity,boolean b)
	{
		AppContext ac = (AppContext)activity.getApplication();
		ac.setConfigLoadimage(b);
	}
	
	/**
	 * clear app cache
	 * @param activity
	 */
	public static void clearAppCache(Activity activity)
	{
		final AppContext ac = (AppContext)activity.getApplication();
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==1){
					ToastMessage(ac, "Cache cleared!");
				}else{
					ToastMessage(ac, "Clean cache failed!");
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {				
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
	            	msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * Send App crash report
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context cont, final String crashReport)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//Send Exception Report
				Intent i = new Intent(Intent.ACTION_SEND);
				//i.setType("text/plain"); //模拟器
				i.setType("message/rfc822") ; //真机
				i.putExtra(Intent.EXTRA_EMAIL, new String[]{"zaiqiangliu@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT,"mLearning-Android: error report");
				i.putExtra(Intent.EXTRA_TEXT,crashReport);
				cont.startActivity(Intent.createChooser(i, "Send Error Report"));
				//Exit
				AppManager.getAppManager().AppExit(cont);
			}
		});
		builder.setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//Exit
				AppManager.getAppManager().AppExit(cont);
			}
		});
		builder.show();
	}
	
	/**
	 * exit app
	 * @param cont
	 */
	public static void Exit(final Context cont)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_menu_surelogout);
		builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//save the current course state
				Course curCourse = AppContext.getInstance().getCurCourse();
				
				
				//exit
				AppManager.getAppManager().AppExit(cont);
			}
		});
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}

	public static void showLessonList(Context context) {
		// TODO Auto-generated method stub
		
	}
	
	public static void playVideo(Context context, String url){
		if(url == null) {
			url = "http://www.denivip.ru/sites/default/files/trailer2.mp4"; 
		} 

	    Intent intent = VideoPlayer.createIntent(context, null, url);
	    Log.i("UIController_palyVideo", url);
	    
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    context.startActivity(intent);
	   
	}
	
	public static void showImageZoomDialog(Context context, String imgUrl)
	{
		Intent intent = new Intent(context, ImageZoomDialog.class);
		intent.putExtra("img_url", imgUrl);
		context.startActivity(intent);
	}
}
