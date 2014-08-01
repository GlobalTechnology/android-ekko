package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.BuildConfig.THEKEY_CLIENTID;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.ekkoproject.android.player.sync.EkkoSyncService;

import java.util.Date;

import me.thekey.android.TheKey;
import me.thekey.android.lib.TheKeyImpl;

public class BaseActivity extends ActionBarActivity {
    TheKey mTheKey;

    private static final long INITIAL_SYNC_MAX_AGE = 3 * 60 * 60 * 1000; // 3 hours
    private static Date mLastSync;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        mTheKey = TheKeyImpl.getInstance(this, THEKEY_CLIENTID);

        // start an initial background sync if we haven't done one recently
        if (mLastSync == null || mLastSync.before(new Date(System.currentTimeMillis() - INITIAL_SYNC_MAX_AGE))) {
            final String guid = mTheKey.getGuid();
            EkkoSyncService.syncCourses(this, guid != null ? guid : GUID_GUEST);
            mLastSync = new Date();
        }
    }
}
