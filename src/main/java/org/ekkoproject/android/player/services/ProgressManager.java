package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.util.ThreadUtils.assertNotOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.assertOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;
import static org.ekkoproject.android.player.util.ThreadUtils.isUiThread;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Answer;
import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Media;
import org.ekkoproject.android.player.model.Option;
import org.ekkoproject.android.player.model.Progress;
import org.ekkoproject.android.player.model.Question;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.model.Text;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

            Cursor c1 = null;
            Cursor c2 = null;
            try {
                final Set<String> progress = new HashSet<String>();

                // fetch a Cursor for all the regular progress in the specified course
                c1 = this.dao.getCursor(Progress.class, new String[] {Contract.Progress.COLUMN_CONTENT_ID},
                                        Contract.Progress.COLUMN_COURSE_ID + "=?",
                                        new String[] {Long.toString(courseId)}, null);
                final int column1 = c1.getColumnIndex(Contract.Progress.COLUMN_CONTENT_ID);
                if (column1 != -1) {
                    while (c1.moveToNext()) {
                        progress.add(c1.getString(column1));
                    }
                }

                // fetch a Cursor for all the quiz question answers in the specified course
                // XXX: right now we handle answers as progress, this may need to change in the future
                c2 = this.dao.getCursor(Answer.class, new String[] {Contract.Answer.COLUMN_ANSWER_ID},
                                        Contract.Answer.COLUMN_COURSE_ID + " = ?",
                                        new String[] {Long.toString(courseId)}, null);
                final int column2 = c2.getColumnIndex(Contract.Answer.COLUMN_ANSWER_ID);
                if (column2 != -1) {
                    while (c2.moveToNext()) {
                        progress.add(c2.getString(column2));
                    }
                }

                // store the progress
                synchronized (this.progress) {
                    this.progress.put(courseId, progress);
                }

                // broadcast a progress update
                broadcastProgressUpdate(this.context, courseId);

                return Collections.unmodifiableSet(progress);

            } catch (final SQLiteException e) {
                // suppress db exceptions
            } finally {
                if (c1 != null) {
                    c1.close();
                }
                if (c2 != null) {
                    c2.close();
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

                // reload progress
                synchronized (this.progress) {
                    this.progress.remove(courseId);
                }
                this.loadProgress(courseId);
            }
        } catch (final SQLiteException e) {
        }
    }

    public void recordAnswersAsync(final long courseId, final String questionId, final String... answers) {
        assertOnUiThread();
        new RecordAnswersAsyncTask(courseId, questionId, answers).execute();
    }

    public void recordAnswers(final long courseId, final String questionId, final String... answers) {
        assertNotOnUiThread();

        // short-circuit if this is an invalid course
        if (courseId == INVALID_COURSE) {
            return;
        }

        // record answers
        try {
            boolean changed = false;

            final Map<String, Answer> existingAnswers = new HashMap<String, Answer>();
            for (final Answer answer : this.dao.get(Answer.class,
                                                    Contract.Answer.COLUMN_COURSE_ID + " = ? AND " +
                                                            Contract.Answer.COLUMN_QUESTION_ID + " = ?",
                                                    new String[] {Long.toString(courseId), questionId})) {
                existingAnswers.put(answer.getAnswerId(), answer);
            }

            for (final String answerId : answers) {
                // answer already recorded, do nothing
                if (existingAnswers.containsKey(answerId)) {
                    existingAnswers.remove(answerId);
                }
                // new answer, record it
                else {
                    final Answer answer = new Answer(courseId, questionId, answerId);
                    answer.setAnswered();
                    this.dao.insert(answer);
                    changed = true;
                }
            }

            // remove any remaining existing answers
            for (final Answer oldAnswer : existingAnswers.values()) {
                this.dao.delete(oldAnswer);
                changed = true;
            }

            // something changed, we need to update
            if (changed) {
                synchronized (this.progress) {
                    this.progress.remove(courseId);
                }
                this.loadProgress(courseId);
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
                final Pair<Integer, Integer> contentProgress;
                if (content instanceof Lesson) {
                    contentProgress = getLessonProgress(manifest.getCourseId(), (Lesson) content, progress);
                } else if (content instanceof Quiz) {
                    contentProgress = getQuizProgress(manifest.getCourseId(), (Quiz) content, progress);
                } else {
                    // we probably won't hit this, but let's be safe
                    contentProgress = Pair.create(0, 0);
                }

                // add in progress stats
                complete += contentProgress.first;
                total += contentProgress.second;
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
            for (final Text text : lesson.getText()) {
                total++;
                if (progress.contains(text.getId())) {
                    complete++;
                }
            }

            // check if the lesson is complete
            if (progress.contains(lesson.getId())) {
                lessonComplete = true;
            } else if (complete == total && total > 0) {
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

        return Pair.create(lessonComplete ? total : complete, total);
    }

    public static final Pair<Integer, Integer> getQuizProgress(final long courseId, final Quiz quiz,
            final Set<String> progress) {
        int complete = 0;
        int total = 0;
        if (quiz != null && progress != null) {
            // iterate over the questions
            for (final Question question : quiz.getQuestions()) {
                boolean correct = true;
                total++;

                // iterate over the options
                for (final Option option : question.getOptions()) {
                    if (option.isAnswer()) {
                        correct = correct && progress.contains(option.getId());
                    } else {
                        correct = correct && !progress.contains(option.getId());
                    }
                }

                if (correct) {
                    complete++;
                }
            }
        }

        return Pair.create(complete, total);
    }

    private class RecordAnswersAsyncTask extends AsyncTask<Void, Void, Void> {
        private final long courseId;
        private final String questionId;
        private final String[] answers;

        public RecordAnswersAsyncTask(final long courseId, final String questionId, final String... answers) {
            this.courseId = courseId;
            this.questionId = questionId;
            this.answers = answers;
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
            ProgressManager.this.recordAnswers(this.courseId, this.questionId, this.answers);
            return null;
        }
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
