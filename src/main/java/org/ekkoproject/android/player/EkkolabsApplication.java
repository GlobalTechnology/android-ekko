package org.ekkoproject.android.player;

import static org.ekkoproject.android.player.BuildConfig.NEW_RELIC_API_KEY;

import android.app.Application;

import com.newrelic.agent.android.NewRelic;

public class EkkolabsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize New Relic
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);
    }
}
