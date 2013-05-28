package org.appdev.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.appdev.app.AppContext;
import org.appdev.app.AppException;
import org.appdev.R;
import org.appdev.api.ApiClient;
import org.appdev.entity.Update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * EKKO Application Online Update
 */
public class UpdateManager {

	private static final int DOWN_NOSDCARD = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
	
    private static final int DIALOG_TYPE_LATEST = 0;
    private static final int DIALOG_TYPE_FAIL   = 1;
    
	private static UpdateManager updateManager;
	
	private Context mContext;
	//Notification dialog
	private Dialog noticeDialog;
	//Download dialog
	private Dialog downloadDialog;
	//Download error, or no need to download notification dialog
	private Dialog latestOrFailDialog;
    //Progress bar
    private ProgressBar mProgress;
    //Download progress
    private TextView mProgressText;
    
    private ProgressDialog mProDialog;
    
    private int progress;
    //download thread
    private Thread downLoadThread;
    //Stop flag
    private boolean interceptFlag;
	
	private String updateMsg = "";
	private String apkUrl = "";
	//save path
    private String savePath = "";
	//apk file in full path
	private String apkFilePath = "";
	private String tmpFilePath = "";
	private String apkFileSize;
	private String tmpFileSize;
	
	private String curVersionName = "";
	private int curVersionCode;
	private Update mUpdate;
    
    private Handler mHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				mProgressText.setText(tmpFileSize + "/" + apkFileSize);
				break;
			case DOWN_OVER:
				downloadDialog.dismiss();
				installApk();
				break;
			case DOWN_NOSDCARD:
				downloadDialog.dismiss();
				Toast.makeText(mContext, "Failed to download, please check if SD mounted", 3000).show();
				break;
			}
    	};
    };
    
	public static UpdateManager getUpdateManager() {
		if(updateManager == null){
			updateManager = new UpdateManager();
		}
		updateManager.interceptFlag = false;
		return updateManager;
	}
	
	/**
	 * Check app update
	 * @param context
	 * @param isShowMsg 
	 */
	public void checkAppUpdate(Context context, final boolean isShowMsg){
		this.mContext = context;
		getCurrentVersion();
		if(isShowMsg){
			if(mProDialog == null)
				mProDialog = ProgressDialog.show(mContext, null, "Checking, please waiting...", true, true);
			else if(mProDialog.isShowing() || (latestOrFailDialog!=null && latestOrFailDialog.isShowing()))
				return;
		}
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(mProDialog != null && !mProDialog.isShowing()){
					return;
				}
				if(isShowMsg && mProDialog != null){
					mProDialog.dismiss();
					mProDialog = null;
				}
				if(msg.what == 1){
					mUpdate = (Update)msg.obj;
					if(mUpdate != null){
						if(curVersionCode < mUpdate.getVersionCode()){
							apkUrl = mUpdate.getDownloadUrl();
							updateMsg = mUpdate.getUpdateLog();
							showNoticeDialog();
						}else if(isShowMsg){
							showLatestOrFailDialog(DIALOG_TYPE_LATEST);
						}
					}
				}else if(isShowMsg){
					showLatestOrFailDialog(DIALOG_TYPE_FAIL);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg = new Message();
				try {					
					Update update = ApiClient.checkVersion((AppContext)mContext.getApplicationContext());
					msg.what = 1;
					msg.obj = update;
				} catch (AppException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}			
		}.start();		
	}	
	
	/**
	 * No update, or fail to download dialog
	 */
	private void showLatestOrFailDialog(int dialogType) {
		if (latestOrFailDialog != null) {
			latestOrFailDialog.dismiss();
			latestOrFailDialog = null;
		}
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("System Notice");
		if (dialogType == DIALOG_TYPE_LATEST) {
			builder.setMessage("No update");
		} else if (dialogType == DIALOG_TYPE_FAIL) {
			builder.setMessage("Fail to get the update info");
		}
		builder.setPositiveButton("OK", null);
		latestOrFailDialog = builder.create();
		latestOrFailDialog.show();
	}

	/**
	 * get the current apk version
	 */
	private void getCurrentVersion(){
        try { 
        	PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        	curVersionName = info.versionName;
        	curVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
	}
	
	/**
	 * Show notice dialog
	 */
	private void showNoticeDialog(){
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("EKKO Update");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("Update now", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();			
			}
		});
		builder.setNegativeButton("Later", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}
	
	/**
	 * show download dialog
	 */
	private void showDownloadDialog(){
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("Downloading New version");
		
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.update_progress, null);
		mProgress = (ProgressBar)v.findViewById(R.id.update_progress);
		mProgressText = (TextView) v.findViewById(R.id.update_progress_text);
		
		builder.setView(v);
		builder.setNegativeButton("Cancel", new OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();
		
		downloadApk();
	}
	
	private Runnable mdownApkRunnable = new Runnable() {	
		@Override
		public void run() {
			try {
				String apkName = "ekko_"+ Integer.toString(mUpdate.getVersionCode())+".apk";
				String tmpApk = "ekko_"+Integer.toString(mUpdate.getVersionCode())+".tmp";
				//SD card is mounted
				String storageState = Environment.getExternalStorageState();		
				if(storageState.equals(Environment.MEDIA_MOUNTED)){
					savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ekko/Update/";
					File file = new File(savePath);
					if(!file.exists()){
						file.mkdirs();
					}
					apkFilePath = savePath + apkName;
					tmpFilePath = savePath + tmpApk;
				}
				
				//SD is not mounted
				if(apkFilePath == null || apkFilePath == ""){
					mHandler.sendEmptyMessage(DOWN_NOSDCARD);
					return;
				}
				
				File ApkFile = new File(apkFilePath);
				
				if(ApkFile.exists()){
					downloadDialog.dismiss();
					installApk();
					return;
				}
				
				//temp file
				File tmpFile = new File(tmpFilePath);
				FileOutputStream fos = new FileOutputStream(tmpFile);
				
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				
		    	DecimalFormat df = new DecimalFormat("0.00");
		    	//apk file size
		    	apkFileSize = df.format((float) length / 1024 / 1024) + "MB";
				
				int count = 0;
				byte buf[] = new byte[1024];
				
				do{   		   		
		    		int numread = is.read(buf);
		    		count += numread;
		    		tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
		    	    progress =(int)(((float)count / length) * 100);
		    	    //update progress
		    	    mHandler.sendEmptyMessage(DOWN_UPDATE);
		    		if(numread <= 0){	
						if(tmpFile.renameTo(ApkFile)){
							mHandler.sendEmptyMessage(DOWN_OVER);
						}
		    			break;
		    		}
		    		fos.write(buf,0,numread);
		    	}while(!interceptFlag);//cancel download
				
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			}
			
		}
	};
	
	/**
	* download apk
	* @param url
	*/	
	private void downloadApk(){
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	
	/**
    * install apk
    * @param url
    */
	private void installApk(){
		File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            return;
        }    
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
       // i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive"); 
        mContext.startActivity(i);
	}
}

