package org.ekkoproject.android.player.tasks;

import android.app.Activity;
import android.content.Context;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ekkoproject.android.player.NavigationListener;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.sync.EkkoSyncService;

// TODO: convert this to an AsyncTask to avoid leaky mActivity code & allow canceling of enrollment
public class EnrollmentRunnable implements Runnable {
    public static final int ENROLL = 1;
    public static final int UNENROLL = 2;

    private final Context mContext;
    private final EkkoHubApi api;
    private final int type;
    private final String guid;
    private final long id;

    private Activity mActivity = null;
    private NavigationListener mNavigationListener = null;

    public EnrollmentRunnable(final Activity activity, final String guid, final int type, final long id) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        this.api = EkkoHubApi.getInstance(activity, guid);
        this.type = type;
        this.guid = guid;
        this.id = id;
    }

    public void setNavigationListener(final NavigationListener listener) {
        this.mNavigationListener = listener;
    }

    @Override
    public void run() {
        try {
            switch (this.type) {
                case ENROLL:
                    final Course course = api.enroll(this.id);
                    if (mNavigationListener != null) {
                        // check to see if the course is now visible to the user
                        final Permission permission = course != null ? course.getPermission() : null;
                        if (permission != null && permission.isContentVisible()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNavigationListener.showCourse(course.getId());
                                }
                            });
                        }
                    }
                    break;
                case UNENROLL:
                    api.unenroll(this.id);
                    break;
            }

            // trigger a sync of the course now that we enrolled/unenrolled
            EkkoSyncService.syncCourse(mContext, guid, id);
        } catch (final ApiSocketException | InvalidSessionApiException ignored) {
        }
    }

    public void schedule() {
        this.api.async(this);
    }
}
