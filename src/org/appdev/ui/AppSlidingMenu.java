package org.appdev.ui;

import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.appdev.utils.UIController;
import org.appdev.api.SlideMenuListener;

import org.appdev.R;


public class AppSlidingMenu extends ListFragment  {
	
	private SlideMenuListener listener;
	
	public AppSlidingMenu(SlideMenuListener listener){
		this.setListener(listener);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MenuAdapter adapter = new MenuAdapter(getActivity());
	
		adapter.add(new AppMenuItem("Login", R.drawable.ic_menu_login));
		//adapter.add(new AppMenuItem("Download", android.R.drawable.ic_menu_add));
		adapter.add(new AppMenuItem("Setting", R.drawable.ic_menu_setting));
		adapter.add(new AppMenuItem("Quit", R.drawable.ic_menu_exit));
		
		setListAdapter(adapter);
	}

	private class AppMenuItem {
		public String tag;
		public int iconRes;
		public AppMenuItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	public class MenuAdapter extends ArrayAdapter<AppMenuItem> {

		public MenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		
		switch (position) {
		case 0:
			this.listener.showcontent();
			UIController.loginOrLogout(getActivity());
			break;
		case 1:
			this.listener.showcontent();
			UIController.showSetting(getActivity());
			break;
		case 2:
			this.listener.showcontent();
			UIController.Exit(getActivity());
			break;

		}

	}

	public SlideMenuListener getListener() {
		return listener;
	}

	public void setListener(SlideMenuListener listener) {
		this.listener = listener;
		
	}
}
