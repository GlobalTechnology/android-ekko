package org.ekkoproject.android.player.activity;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        startActivity(CourseListActivity.newIntent(this, false));
        finish();
    }
}
