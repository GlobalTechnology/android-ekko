package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.sync.EkkoSyncService.ACTION_UPDATE_COURSES;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

public final class EkkoBroadcastReceiver extends BroadcastReceiver {
    private final Object owner;

    public EkkoBroadcastReceiver(final Fragment owner) {
        this.owner = owner;
    }

    public EkkoBroadcastReceiver registerReceiver() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this, this.getIntentFilter());
        return this;
    }

    public EkkoBroadcastReceiver unregisterReceiver() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
        return this;
    }

    private Context getContext() {
        if (this.owner instanceof Fragment) {
            return ((Fragment) this.owner).getActivity();
        }

        return null;
    }

    protected IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_COURSES);
        return filter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (ACTION_UPDATE_COURSES.equals(action)) {
            if (owner instanceof CourseUpdateListener) {
                ((CourseUpdateListener) owner).onCourseUpdate();
            }
        }
    }

    public interface CourseUpdateListener {
        void onCourseUpdate();
    }
}
