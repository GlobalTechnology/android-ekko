package org.ekkoproject.android.player.widget;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.ekkoproject.android.player.services.GoogleAnalyticsService;

public class GoogleAnalyticsDrawerListener extends DrawerLayout.SimpleDrawerListener {
    @Override
    public void onDrawerOpened(View drawerView) {
        Tracker tracker = GoogleAnalyticsService.getTracker(drawerView.getContext());
        tracker.setScreenName("Course Navigation");
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }
}
