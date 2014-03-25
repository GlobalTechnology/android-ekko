package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.newrelic.agent.android.NewRelic;

import org.ccci.gto.android.thekey.TheKeyImpl;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.sync.EkkoSyncService;

public class SplashScreenActivity extends Activity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NewRelic.withApplicationToken(this.getString(R.string.newRelicApiKey)).start(this.getApplication());
        final View view = View.inflate(this, R.layout.activity_splash_screen, null);
        setContentView(view);

        final AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(2000);
        view.startAnimation(aa);
        aa.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                redirectTo();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
            }

            @Override
            public void onAnimationStart(final Animation animation) {
            }
        });

        // start an initial background sync
        final String guid = TheKeyImpl.getInstance(this, THEKEY_CLIENTID).getGuid();
        EkkoSyncService.syncCourses(this, guid != null ? guid : GUID_GUEST);
    }

    private void redirectTo() {
        startActivity(MainActivity.newIntent(this));
        finish();
    }
}
