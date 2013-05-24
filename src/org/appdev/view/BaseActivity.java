package org.appdev.view;

import org.appdev.R;
import org.appdev.app.AppManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Activity Base class
 */
//public class BaseActivity extends Activity{
public class BaseActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		
		//Add Activity to stack
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//Remove Activity from the Stack
		AppManager.getAppManager().finishActivity(this);
	}
	
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Used to put dark icons on light action bar
        
        menu.add(Menu.NONE, R.id.ID_ACTION_SAVE, Menu.NONE, "Save")
            .setIcon( R.drawable.ic_compose)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, R.id.ID_ACTION_SEARCH, Menu.NONE, "Search")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add(Menu.NONE, R.id.ID_ACTION_REFRESH, Menu.NONE, "Refresh")
            .setIcon( R.drawable.ic_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Toast toast;
      switch (item.getItemId()) {
      case R.id.ID_ACTION_SAVE:
	    toast = Toast.makeText(this, "save clicked", Toast.LENGTH_LONG);
	    toast.show();
        break;
      case R.id.ID_ACTION_SEARCH:
  	    toast = Toast.makeText(this, "search clicked", Toast.LENGTH_LONG);
  	    toast.show();
        break;
      case R.id.ID_ACTION_REFRESH:
    	    toast = Toast.makeText(this, "refresh clicked", Toast.LENGTH_LONG);
    	    toast.show();
            final Intent intent = new Intent(this, org.ccci.gto.android.thekey.LoginActivity.class);
            intent.putExtra(org.ccci.gto.android.thekey.LoginActivity.EXTRA_CASSERVER, "https://casdev.gcx.org/cas/");
            intent.putExtra(org.ccci.gto.android.thekey.LoginActivity.EXTRA_CLIENTID, "85613451684391165");
            this.startActivityForResult(intent, 1);
          break;
    	  
      }
      return super.onOptionsItemSelected(item);
    }
	*/
}
