package org.ekkoproject.android.player.tasks;

import android.content.Context;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.sync.EkkoSyncService;

public class EnrollmentRunnable implements Runnable {
    public static final int ENROLL = 1;
    public static final int UNENROLL = 2;

    private final Context mContext;
    private final EkkoHubApi api;
    private final int type;
    private final long id;

    public EnrollmentRunnable(final Context mContext, final int type, final long id) {
        this.mContext = mContext.getApplicationContext();
        this.api = EkkoHubApi.getInstance(mContext);
        this.id = id;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            switch (this.type) {
                case ENROLL:
                    api.enroll(this.id);
                    break;
                case UNENROLL:
                    api.unenroll(this.id);
                    break;
            }

            // sync the course now that we enrolled/unenrolled
            EkkoSyncService.syncCourse(mContext, id);
        } catch (final ApiSocketException e) {
        } catch (final InvalidSessionApiException e) {
        }
    }
}
