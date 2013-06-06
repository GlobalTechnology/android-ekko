package org.ekkoproject.android.player.sync;

import org.appdev.entity.Course;
import org.appdev.entity.CourseList;
import org.ekkoproject.android.player.api.ApiSocketException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.api.InvalidSessionApiException;
import org.ekkoproject.android.player.db.EkkoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class EkkoSyncService extends IntentService {
    private final static Logger LOG = LoggerFactory.getLogger(EkkoSyncService.class);

    // data passed to this service in Intents
    public static final String EXTRA_SYNCTYPE = "org.ekkoproject.android.player.sync.SYNCTYPE";

    public static final int SYNCTYPE_COURSES = 1;
    public static final int SYNCTYPE_MANIFEST = 2;

    private final EkkoDao dao;
    private final EkkoHubApi ekkoApi;

    public EkkoSyncService() {
        super("ekko_sync_service");
        this.dao = new EkkoDao(this);
        this.ekkoApi = new EkkoHubApi(this);
    }

    public static void syncCourses(final Context context) {
        LOG.debug("sending syncCourses intent");
        context.startService(new Intent(context, EkkoSyncService.class).putExtra(EXTRA_SYNCTYPE, SYNCTYPE_COURSES));
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        LOG.debug("handling an intent");
        final int syncType = intent.getIntExtra(EXTRA_SYNCTYPE, 0);
        try {
            switch (syncType) {
            case SYNCTYPE_COURSES:
                this.syncCourses();
                break;
            }
        } catch (final ApiSocketException e) {
            // TODO
        } catch (final InvalidSessionApiException e) {
            // TODO this should broadcast a request auth intent
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dao.close();
    }

    /**
     * synchronize all available courses from the hub
     * 
     * @throws ApiSocketException
     * @throws InvalidSessionApiException
     */
    private void syncCourses() throws ApiSocketException, InvalidSessionApiException {
        LOG.debug("triggering a courses sync");

        final CourseList courses = this.ekkoApi.getCourseList(0, 50);

        for (final Course course : courses.getCourselist()) {
            course.setLastSynced();

            final Course existing = this.dao.findCourse(course.getId(), false);
            if (existing != null) {
                LOG.debug("updating course: {}", course.getId());
                this.dao.update(course);
            } else {
                LOG.debug("inserting course: {}", course.getId());
                this.dao.insert(course);
            }
        }
    }
}
