package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public final class GoogleAnalyticsManager {
    private static final int DIMEN_COURSE_ID = 1;

    private final Tracker mTracker;

    private static final Object LOCK_INSTANCE = new Object();
    private static GoogleAnalyticsManager instance;

    private GoogleAnalyticsManager(final Context context) {
        mTracker = GoogleAnalytics.getInstance(context).newTracker(GOOGLE_ANALYTICS_CLIENT_ID);
    }

    public static GoogleAnalyticsManager getInstance(final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (instance == null) {
                instance = new GoogleAnalyticsManager(context.getApplicationContext());
            }
        }

        return instance;
    }

    public void sendEvent(final String screenName) {
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendEvent(final String screenName, final long courseId) {
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().setCustomDimension(DIMEN_COURSE_ID, Long.toString(courseId))
                              .build());
    }
}
