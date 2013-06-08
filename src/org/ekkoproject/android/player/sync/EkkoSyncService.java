package org.ekkoproject.android.player.sync;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.appdev.entity.CourseList;
import org.ekkoproject.android.player.api.ApiSocketException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.api.InvalidSessionApiException;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.services.ManifestManager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class EkkoSyncService extends IntentService {
    // actions this service can broadcast
    public static final String ACTION_UPDATE_COURSES = "org.ekkoproject.android.player.sync.EkkoSyncService.UPDATE_COURSES";

    // data passed to this service in Intents
    public static final String EXTRA_SYNCTYPE = "org.ekkoproject.android.player.sync.EkkoSyncService.SYNCTYPE";
    public static final String EXTRA_COURSEID = "org.ekkoproject.android.player.sync.EkkoSyncService.COURSEID";

    public static final int SYNCTYPE_COURSES = 1;
    public static final int SYNCTYPE_MANIFEST = 2;

    private final EkkoDao dao;
    private final EkkoHubApi ekkoApi;

    public EkkoSyncService() {
        super("ekko_sync_service");
        this.dao = new EkkoDao(this);
        this.ekkoApi = new EkkoHubApi(this);
    }

    public static void broadcastCoursesUpdate(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_UPDATE_COURSES));
    }

    public static void syncCourses(final Context context) {
        context.startService(new Intent(context, EkkoSyncService.class).putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSES));
    }

    public static void syncManifest(final Context context, final long courseId) {
        context.startService(new Intent(context, EkkoSyncService.class).putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MANIFEST)
                .putExtra(EXTRA_COURSEID, courseId));
    }

    /** BEGIN lifecycle */

    @Override
    protected void onHandleIntent(final Intent intent) {
        final int syncType = intent.getIntExtra(EXTRA_SYNCTYPE, 0);
        try {
            switch (syncType) {
            case SYNCTYPE_COURSES:
                this.syncCourses();
                break;
            case SYNCTYPE_MANIFEST:
                this.syncManifest(intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE));
                break;
            }
        } catch (final ApiSocketException e) {
            EkkoHubApi.broadcastConnectionError(this);
        } catch (final InvalidSessionApiException e) {
            EkkoHubApi.broadcastInvalidSession(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dao.close();
    }

    /** END lifecycle */

    /**
     * synchronize all available courses from the hub
     * 
     * @throws ApiSocketException
     * @throws InvalidSessionApiException
     */
    private void syncCourses() throws ApiSocketException, InvalidSessionApiException {
        final CourseList courses = this.ekkoApi.getCourseList(0, 50);

        for (final Course course : courses.getCourselist()) {
            course.setLastSynced();

            final Course existing = this.dao.findCourse(course.getId(), false);
            if (existing != null) {
                this.dao.update(course, Contract.Course.PROJECTION_UPDATE_EKKOHUB);
                if (course.getVersion() > course.getManifestVersion()) {
                    EkkoSyncService.syncManifest(this, course.getId());
                }
            } else {
                this.dao.insert(course);
                EkkoSyncService.syncManifest(this, course.getId());
            }
        }

        // broadcast that courses were just updated
        broadcastCoursesUpdate(this);
    }

    private void syncManifest(final long courseId) {
        ManifestManager.downloadManifest(this, courseId);
    }
}
