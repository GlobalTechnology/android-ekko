package org.ekkoproject.android.player.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.appdev.entity.Course;
import org.appdev.entity.Resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EkkoDao {
    private final EkkoDbHelper dbHelper;

    // model mapping objects
    private static final Mapper<Course> COURSE_MAPPER = new CourseMapper();
    private static final Mapper<Resource> RESOURCE_MAPPER = new ResourceMapper();

    public EkkoDao(final Context context) {
        this.dbHelper = new EkkoDbHelper(context);
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
        }

        // default to null
        return null;
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
                this.insertResource(course, null, resource);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertResource(final Course course, final Resource parent, final Resource resource) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // insert this resource
            final ContentValues values = RESOURCE_MAPPER.toContentValues(resource);
            values.put(Contract.Course.Resource.COLUMN_NAME_COURSE_ID, course.getId());
            if (parent != null) {
                values.put(Contract.Course.Resource.COLUMN_NAME_PARENT_RESOURCE, parent.getId());
            }
            db.insert(Contract.Course.Resource.TABLE_NAME, null, values);

            // if this resource was dynamic, insert all children resources
            if (resource.isDynamic()) {
                for (final Resource childResource : resource.getResources()) {
                    this.insertResource(course, resource, childResource);
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

    public void close() {
        this.dbHelper.close();
    }
}
