package org.ekkoproject.android.player.sync;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.services.ManifestManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EkkoSyncService extends IntentService {
    // actions this service can broadcast
    public static final String ACTION_UPDATE_COURSES = "org.ekkoproject.android.player.sync.EkkoSyncService.UPDATE_COURSES";

    // data passed to this service in Intents
    public static final String EXTRA_SYNCTYPE = "org.ekkoproject.android.player.sync.EkkoSyncService.SYNCTYPE";

    public static final int SYNCTYPE_COURSES = 1;
    public static final int SYNCTYPE_MANIFEST = 2;

    private EkkoDao dao;
    private EkkoHubApi ekkoApi;
    private ManifestManager manifestManager;

    public EkkoSyncService() {
        super("ekko_sync_service");
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
    public void onCreate() {
        super.onCreate();
        this.dao = EkkoDao.getInstance(this);
        this.ekkoApi = new EkkoHubApi(this);
        this.manifestManager = ManifestManager.getInstance(this);
    }

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
        this.dao = null;
        this.ekkoApi = null;
        this.manifestManager = null;
    }

    /** END lifecycle */

    /**
     * synchronize all available courses from the hub
     * 
     * @throws ApiSocketException
     * @throws InvalidSessionApiException
     */
    private void syncCourses() throws ApiSocketException, InvalidSessionApiException {
        // load all existing courses
        final Map<Long, Course> existing = new HashMap<Long, Course>();
        for (final Course course : this.dao.getCourses(null, null, null, false)) {
            if (course != null) {
                existing.put(course.getId(), course);
            }
        }

        boolean error = false;
        boolean hasMore = true;
        int start = 0;
        int limit = 50;
        final Set<Long> seen = new HashSet<Long>();
        while (hasMore) {
            final CourseList courses = this.ekkoApi.getCourseList(start, limit);
            if (courses != null) {
                for (final Course course : courses.getCourses()) {
                    // update sync flags/data
                    course.setAccessible(true);
                    course.setLastSynced();

                    // should we insert or update
                    final Course old = existing.remove(course.getId());
                    if (old != null || seen.contains(course.getId())) {
                        this.dao.updateCourse(course, Contract.Course.PROJECTION_UPDATE_EKKOHUB, true);
                        if (old != null && course.getVersion() > old.getManifestVersion()) {
                            EkkoSyncService.syncManifest(this, course.getId());
                        }
                    } else {
                        this.dao.insertCourse(course);
                        EkkoSyncService.syncManifest(this, course.getId());
                    }

                    // track courses we have seen
                    seen.add(course.getId());
                }

                // broadcast that courses were just updated
                if (courses.getCourses().size() > 0) {
                    broadcastCoursesUpdate(this);
                }

                // update values
                limit = courses.getLimit();
                start = courses.getStart() + limit;
                hasMore = courses.hasMore();
            } else {
                // there was some sort of error preventing courses from being
                // returned
                error = true;
                break;
            }
        }

        // mark any remaining courses inaccessible
        if (!error) {
            boolean updated = false;
            for (final Course course : existing.values()) {
                // only update newly inaccessible courses
                if (course.isAccessible()) {
                    course.setAccessible(false);
                    this.dao.updateCourse(course, new String[] {Contract.Course.COLUMN_NAME_ACCESSIBLE}, false);
                    updated = true;
                }
            }

            // something changed, trigger an update
            if (updated) {
                broadcastCoursesUpdate(this);
            }
        }
    }

    private void syncManifest(final long courseId) {
        this.manifestManager.downloadManifest(courseId);
    }
}
