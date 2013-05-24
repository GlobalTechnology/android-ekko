package org.appdev.view;

import org.appdev.app.AppContext;
import org.appdev.app.AppException;

import org.appdev.api.ApiClient;
import org.appdev.entity.Result;
import org.appdev.entity.User;

import org.appdev.utils.StringUtils;
import org.appdev.utils.UIController;

import org.appdev.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

import org.jsoup.*;

/**
 * Log in dialog for the App
 */
public class LoginDialog extends Activity{
	
	private ViewSwitcher mViewSwitcher;
	private ImageButton btn_close;
	private Button btn_login;
	private AutoCompleteTextView mAccount;
	private EditText mPwd;
	private AnimationDrawable loadingAnimation;
	private View loginLoading;
	private CheckBox chb_rememberMe;
	private int curLoginType;
	private InputMethodManager imm;
	
	public final static int LOGIN_OTHER = 0x00;
	public final static int LOGIN_MAIN = 0x01;
	public final static int LOGIN_SETTING = 0x02;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        
        curLoginType = getIntent().getIntExtra("LOGINTYPE", LOGIN_OTHER);
        
        mViewSwitcher = (ViewSwitcher)findViewById(R.id.logindialog_view_switcher);       
        loginLoading = (View)findViewById(R.id.login_loading);
        mAccount = (AutoCompleteTextView)findViewById(R.id.login_account);
        mPwd = (EditText)findViewById(R.id.login_password);
        chb_rememberMe = (CheckBox)findViewById(R.id.login_checkbox_rememberMe);
        
        btn_close = (ImageButton)findViewById(R.id.login_close_button);
        btn_close.setOnClickListener(UIController.finish(this));        
        
        btn_login = (Button)findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
				
				String account = mAccount.getText().toString();
				String pwd = mPwd.getText().toString();
				boolean isRememberMe = chb_rememberMe.isChecked();
				
				if(StringUtils.isEmpty(account)){
					UIController.ToastMessage(v.getContext(), getString(R.string.msg_login_email_null));
					return;
				}
				if(StringUtils.isEmpty(pwd)){
					UIController.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
					return;
				}
				
		        btn_close.setVisibility(View.GONE);
		        loadingAnimation = (AnimationDrawable)loginLoading.getBackground();
		        loadingAnimation.start();
		        mViewSwitcher.showNext();
		        
		        login(account, pwd, isRememberMe);
			}
		});

        
        AppContext ac = (AppContext)getApplication();
        User user = ac.getLoginInfo();
        if(user==null || !user.isRememberMe()) return;
        if(!StringUtils.isEmpty(user.getAccount())){
        	mAccount.setText(user.getAccount());
        	mAccount.selectAll();
        	chb_rememberMe.setChecked(user.isRememberMe());
        }
        if(!StringUtils.isEmpty(user.getPwd())){
        	mPwd.setText(user.getPwd());
        }
    }
    
  
    private void login(final String account, final String pwd, final boolean isRememberMe) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if(msg.what == 1){
					User user = (User)msg.obj;
					if(user.getSessionId() != null){					
						ApiClient.cleanCookie();
				
						//UIController.sendBroadCast(LoginDialog.this, user.getNotice());					
						UIController.ToastMessage(LoginDialog.this, R.string.msg_login_success);
						UIController.ToastMessage(LoginDialog.this, user.getSessionId());
						if(curLoginType == LOGIN_MAIN){
						
							Intent intent = new Intent(LoginDialog.this, Main.class);
							intent.putExtra("LOGIN", true);
							startActivity(intent);
						}else if(curLoginType == LOGIN_SETTING){
						
							Intent intent = new Intent(LoginDialog.this, Setting.class);
							intent.putExtra("LOGIN", true);
							startActivity(intent);
						}
						finish();
					}
				}else if(msg.what == 0){
					mViewSwitcher.showPrevious();
					btn_close.setVisibility(View.VISIBLE);
					UIController.ToastMessage(LoginDialog.this, getString(R.string.msg_login_fail)+msg.obj);
				}else if(msg.what == -1){
					mViewSwitcher.showPrevious();
					btn_close.setVisibility(View.VISIBLE);
					((AppException)msg.obj).makeToast(LoginDialog.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
					AppContext ac = (AppContext)getApplication(); 
	                User user = ac.loginVerify(account, pwd);
	                user.setAccount(account);	           
	                user.setRememberMe(isRememberMe);
	              
	                if(user.getSessionId() != null) {
	                	ac.saveLoginInfo(user);
	                	msg.what = 1;
	                	msg.obj = user;
	                }else{
	                	ac.cleanLoginInfo();
	                	msg.what = 0;
	                	msg.obj = null;
	                }
	                
/*	                Result res = user.getValidate();
	                if(res.OK()){
	                	ac.saveLoginInfo(user);
	                	msg.what = 1;
	                	msg.obj = user;
	                }else{
	                	ac.cleanLoginInfo();
	                	msg.what = 0;
	                	msg.obj = res.getErrorMessage();
	                }*/
	            } catch (AppException e) {
	            	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
	            }
				handler.sendMessage(msg);
			}
		}.start();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		this.onDestroy();
    	}
    	return super.onKeyDown(keyCode, event);
    }
}
