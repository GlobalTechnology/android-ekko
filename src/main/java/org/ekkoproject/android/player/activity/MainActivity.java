package org.ekkoproject.android.player.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        startActivity(CourseListActivity.newIntent(this, false));
        finish();
    }
}
