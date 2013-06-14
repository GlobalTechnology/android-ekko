package org.appdev.view;

import static org.ccci.gto.android.thekey.LoginActivity.EXTRA_CLIENTID;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import java.io.File;

import org.appdev.app.AppContext;
import org.appdev.app.AppManager;
import org.appdev.utils.FileUtils;
import org.appdev.utils.MethodsCompat;
import org.appdev.utils.UIController;
import org.appdev.utils.UpdateManager;
import org.ccci.gto.android.thekey.LoginActivity;
import org.ccci.gto.android.thekey.dialog.LoginDialogFragment;
import org.ekkoproject.android.player.R;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Setting extends PreferenceActivity {
	
	SharedPreferences mPreferences;
	Preference account;
	Preference myinfo;
	Preference cache;
	Preference feedback;
	Preference update;
	Preference about;
	CheckBoxPreference productionEnv;
	CheckBoxPreference loadimage;
	CheckBoxPreference scroll;
	CheckBoxPreference voice;
	CheckBoxPreference checkup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Add Activity to stack
		AppManager.getAppManager().addActivity(this);
		
		//Add Preferences
		addPreferencesFromResource(R.xml.preferences);
		//get SharedPreferences
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);		
		
		ListView localListView = getListView();
		localListView.setBackgroundColor(0);
		localListView.setCacheColorHint(0);
		((ViewGroup)localListView.getParent()).removeView(localListView);
		ViewGroup localViewGroup = (ViewGroup)getLayoutInflater().inflate(R.layout.setting, null);
		((ViewGroup)localViewGroup.findViewById(R.id.setting_content)).addView(localListView, -1, -1);
		setContentView(localViewGroup);
	      
	    
		final AppContext ac = (AppContext)getApplication();
		
		//Login and logout
		account = (Preference)findPreference("account");
		if(ac.isLogin()){
			account.setTitle(R.string.main_menu_logout);
		}else{
			account.setTitle(R.string.main_menu_login);
		}
        account.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public boolean onPreferenceClick(final Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    final FragmentManager fm = Setting.this.getFragmentManager();
                    final FragmentTransaction ft = fm.beginTransaction();
                    final Fragment prev = fm.findFragmentByTag("loginDialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    final LoginDialogFragment newFragment = LoginDialogFragment.newInstance(THEKEY_CLIENTID);
                    newFragment.show(ft, "loginDialog");
                } else {
                    // fragments aren't supported, so use a separate activity
                    final Intent intent = new Intent(Setting.this, LoginActivity.class);
                    intent.putExtra(EXTRA_CLIENTID, THEKEY_CLIENTID);
                    Setting.this.startActivity(intent);
                }

                // UIController.loginOrLogout(Setting.this);
                account.setTitle(R.string.main_menu_login);
                return true;
            }
        });
		
		//My info
		myinfo = (Preference)findPreference("myinfo");
		myinfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
			//	UIHelper.showUserInfo(Setting.this);
				return true;
			}
		});
		
		//Production Env
		productionEnv = (CheckBoxPreference)findPreference("productionEnv");
		productionEnv.setChecked(ac.isProductionEnv());
		if(ac.isProductionEnv()){
			productionEnv.setSummary("Production Environment");
		}else{
			productionEnv.setSummary("Development Environment");
		}
		productionEnv.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigProductionEnv(productionEnv.isChecked());
				if(productionEnv.isChecked()){
					productionEnv.setSummary("Production Environment");
				}else{
					productionEnv.setSummary("Development Environment");
				}
				return true;
			}
		});
		
		//load image
		loadimage = (CheckBoxPreference)findPreference("loadimage");
		loadimage.setChecked(ac.isDisplayImage());
		if(ac.isDisplayImage()){
			loadimage.setSummary("Load Pic (Load Pic as default for WIFI)");
		}else{
			loadimage.setSummary("Load no  (Load Pic as default for WIFI)");
		}
		loadimage.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIController.changeSettingIsLoadImage(Setting.this,loadimage.isChecked());
				if(loadimage.isChecked()){
					loadimage.setSummary("Load Pic (Load Pic as default for WIFI)");
				}else{
					loadimage.setSummary("Load no  (Load Pic as default for WIFI)");
				}
				return true;
			}
		});
		
		//Scroll allowed?
		scroll = (CheckBoxPreference)findPreference("scroll");
		scroll.setChecked(ac.isScroll());
		if(ac.isScroll()){
			scroll.setSummary("Horizontal Scroll enabled!");
		}else{
			scroll.setSummary("Horizontal Scroll Disabled");
		}
		scroll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigScroll(scroll.isChecked());
				if(scroll.isChecked()){
					scroll.setSummary("Horizontal Scroll enabled!");
				}else{
					scroll.setSummary("Horizontal Scroll Disabled");
				}
				return true;
			}
		});
		
		//Play voice
		voice = (CheckBoxPreference)findPreference("voice");
		voice.setChecked(ac.isVoice());
		if(ac.isVoice()){
			voice.setSummary("Sound alert enabled");
		}else{
			voice.setSummary("Sound alert disabled");
		}
		voice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigVoice(voice.isChecked());
				if(voice.isChecked()){
					voice.setSummary("Sound alert enabled");
				}else{
					voice.setSummary("Sound alert disabled");
				}
				return true;
			}
		});
		
		//Enable version update check
		checkup = (CheckBoxPreference)findPreference("checkup");
		checkup.setChecked(ac.isCheckUp());
		checkup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ac.setConfigCheckUp(checkup.isChecked());
				return true;
			}
		});
		
		//Calculate cache size		
		long fileSize = 0;
		String cacheSize = "0KB";		
		File filesDir = getFilesDir();
		File cacheDir = getCacheDir();
		
		fileSize += FileUtils.getDirSize(filesDir);
		fileSize += FileUtils.getDirSize(cacheDir);		
		//Only android v2.2 and above can store cache on SD card
		if(AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)){
			File externalCacheDir = MethodsCompat.getExternalCacheDir(this);
			fileSize += FileUtils.getDirSize(externalCacheDir);
		}		
		if(fileSize > 0)
			cacheSize = FileUtils.formatFileSize(fileSize);
		
		//Clear cache
		cache = (Preference)findPreference("cache");
		cache.setSummary(cacheSize);
		cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIController.clearAppCache(Setting.this);
				cache.setSummary("0KB");
				return true;
			}
		});
		
		//Feedback
		feedback = (Preference)findPreference("feedback");
		feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				//UIController.showFeedBack(Setting.this);
				return true;
			}
		});
		
		//Update
		update = (Preference)findPreference("update");
		update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UpdateManager.getUpdateManager().checkAppUpdate(Setting.this, true);
				return true;
			}
		});
		
		//About us
		about = (Preference)findPreference("about");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				UIController.showAbout(Setting.this);
				return true;
			}
		});
		
	}
	public void back(View paramView)
	{
		finish();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(intent.getBooleanExtra("LOGIN", false)){
			account.setTitle(R.string.main_menu_logout);
		}				
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		AppManager.getAppManager().finishActivity(this);
    }
}
