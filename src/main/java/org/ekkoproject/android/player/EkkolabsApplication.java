package org.ekkoproject.android.player;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.jesusfilmmedia.eventtracker.EventTracker;

import static org.ekkoproject.android.player.BuildConfig.ARCLIGHT_API_KEY;

public class EkkolabsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Arclight Event Tracker
        EventTracker.getInstance().initialize(this, ARCLIGHT_API_KEY, getPackageName(), getAppVersionName(this));
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }

}
