package org.ekkoproject.android.player;

import static org.ekkoproject.android.player.BuildConfig.ARCLIGHT_API_KEY;
import static org.ekkoproject.android.player.BuildConfig.NEW_RELIC_API_KEY;
import static org.ekkoproject.android.player.BuildConfig.THEKEY_CLIENTID;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;

import android.app.Application;
import android.content.pm.PackageManager;

import com.jesusfilmmedia.eventtracker.EventTracker;
import com.newrelic.agent.android.NewRelic;

import org.ekkoproject.android.player.sync.EkkoSyncService;

import me.thekey.android.lib.TheKeyImpl;

public class EkkolabsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize New Relic
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);

        // Initialize Arclight Event Tracker
        EventTracker.getInstance().initialize(this, ARCLIGHT_API_KEY, getPackageName(), getAppVersionName());

        // start an initial background sync
        final String guid = TheKeyImpl.getInstance(this, THEKEY_CLIENTID).getGuid();
        EkkoSyncService.syncCourses(this, guid != null ? guid : GUID_GUEST);
    }

    private String getAppVersionName() {
        try {
            return this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (final PackageManager.NameNotFoundException ignored) {
        }

        return null;
    }
}
