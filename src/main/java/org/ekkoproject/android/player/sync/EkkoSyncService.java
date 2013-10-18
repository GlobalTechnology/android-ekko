package org.ekkoproject.android.player.sync;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.ccci.gto.android.thekey.TheKeyImpl;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.services.ManifestManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.thekey.android.TheKey;

public class EkkoSyncService extends ThreadedIntentService {
    // actions this service can broadcast
    public static final String ACTION_UPDATE_COURSES = "org.ekkoproject.android.player.sync.EkkoSyncService.UPDATE_COURSES";

    // data passed to this service in Intents
    public static final String EXTRA_SYNCTYPE = "org.ekkoproject.android.player.sync.EkkoSyncService.SYNCTYPE";

    public static final int SYNCTYPE_COURSES = 1;
    public static final int SYNCTYPE_COURSE = 2;
    public static final int SYNCTYPE_MANIFEST = 3;

    private EkkoDao dao;
    private EkkoHubApi ekkoApi;
    private ManifestManager manifestManager;
    private TheKey thekey;

    public EkkoSyncService() {
        super("ekko_sync_service");
    }

    public static void broadcastCoursesUpdate(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_UPDATE_COURSES));
    }

    public static void syncCourses(final Context context) {
        context.startService(new Intent(context, EkkoSyncService.class).putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSES));
    }

    public static void syncCourse(final Context context, final long courseId) {
        final Intent intent = new Intent(context, EkkoSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSE);
        intent.putExtra(EXTRA_COURSEID, courseId);
        context.startService(intent);
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
        this.ekkoApi = EkkoHubApi.getInstance(this);
        this.manifestManager = ManifestManager.getInstance(this);
        this.thekey = new TheKeyImpl(this, THEKEY_CLIENTID);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final int syncType = intent.getIntExtra(EXTRA_SYNCTYPE, 0);
        try {
            switch (syncType) {
            case SYNCTYPE_COURSES:
                this.syncCourses();
                break;
                case SYNCTYPE_COURSE:
                    this.syncCourse(intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE));
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
        final LongSparseArray<Course> existing = new LongSparseArray<Course>();
        for (final Course course : this.dao.getCourses(null, null, null, false)) {
            if (course != null) {
                existing.put(course.getId(), course);
            }
        }

        boolean error = false;
        boolean hasMore = true;
        int start = 0;
        int limit = 50;
        final Map<String, Set<Long>> visible = new HashMap<String, Set<Long>>();
        while (hasMore) {
            final CourseList courses = this.ekkoApi.getCourseList(start, limit);
            if (courses != null) {
                final Transaction tx = this.dao.beginTransaction();
                try {
                    for (final Course course : courses.getCourses()) {
                        this.processCourse(course, true);

                        // record as visible if we have permissions
                        final Permission permission = course.getPermission();
                        if (permission != null) {
                            // track this course as visible
                            final String guid = permission.getGuid();
                            if (!visible.containsKey(guid)) {
                                visible.put(guid, new HashSet<Long>());
                            }
                            visible.get(guid).add(permission.getCourseId());
                        }
                    }
                    tx.setTransactionSuccessful();
                } finally {
                    tx.endTransaction();
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

        if(!error) {
            // delete any permissions not returned
            final Transaction tx = this.dao.beginTransaction();
            try {
                for (final Map.Entry<String, Set<Long>> entry : visible.entrySet()) {
                    final String guid = entry.getKey();
                    final Set<Long> ids = entry.getValue();

                    final List<Permission> known = this.dao
                            .get(Permission.class, Contract.Permission.COLUMN_GUID + " = ?", new String[] {guid});
                    for (final Permission permission : known) {
                        if (!ids.contains(permission.getCourseId())) {
                            this.dao.delete(permission);
                        }
                    }
                }
                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction();
            }
        }
    }

    private void syncCourse(final long courseId) throws ApiSocketException, InvalidSessionApiException {
        final Course course = this.ekkoApi.getCourse(courseId);
        if (course != null) {
            this.processCourse(course, true);
            broadcastCoursesUpdate(this);
        } else {
            // we didn't get a course back, so remove permissions for the current user
            final String guid = this.thekey.getGuid();
            this.dao.delete(new Permission(courseId, guid));
        }
    }

    private void syncManifest(final long courseId) {
        final Course course = this.dao.find(Course.class, courseId);
        if (course != null) {
            // is there a newer manifest available?
            if (course.getVersion() > course.getManifestVersion()) {
                // make sure we can actually access the manifest before attempting to download it
                final String guid;
                final Permission permission;
                if ((guid = this.thekey.getGuid()) != null &&
                        (permission = this.dao.find(Permission.class, guid, course.getId())) != null &&
                        permission.isContentVisible()) {
                    this.manifestManager.downloadManifest(courseId);
                }
            }
        }
    }

    private void processCourse(final Course course, final boolean containsResources) {
        final Transaction tx = this.dao.beginTransaction();
        try {
            // update sync date
            course.setLastSynced();

            // update/insert course & resources
            if (containsResources) {
                this.dao.deleteResources(course);
            }
            this.dao.updateOrInsert(course, Contract.Course.PROJECTION_UPDATE_EKKOHUB);
            if (containsResources) {
                this.dao.insertResources(course);
            }

            // schedule a manifest sync
            EkkoSyncService.syncManifest(this, course.getId());

            // update the permissions for this course
            final Permission permission = course.getPermission();
            if (permission != null) {
                this.dao.updateOrInsert(permission, new String[] {Contract.Permission.COLUMN_ADMIN,
                        Contract.Permission.COLUMN_ENROLLED, Contract.Permission.COLUMN_PENDING,
                        Contract.Permission.COLUMN_CONTENT_VISIBLE});
            }
            tx.setTransactionSuccessful();
        } finally {
            tx.endTransaction();
        }
    }
}
