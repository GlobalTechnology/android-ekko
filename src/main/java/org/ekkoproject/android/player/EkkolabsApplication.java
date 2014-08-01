package org.ekkoproject.android.player;

import static org.ekkoproject.android.player.BuildConfig.NEW_RELIC_API_KEY;
import static org.ekkoproject.android.player.BuildConfig.THEKEY_CLIENTID;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;

import android.app.Application;

import com.newrelic.agent.android.NewRelic;

import org.ekkoproject.android.player.sync.EkkoSyncService;

import me.thekey.android.lib.TheKeyImpl;

public class EkkolabsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize New Relic
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);

        // start an initial background sync
        final String guid = TheKeyImpl.getInstance(this, THEKEY_CLIENTID).getGuid();
        EkkoSyncService.syncCourses(this, guid != null ? guid : GUID_GUEST);
    }
}
