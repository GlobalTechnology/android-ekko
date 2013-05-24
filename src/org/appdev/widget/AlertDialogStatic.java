package org.appdev.widget;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

public class AlertDialogStatic {
	
	private static ProgressDialog mProgressDialog;
	
	private AlertDialogStatic(){}
	
	/**
	 * 
	 * Static method to show a simple alert dialog. The button will simply dismiss the dialog
	 * 
	 * @param context Application Context
	 * @param title Title of the dialog
	 * @param message Message to display in the dialog
	 * @param neutralText Text to display on the dialog button
	 */
	public static void showSimpleAlertDialog(Context context, String title, String message, String neutralText){
	    try {
    		new AlertDialog.Builder(context)
    		.setTitle(title)
    		.setMessage(message)
    		.setNeutralButton(neutralText, null)
    		.create()
    		.show();
	    } catch (Exception e) {
	    }
	}
	
	/**
	 * 
	 * Static method to show a simple alert dialog with an OnClickListener to be called when the button is pressed.
	 * 
	 * @param context Application Context
	 * @param title Title of the dialog
	 * @param message Message to display in the dialog
	 * @param neutralText Text to display on the dialog button
	 * @param list OnClickListener to call when the button is pressed.
	 */
	public static void showSimpleAlertDialogWithCallback(Context context, String title, String message, String neutralText, DialogInterface.OnClickListener list){
	    try {
    		new AlertDialog.Builder(context)
    		.setTitle(title)
    		.setMessage(message)
    		.setNeutralButton(neutralText, list)
    		.setCancelable(true)
    		.create()
    		.show();
        } catch (Exception e) {
        }
	}
	
	public static void showTwoButtonAlertDialogWithCallback(Context context, String title, String message,
			             									String positive_text,
			             									String negative_text,
			             									DialogInterface.OnClickListener positive_callback,
			             									DialogInterface.OnClickListener negative_callback){
		try {
			new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(positive_text, positive_callback)
			.setNegativeButton(negative_text, negative_callback)
			.setCancelable(true)
			.create()
			.show();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 
	 * Static method to show a simple progress dialog with an indeterminate progress spinner and some text.
	 * 
	 * @param context Application Context
	 * @param title Title of the dialog
	 * @param message Message to display in the dialog
	 */
	public static void showProgressDialog(Context context, String title, String message){
	    try {
    		// If there is a dialog currently displayed, destroy it.
    		hideProgressDialog();
    		
    		ProgressDialog temp = mProgressDialog = new ProgressDialog(context);
    		
    		// Set some paramaters
    		temp.setIndeterminate(true);
    		temp.setTitle(title);
    		temp.setMessage(message);
    		temp.setCancelable(false);
    		temp.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
    				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    
    		temp.show();
        } catch (Exception e) {
        }
	}
	
	/**
	 * Static method to hide any dialogs invoked by methods in this class.
	 */
	public static void hideProgressDialog(){
		if(mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}
		}
		mProgressDialog = null;
	}
	
}
