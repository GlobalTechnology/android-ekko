package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.services.ManifestManager.ACTION_UPDATE_MANIFEST;
import static org.ekkoproject.android.player.services.ProgressManager.ACTION_UPDATE_PROGRESS;
import static org.ekkoproject.android.player.sync.EkkoSyncService.ACTION_UPDATE_COURSES;

import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

public final class EkkoBroadcastReceiver extends BroadcastReceiver {
    private final Object owner;
    private final HashSet<Long> courses = new HashSet<Long>();

    public EkkoBroadcastReceiver(final Fragment owner) {
        this.owner = owner;
    }

    public EkkoBroadcastReceiver(final Fragment owner, final long courseId) {
        this(owner);
        this.courses.add(courseId);
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

    protected boolean forCourse(final Intent intent) {
        final long courseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        return this.courses.contains(courseId);
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
        } else if (ACTION_UPDATE_MANIFEST.equals(action) && forCourse(intent)) {
            if (owner instanceof ManifestUpdateListener) {
                final long courseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
                ((ManifestUpdateListener) this.owner).onManifestUpdate(courseId);
            }
        } else if (ACTION_UPDATE_PROGRESS.equals(action) && forCourse(intent)) {
            if (owner instanceof ProgressUpdateListener) {
                final long courseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
                ((ProgressUpdateListener) this.owner).onProgressUpdate(courseId);
            }
        }
    }

    public interface CourseUpdateListener {
        void onCourseUpdate();
    }

    public interface ManifestUpdateListener {
        void onManifestUpdate(long courseId);
    }

    public interface ProgressUpdateListener {
        void onProgressUpdate(long courseId);
    }
}
