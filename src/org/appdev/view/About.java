package org.appdev.view;

import org.appdev.R;
import org.appdev.app.AppManager;

import android.app.Activity;
//import org.appdev.utils.UpdateManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * About
 * @author Henry
 * @version 1.0
 * 
 */
public class About extends Activity{
	
	private TextView mVersion;
	private Button mUpdate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		//添加Activity到堆栈
		AppManager.getAppManager().addActivity(this);
		
        try { 
        	PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
        	mVersion = (TextView)findViewById(R.id.about_version);
    		mVersion.setText(""+info.versionName);
        } catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
        
        mUpdate = (Button)findViewById(R.id.about_update);
        mUpdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			//	UpdateManager.getUpdateManager().checkAppUpdate(About.this, true);
			}
		});        
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}
}
