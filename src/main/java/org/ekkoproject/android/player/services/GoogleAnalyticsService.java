package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public final class GoogleAnalyticsService {

    private static Tracker tracker = null;

    public static synchronized Tracker getTracker(Context context) {
        if(tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context.getApplicationContext());
            tracker = analytics.newTracker(GOOGLE_ANALYTICS_CLIENT_ID);
        }
        return tracker;
    }
}
