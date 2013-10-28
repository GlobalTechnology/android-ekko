package org.ekkoproject.android.player.tasks;

import android.content.Context;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.sync.EkkoSyncService;

public class EnrollmentRunnable implements Runnable {
    public static final int ENROLL = 1;
    public static final int UNENROLL = 2;

    private final Context mContext;
    private final EkkoHubApi api;
    private final int type;
    private final long id;

    private OnNavigationListener mOnNavigationListener = null;

    public EnrollmentRunnable(final Context mContext, final int type, final long id) {
        this.mContext = mContext.getApplicationContext();
        this.api = EkkoHubApi.getInstance(mContext);
        this.id = id;
        this.type = type;
    }

    public void setOnNavigationListener(final OnNavigationListener listener) {
        this.mOnNavigationListener = listener;
    }

    @Override
    public void run() {
        try {
            switch (this.type) {
                case ENROLL:
                    final Course course = api.enroll(this.id);
                    if (mOnNavigationListener != null) {
                        // check to see if the course is now visible to the user
                        final Permission permission = course != null ? course.getPermission() : null;
                        if (permission != null && permission.isContentVisible()) {
                            mOnNavigationListener.onSelectCourse(course.getId());
                        }
                    }
                    break;
                case UNENROLL:
                    api.unenroll(this.id);
                    break;
            }

            // trigger a sync of the course now that we enrolled/unenrolled
            EkkoSyncService.syncCourse(mContext, id);
        } catch (final ApiSocketException e) {
        } catch (final InvalidSessionApiException e) {
        }
    }
}
