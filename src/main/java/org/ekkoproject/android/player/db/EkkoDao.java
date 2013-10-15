package org.ekkoproject.android.player.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.appdev.entity.Resource;
import org.ccci.gto.android.common.db.Mapper;
import org.ekkoproject.android.player.model.Answer;
import org.ekkoproject.android.player.model.CachedResource;
import org.ekkoproject.android.player.model.CachedUriResource;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Progress;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EkkoDao {
    private final EkkoDbHelper dbHelper;

    // model mapping objects
    private static final Mapper<Course> COURSE_MAPPER = new CourseMapper();
    private static final Mapper<Resource> RESOURCE_MAPPER = new ResourceMapper();
    private static final Mapper<CachedResource> CACHED_RESOURCE_MAPPER = new CachedResourceMapper();
    private static final Mapper<CachedUriResource> CACHED_URI_RESOURCE_MAPPER = new CachedUriResourceMapper();
    private static final Mapper<Progress> PROGRESS_MAPPER = new ProgressMapper();
    private static final Mapper<Answer> ANSWER_MAPPER = new AnswerMapper();

    private static Object instanceLock = new Object();
    private static EkkoDao instance = null;

    private EkkoDao(final Context context) {
        this.dbHelper = new EkkoDbHelper(context);
    }

    public static final EkkoDao getInstance(final Context context) {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new EkkoDao(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T find(final Class<T> entityClass, final Object... key) {
        if (entityClass.equals(Course.class)) {
            if (key.length != 1) {
                throw new IllegalArgumentException("invalid Course key");
            }
            final Cursor c = this.getCoursesCursor(Contract.Course.COLUMN_NAME_COURSE_ID + " = ?",
                    new String[] { key[0].toString() }, null);

            if (c.getCount() > 0) {
                // get the first node & close the cursor
                c.moveToFirst();
                final Course course = COURSE_MAPPER.toObject(c);
                c.close();

                // return the loaded node
                return (T) course;
            }
        } else if (entityClass.equals(CachedResource.class)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid CachedResource key");
            }
            final Cursor c = this.getCachedResourceCursor(Contract.CachedResource.COLUMN_NAME_COURSE_ID + " = ? AND "
                    + Contract.CachedResource.COLUMN_NAME_SHA1 + " = ?",
                    new String[] { key[0].toString(), key[1].toString() }, null);

            if (c.getCount() > 0) {
                // get the first node & close the cursor
                c.moveToFirst();
                final CachedResource resource = CACHED_RESOURCE_MAPPER.toObject(c);
                c.close();

                // return the loaded node
                return (T) resource;
            }
        } else if (entityClass.equals(CachedUriResource.class)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid CachedUriResource key");
            }
            final Cursor c = this.getCachedUriResourceCursor(Contract.CachedUriResource.COLUMN_NAME_COURSE_ID
                    + " = ? AND " + Contract.CachedUriResource.COLUMN_NAME_URI + " = ?",
                    new String[] { key[0].toString(), key[1].toString() }, null);

            if (c.getCount() > 0) {
                // get the first node & close the cursor
                c.moveToFirst();
                final CachedUriResource resource = CACHED_URI_RESOURCE_MAPPER.toObject(c);
                c.close();

                // return the loaded node
                return (T) resource;
            }
        } else if (entityClass.equals(Progress.class)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid Progress key");
            }
            final Cursor c = this.getProgressCursor(Contract.Progress.COLUMN_NAME_COURSE_ID + " = ? AND "
                    + Contract.Progress.COLUMN_NAME_CONTENT_ID + " = ?",
                    new String[] { key[0].toString(), key[1].toString() }, null);

            if (c.getCount() > 0) {
                // get the first node & close the cursor
                c.moveToFirst();
                final Progress progress = PROGRESS_MAPPER.toObject(c);
                c.close();

                // return the loaded node
                return (T) progress;
            }
        } else if (entityClass.equals(Answer.class)) {
            if (key.length != 3) {
                throw new IllegalArgumentException("invalid Answer key");
            }
            final Cursor c = this.getAnswerCursor(Contract.Answer.COLUMN_NAME_COURSE_ID + " = ? AND "
                    + Contract.Answer.COLUMN_NAME_QUESTION_ID + " = ? AND " + Contract.Answer.COLUMN_NAME_ANSWER_ID
                    + " = ?", new String[] { key[0].toString(), key[1].toString(), key[2].toString() }, null);

            if (c.getCount() > 0) {
                // get the first node & close the cursor
                c.moveToFirst();
                final Answer answer = ANSWER_MAPPER.toObject(c);
                c.close();

                // return the loaded node
                return (T) answer;
            }
        }

        // default to null
        return null;
    }

    public void insert(final Object obj) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // get parts of query based on the object type
            final String table;
            final ContentValues values;
            if (obj instanceof Answer) {
                table = Contract.Answer.TABLE_NAME;
                values = ANSWER_MAPPER.toContentValues((Answer) obj);
            } else if (obj instanceof Progress) {
                table = Contract.Progress.TABLE_NAME;
                values = PROGRESS_MAPPER.toContentValues((Progress) obj);
            } else if (obj instanceof CachedResource) {
                table = Contract.CachedResource.TABLE_NAME;
                values = CACHED_RESOURCE_MAPPER.toContentValues((CachedResource) obj);
            } else if (obj instanceof CachedUriResource) {
                table = Contract.CachedUriResource.TABLE_NAME;
                values = CACHED_URI_RESOURCE_MAPPER.toContentValues((CachedUriResource) obj);
            } else {
                throw new IllegalArgumentException("invalid object passed to EkkoDao.insert");
            }

            // execute insert
            db.insert(table, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * retrieve all objects of the specified type
     * 
     * @param clazz
     *            the type of object to retrieve
     * @return
     */
    public <T> List<T> get(final Class<T> clazz) {
        return this.get(clazz, true);
    }

    public <T> List<T> get(final Class<T> clazz, final boolean loadChildren) {
        return this.get(clazz, null, null, null, loadChildren);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> get(final Class<T> clazz, final String whereClause, final String[] whereBindValues,
            final String orderBy, final boolean loadChildren) {
        final Cursor c;
        final Mapper<T> mapper;
        if (Course.class.equals(clazz)) {
            c = this.getCoursesCursor(whereClause, whereBindValues, orderBy);
            mapper = (Mapper<T>) COURSE_MAPPER;
        } else {
            throw new IllegalArgumentException("invalid class specified");
        }

        // load all rows from the cursor
        final List<T> results = new ArrayList<T>();
        while (c.moveToNext()) {
            results.add(mapper.toObject(c));
        }

        // close the cursor to prevent leaking it
        c.close();

        if (loadChildren) {
            if (Course.class.equals(clazz)) {
                // load resources
                for (final Course course : (List<Course>) results) {
                    this.loadResources(course);
                }
            }
        }

        // return the results
        return results;
    }

    public void replace(final Object obj) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            this.delete(obj);
            this.insert(obj);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void delete(final Object obj) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // get parts of query based on the object type
            final String table;
            final String whereClause;
            final String[] whereArgs;
            if (obj instanceof Answer) {
                table = Contract.Answer.TABLE_NAME;
                whereClause = Contract.Answer.COLUMN_NAME_COURSE_ID + " = ? AND "
                        + Contract.Answer.COLUMN_NAME_QUESTION_ID + " = ? AND " + Contract.Answer.COLUMN_NAME_ANSWER_ID
                        + " = ?";
                whereArgs = new String[] { Long.toString(((Answer) obj).getCourseId()), ((Answer) obj).getQuestionId(),
                        ((Answer) obj).getAnswerId() };
            } else if (obj instanceof Progress) {
                table = Contract.Progress.TABLE_NAME;
                whereClause = Contract.Progress.COLUMN_NAME_COURSE_ID + " = ? AND "
                        + Contract.Progress.COLUMN_NAME_CONTENT_ID + " = ?";
                whereArgs = new String[] { Long.toString(((Progress) obj).getCourseId()),
                        ((Progress) obj).getContentId() };
            } else if (obj instanceof CachedResource) {
                table = Contract.CachedResource.TABLE_NAME;
                whereClause = Contract.CachedResource.COLUMN_NAME_COURSE_ID + " = ? AND "
                        + Contract.CachedResource.COLUMN_NAME_SHA1 + " = ?";
                whereArgs = new String[] { Long.toString(((CachedResource) obj).getCourseId()),
                        ((CachedResource) obj).getSha1() };
            } else if (obj instanceof CachedUriResource) {
                table = Contract.CachedUriResource.TABLE_NAME;
                whereClause = Contract.CachedUriResource.COLUMN_NAME_COURSE_ID + " = ? AND "
                        + Contract.CachedUriResource.COLUMN_NAME_URI + " = ?";
                whereArgs = new String[] { Long.toString(((CachedUriResource) obj).getCourseId()),
                        ((CachedUriResource) obj).getUri() };
            } else {
                throw new IllegalArgumentException("invalid object passed to EkkoDao.delete");
            }

            // execute delete
            db.delete(table, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Course findCourse(final long id) {
        return this.findCourse(id, true);
    }

    public Course findCourse(final long id, final boolean loadResources) {
        final Course course = this.find(Course.class, id);

        if (loadResources) {
            this.loadResources(course);
        }

        return course;
    }

    public Cursor getCoursesCursor() {
        return this.getCoursesCursor(null, null, Contract.Course.COLUMN_NAME_TITLE + " COLLATE NOCASE");
    }

    public Cursor getCoursesCursor(final String whereClause, final String[] whereBindValues) {
        return this.getCoursesCursor(whereClause, whereBindValues, Contract.Course.COLUMN_NAME_TITLE
                + " COLLATE NOCASE");
    }

    public Cursor getCoursesCursor(final String whereClause, final String[] whereBindValues, final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.Course.TABLE_NAME,
                Contract.Course.PROJECTION_ALL, whereClause, whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    public Cursor getResourcesCursor(final String whereClause, final String[] whereBindValues, final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.Course.Resource.TABLE_NAME,
                Contract.Course.Resource.PROJECTION_ALL, whereClause, whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    public Cursor getCachedResourceCursor(final String whereClause, final String[] whereBindValues, final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.CachedResource.TABLE_NAME,
                Contract.CachedResource.PROJECTION_ALL, whereClause, whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    public Cursor getCachedUriResourceCursor(final String whereClause, final String[] whereBindValues,
            final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.CachedUriResource.TABLE_NAME,
                Contract.CachedUriResource.PROJECTION_ALL, whereClause, whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    public Cursor getProgressCursor(final String whereClause, final String[] whereBindValues, final String orderBy) {
        return this.getProgressCursor(Contract.Progress.PROJECTION_ALL, whereClause, whereBindValues, orderBy);
    }

    public Cursor getProgressCursor(final String[] projection, final String whereClause,
            final String[] whereBindValues, final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.Progress.TABLE_NAME, projection,
                whereClause, whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    public Cursor getAnswerCursor(final String whereClause, final String[] whereBindValues, final String orderBy) {
        return this.getAnswerCursor(Contract.Answer.PROJECTION_ALL, whereClause, whereBindValues, orderBy);
    }

    public Cursor getAnswerCursor(final String[] projection, final String whereClause, final String[] whereBindValues,
            final String orderBy) {
        final Cursor c = this.dbHelper.getReadableDatabase().query(Contract.Answer.TABLE_NAME, projection, whereClause,
                whereBindValues, null, null, orderBy);

        if (c != null) {
            c.moveToPosition(-1);
        }

        return c;
    }

    private void loadResources(final Course course) {
        if (course != null) {
            // clear the current resources
            course.setResources(null);

            // retrieve all the resources for this course
            final Cursor c = this.getResourcesCursor(Contract.Course.Resource.COLUMN_NAME_COURSE_ID + " = ?",
                    new String[] { Long.toString(course.getId()) }, null);

            // process the resources
            final HashMap<String, List<Resource>> childResources = new HashMap<String, List<Resource>>();
            while (c.moveToNext()) {
                final Resource resource = RESOURCE_MAPPER.toObject(c);

                // is this resource a child of a dynamic resource
                final String parent = c.getString(c
                        .getColumnIndex(Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE));
                if (parent != null) {
                    if (!childResources.containsKey(parent)) {
                        childResources.put(parent, new ArrayList<Resource>());
                    }

                    childResources.get(parent).add(resource);
                } else {
                    course.addResource(resource);
                }
            }

            // close the cursor to prevent leaking it
            c.close();

            // store child resources in dynamic resources
            for (final Entry<String, List<Resource>> entry : childResources.entrySet()) {
                final Resource resource = course.getResource(entry.getKey());
                if (resource != null && resource.isDynamic()) {
                    resource.setResources(entry.getValue());
                }
            }
        }
    }

    public void insert(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.insert(Contract.Course.TABLE_NAME, null, COURSE_MAPPER.toContentValues(course));
            this.insertResources(course);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void update(final Course course) {
        this.update(course, Contract.Course.PROJECTION_ALL);
    }

    public void update(final Course course, final String[] projection) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.update(Contract.Course.TABLE_NAME, COURSE_MAPPER.toContentValues(course, projection),
                    Contract.Course.COLUMN_NAME_COURSE_ID + " = ?", new String[] { Long.toString(course.getId()) });
            this.deleteResources(course);
            this.insertResources(course);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void replace(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            this.delete(course);
            this.insert(course);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void delete(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            this.deleteResources(course);
            db.delete(Contract.Course.TABLE_NAME, Contract.Course.COLUMN_NAME_COURSE_ID + " = ?",
                    new String[] { Long.toString(course.getId()) });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertResources(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (final Resource resource : course.getResources()) {
                this.insertResource(resource, null);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertResource(final Resource resource, final Resource parent) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // insert this resource
            final ContentValues values = RESOURCE_MAPPER.toContentValues(resource);
            if (parent != null) {
                values.put(Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE, parent.getId());
            }
            db.insert(Contract.Course.Resource.TABLE_NAME, null, values);

            // if this resource was dynamic, insert all children resources
            if (resource.isDynamic()) {
                for (final Resource childResource : resource.getResources()) {
                    this.insertResource(childResource, resource);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void deleteResources(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Contract.Course.Resource.TABLE_NAME, Contract.Course.Resource.COLUMN_NAME_COURSE_ID + " = ?",
                    new String[] { Long.toString(course.getId()) });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void clearProgress(final long courseId) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Contract.Progress.TABLE_NAME, Contract.Progress.COLUMN_NAME_COURSE_ID + " = ?",
                    new String[] { Long.toString(courseId) });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Answer> getAnswers(final String whereClause, final String[] whereBindValues) {
        final Cursor c = this.getAnswerCursor(whereClause, whereBindValues, null);

        // process the answers
        final List<Answer> answers = new ArrayList<Answer>();
        while (c.moveToNext()) {
            answers.add(ANSWER_MAPPER.toObject(c));
        }

        // close the cursor
        c.close();

        return answers;
    }
}
