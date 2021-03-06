package org.ekkoproject.android.player.sync;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.EXTRA_GUID;
import static org.ekkoproject.android.player.Constants.FUNCTION_COURSE_TO_COURSE_ID;
import static org.ekkoproject.android.player.Constants.FUNCTION_PERMISSION_TO_COURSE_ID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.services.ResourceManager.FLAG_TYPE_IMAGE;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.app.ThreadedIntentService;
import org.ccci.gto.android.common.db.AbstractDao.Transaction;
import org.ekkoproject.android.player.BroadcastUtils;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.services.ManifestManager;
import org.ekkoproject.android.player.services.ResourceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EkkoSyncService extends ThreadedIntentService {
    // actions this service can broadcast
    public static final String ACTION_UPDATE_COURSES = EkkoSyncService.class.getName() + ".ACTION_UPDATE_COURSES";

    // data passed to this service in Intents
    public static final String EXTRA_SYNCTYPE = EkkoSyncService.class.getName() + ".EXTRA_SYNCTYPE";
    public static final String EXTRA_RESOURCE = EkkoSyncService.class.getName() + ".EXTRA_RESOURCE";
    public static final String EXTRA_IMAGE_RESOURCE = EkkoSyncService.class.getName() + ".EXTRA_IMAGE_RESOURCE";

    public static final int SYNCTYPE_COURSES = 1;
    public static final int SYNCTYPE_COURSE = 2;
    public static final int SYNCTYPE_MANIFEST = 3;
    public static final int SYNCTYPE_RESOURCE = 4;

    private EkkoDao dao;
    private ManifestManager manifestManager;
    private ResourceManager mResources;

    public EkkoSyncService() {
        super("ekko_sync_service");
    }

    public static void broadcastCoursesUpdate(final Context context, final long... courses) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(BroadcastUtils.updateCoursesBroadcast(courses));
    }

    public static void syncCourses(final Context context, final String guid) {
        final Intent intent = new Intent(context, EkkoSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSES);
        intent.putExtra(EXTRA_GUID, guid);
        context.startService(intent);
    }

    public static void syncCourse(final Context context, final String guid, final long courseId) {
        final Intent intent = new Intent(context, EkkoSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSE);
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_COURSEID, courseId);
        context.startService(intent);
    }

    public static void syncManifest(final Context context, final String guid, final long courseId) {
        final Intent intent = new Intent(context, EkkoSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_MANIFEST);
        intent.putExtra(EXTRA_GUID, guid);
        intent.putExtra(EXTRA_COURSEID, courseId);
        context.startService(intent);
    }

    public static void syncResource(final Context context, final long courseId, final String resourceId,
                                    final boolean image) {
        final Intent intent = new Intent(context, EkkoSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_RESOURCE);
        intent.putExtra(EXTRA_COURSEID, courseId);
        intent.putExtra(EXTRA_RESOURCE, resourceId);
        intent.putExtra(EXTRA_IMAGE_RESOURCE, image);
        context.startService(intent);
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate() {
        super.onCreate();
        this.dao = EkkoDao.getInstance(this);
        this.manifestManager = ManifestManager.getInstance(this);
        mResources = ResourceManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final int syncType = intent.getIntExtra(EXTRA_SYNCTYPE, 0);
        final String guid = intent.getStringExtra(EXTRA_GUID);
        final long courseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        try {
            switch (syncType) {
                case SYNCTYPE_COURSES:
                    this.syncCourses(guid);
                    break;
                case SYNCTYPE_COURSE:
                    this.syncCourse(guid, courseId);
                    break;
                case SYNCTYPE_MANIFEST:
                    this.syncManifest(guid, courseId);
                    break;
                case SYNCTYPE_RESOURCE:
                    final String resourceId = intent.getStringExtra(EXTRA_RESOURCE);
                    final boolean image = intent.getBooleanExtra(EXTRA_IMAGE_RESOURCE, false);
                    this.syncResource(courseId, resourceId, image);
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
        this.manifestManager = null;
    }

    /* END lifecycle */

    /**
     * synchronize all available courses from the hub
     *
     * @throws ApiSocketException
     * @throws InvalidSessionApiException
     */
    private void syncCourses(final String guid) throws ApiSocketException, InvalidSessionApiException {
        // load all existing courses
        final LongSparseArray<Course> existing = new LongSparseArray<>();
        for (final Course course : this.dao.getCourses(null, null, null, false)) {
            if (course != null) {
                existing.put(course.getId(), course);
            }
        }

        final EkkoHubApi api = this.getApi(guid);
        boolean error = false;
        boolean hasMore = true;
        int start = 0;
        int limit = 50;
        final Set<Long> visible = new HashSet<>();
        while (hasMore) {
            final CourseList courses = api.getCourseList(start, limit);
            if (courses != null) {
                final Transaction tx = this.dao.beginTransaction();
                try {
                    for (final Course course : courses) {
                        this.processCourse(guid, course, true);

                        // record as visible if we have permissions
                        final Permission permission = course.getPermission();
                        if (permission != null) {
                            // track this course as visible
                            visible.add(permission.getCourseId());
                        }
                    }
                    tx.setTransactionSuccessful();
                } finally {
                    tx.endTransaction();
                }

                // broadcast that courses were just updated
                if (courses.size() > 0) {
                    broadcastCoursesUpdate(this, Longs.toArray(
                            Collections2.transform(courses, FUNCTION_COURSE_TO_COURSE_ID)));
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

        if (!error) {
            // delete any permissions not returned
            final Transaction tx = this.dao.beginTransaction();
            try {
                final List<Permission> known =
                        this.dao.get(Permission.class, Contract.Permission.COLUMN_GUID + " = ?", new String[] {guid});
                final List<Permission> toDelete =
                        new ArrayList<>(Collections2.filter(known, new Predicate<Permission>() {
                            @Override
                            public boolean apply(final Permission permission) {
                                return !visible.contains(permission.getCourseId());
                            }
                        }));

                for (final Permission permission : toDelete) {
                    this.dao.delete(permission);
                }

                // broadcast a courses update
                if (known.size() > 0) {
                    broadcastCoursesUpdate(this, Longs.toArray(
                            Collections2.transform(toDelete, FUNCTION_PERMISSION_TO_COURSE_ID)));
                }

                tx.setTransactionSuccessful();
            } finally {
                tx.endTransaction();
            }
        }
    }

    private void syncCourse(final String guid, final long courseId)
            throws ApiSocketException, InvalidSessionApiException {
        final Course course = this.getApi(guid).getCourse(courseId);
        if (course != null) {
            this.processCourse(guid, course, true);
            broadcastCoursesUpdate(this, courseId);
        } else {
            // we didn't get a course back, so remove permissions for the current user
            this.dao.delete(new Permission(courseId, guid));
        }
    }

    private void syncManifest(final String guid, final long courseId) {
        final Course course = this.dao.find(Course.class, courseId);
        if (guid != null && course != null) {
            // is there a newer manifest available?
            if (course.getVersion() > course.getManifestVersion()) {
                // make sure we can actually access the manifest before attempting to download it
                final Permission permission = this.dao.find(Permission.class, guid, course.getId());
                if (permission != null && permission.isContentVisible()) {
                    this.manifestManager.downloadManifest(courseId, guid);
                }
            }
        }
    }

    private void syncResource(final long courseId, final String resourceId, final boolean image) {
        final Resource resource = mResources.resolveResource(courseId, resourceId);
        final int flags = (image ? FLAG_TYPE_IMAGE : 0);
        mResources.getFile(resource, flags);
    }

    private EkkoHubApi getApi(final String guid) {
        return EkkoHubApi.getInstance(this, guid);
    }

    private void processCourse(final String guid, final Course course, final boolean containsResources) {
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

        // schedule a manifest sync
        EkkoSyncService.syncManifest(this, guid, course.getId());

        // schedule an automated download of the banner resource
        EkkoSyncService.syncResource(this, course.getId(), course.getBanner(), true);
    }
}
