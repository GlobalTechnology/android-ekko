package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.util.ThreadUtils.assertNotOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.assertOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;
import static org.ekkoproject.android.player.util.ThreadUtils.isUiThread;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.appdev.entity.Media;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Progress;
import org.ekkoproject.android.player.model.Quiz;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

public final class ProgressManager {
    /** broadcast actions */
    public static final String ACTION_UPDATE_PROGRESS = ProgressManager.class.getName() + ".ACTION_UPDATE_PROGRESS";

    private static final Object instanceLock = new Object();
    private static ProgressManager instance = null;

    private final Context context;
    private final EkkoDao dao;
    private final ManifestManager manifestManager;

    private final Map<Long, Object> locks = new HashMap<Long, Object>();
    private final Map<Long, Set<String>> progress = new HashMap<Long, Set<String>>();

    private ProgressManager(final Context ctx) {
        this.context = ctx.getApplicationContext();
        this.manifestManager = ManifestManager.getInstance(this.context);
        this.dao = EkkoDao.getInstance(this.context);
    }

    public static final ProgressManager getInstance(final Context context) {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new ProgressManager(context);
                }
            }
        }
        return instance;
    }

    private static void broadcastProgressUpdate(final Context context, final long courseId) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent().setAction(ACTION_UPDATE_PROGRESS).putExtra(EXTRA_COURSEID, courseId));
    }

    public Set<String> getProgress(final long courseId) {
        assertNotOnUiThread();

        // has progress been cached in memory?
        synchronized (this.progress) {
            if (this.progress.containsKey(courseId)) {
                return Collections.unmodifiableSet(this.progress.get(courseId));
            }
        }

        // load the progress from the database
        return this.loadProgress(courseId);
    }

    private Set<String> loadProgress(final long courseId) {
        synchronized (getLock(this.locks, courseId)) {
            // has progress been cached in memory since the last check?
            synchronized (this.progress) {
                if (this.progress.containsKey(courseId)) {
                    return Collections.unmodifiableSet(this.progress.get(courseId));
                }
            }

            // fetch a Cursor for all the progress in the specified course
            Cursor c = null;
            try {
                c = this.dao.getProgressCursor(new String[] { Contract.Progress.COLUMN_NAME_CONTENT_ID },
                        Contract.Progress.COLUMN_NAME_COURSE_ID + "=?", new String[] { Long.toString(courseId) }, null);

                // create a HashSet of all the progress
                final int column = c.getColumnIndex(Contract.Progress.COLUMN_NAME_CONTENT_ID);
                if (column != -1) {
                    final Set<String> progress = new HashSet<String>();
                    while (c.moveToNext()) {
                        progress.add(c.getString(column));
                    }

                    // store the progress
                    synchronized (this.progress) {
                        this.progress.put(courseId, progress);
                    }

                    // broadcast a progress update
                    broadcastProgressUpdate(this.context, courseId);

                    return Collections.unmodifiableSet(progress);
                }
            } catch (final SQLiteException e) {
                // suppress db exceptions
            } finally {
                if (c != null) {
                    c.close();
                }
            }

            // default to no progress if there was an error
            return Collections.emptySet();
        }
    }

    public void recordProgressAsync(final long courseId, final String contentId) {
        assertOnUiThread();

        new RecordProgressAsyncTask(courseId, contentId).execute();
    }

    public void recordProgress(final long courseId, final String contentId) {
        assertNotOnUiThread();

        // short-circuit if this is an invalid course
        if (courseId == INVALID_COURSE) {
            return;
        }

        try {
            // make sure this progress wasn't already recorded
            final Progress existing = this.dao.find(Progress.class, courseId, contentId);
            if (existing == null) {
                // create a new progress entry
                final Progress progress = new Progress(courseId, contentId);
                progress.setCompleted();
                this.dao.insert(progress);

                // remove stale progress data
                synchronized (this.progress) {
                    this.progress.remove(courseId);
                }

                // broadcast a progress update
                broadcastProgressUpdate(this.context, courseId);
            }
        } catch (final SQLiteException e) {
        }
    }

    public final Pair<Integer, Integer> getCourseProgress(final long courseId) {
        assertNotOnUiThread();

        // look up lesson & progress
        final Set<String> progress = this.getProgress(courseId);
        final Manifest manifest = this.manifestManager.getManifest(courseId);

        return getCourseProgress(manifest, progress);
    }

    public final Pair<Integer, Integer> getLessonProgress(final long courseId, final String lessonId) {
        assertNotOnUiThread();

        // look up lesson & progress
        final Set<String> progress = this.getProgress(courseId);
        final Manifest manifest = this.manifestManager.getManifest(courseId);
        final Lesson lesson = manifest != null ? manifest.getLesson(lessonId) : null;

        return getLessonProgress(courseId, lesson, progress);
    }

    public static final Pair<Integer, Integer> getCourseProgress(final Manifest manifest, final Set<String> progress) {
        int complete = 0;
        int total = 0;
        if (manifest != null && progress != null) {
            for (final CourseContent content : manifest.getContent()) {
                if (content instanceof Lesson) {
                    final Pair<Integer, Integer> lessonProgress = getLessonProgress(manifest.getCourseId(),
                            (Lesson) content, progress);
                    complete += lessonProgress.first;
                    total += lessonProgress.second;
                } else if (content instanceof Quiz) {
                    // TODO
                }
            }
        }

        if (total == 0) {
            total = 1;
        }

        return Pair.create(complete, total);
    }

    public static final Pair<Integer, Integer> getLessonProgress(final long courseId, final Lesson lesson,
            final Set<String> progress) {
        boolean lessonComplete = false;
        int complete = 0;
        int total = 0;
        if (lesson != null && progress != null) {
            for (final Media media : lesson.getMedia()) {
                total++;
                if (progress.contains(media.getId())) {
                    complete++;
                }
            }

            // TODO include text progress once we track it

            // check if the lesson is complete
            if (progress.contains(lesson.getId())) {
                lessonComplete = true;
            } else if (complete == total) {
                // lesson should be marked as complete, but isn't currently
                // XXX: this isn't a clean implementation, but it works
                if (instance != null) {
                    if (isUiThread()) {
                        instance.recordProgressAsync(courseId, lesson.getId());
                    } else {
                        instance.recordProgress(courseId, lesson.getId());
                    }
                }
            }
        }

        if (total == 0) {
            total = 1;
        }

        return Pair.create(lessonComplete ? total : complete, total);
    }

    private class RecordProgressAsyncTask extends AsyncTask<Void, Void, Void> {
        private final long courseId;
        private final String contentId;

        public RecordProgressAsyncTask(final long courseId, final String contentId) {
            this.courseId = courseId;
            this.contentId = contentId;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public AsyncTask<Void, Void, Void> execute() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return this.executeOnExecutor(THREAD_POOL_EXECUTOR);
            } else {
                return this.execute(new Void[] {});
            }
        }

        @Override
        protected Void doInBackground(final Void... params) {
            ProgressManager.this.recordProgress(this.courseId, this.contentId);
            return null;
        }
    }
}
