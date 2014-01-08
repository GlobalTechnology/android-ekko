package org.ekkoproject.android.player.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import org.ccci.gto.android.common.db.AbstractDao;
import org.ccci.gto.android.common.db.Mapper;
import org.ekkoproject.android.player.model.Answer;
import org.ekkoproject.android.player.model.CachedFileResource;
import org.ekkoproject.android.player.model.CachedUriResource;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Permission;
import org.ekkoproject.android.player.model.Progress;
import org.ekkoproject.android.player.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EkkoDao extends AbstractDao {
    // model mapping objects
    private static final Mapper<Permission> ACCESS_MAPPER = new PermissionMapper();
    private static final Mapper<Course> COURSE_MAPPER = new CourseMapper();
    private static final Mapper<Resource> RESOURCE_MAPPER = new ResourceMapper();
    private static final Mapper<CachedFileResource> CACHED_RESOURCE_MAPPER = new CachedFileResourceMapper();
    private static final Mapper<CachedUriResource> CACHED_URI_RESOURCE_MAPPER = new CachedUriResourceMapper();
    private static final Mapper<Progress> PROGRESS_MAPPER = new ProgressMapper();
    private static final Mapper<Answer> ANSWER_MAPPER = new AnswerMapper();

    private static final Object LOCK_INSTANCE = new Object();
    private static EkkoDao instance = null;

    private EkkoDao(final Context context) {
        super(new EkkoDbHelper(context.getApplicationContext()));
    }

    public static EkkoDao getInstance(final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (instance == null) {
                instance = new EkkoDao(context.getApplicationContext());
            }
        }

        return instance;
    }

    @Override
    protected String getTable(final Class<?> clazz) {
        if (Course.class.equals(clazz)) {
            return Contract.Course.TABLE_NAME;
        } else if (Permission.class.equals(clazz)) {
            return Contract.Permission.TABLE_NAME;
        } else if (Resource.class.equals(clazz)) {
            return Contract.Course.Resource.TABLE_NAME;
        } else if (Answer.class.equals(clazz)) {
            return Contract.Answer.TABLE_NAME;
        } else if (Progress.class.equals(clazz)) {
            return Contract.Progress.TABLE_NAME;
        } else if (CachedFileResource.class.equals(clazz)) {
            return Contract.CachedFileResource.TABLE_NAME;
        } else if (CachedUriResource.class.equals(clazz)) {
            return Contract.CachedUriResource.TABLE_NAME;
        }

        return super.getTable(clazz);
    }

    @Override
    protected String getJoin(final Class<?> base, final String type, final Class<?> join) {
        if(Course.class.equals(base)) {
            if(Permission.class.equals(join)) {
                return buildJoin(Permission.class, type, Contract.Permission.SQL_JOIN_COURSE);
            }
        }

        return super.getJoin(base, type, join);
    }

    @Override
    protected String[] getFullProjection(final Class<?> clazz) {
        if (Course.class.equals(clazz)) {
            return Contract.Course.PROJECTION_ALL;
        } else if (Permission.class.equals(clazz)) {
            return Contract.Permission.PROJECTION_ALL;
        } else if (Resource.class.equals(clazz)) {
            return Contract.Course.Resource.PROJECTION_ALL;
        } else if (CachedFileResource.class.equals(clazz)) {
            return Contract.CachedFileResource.PROJECTION_ALL;
        } else if (CachedUriResource.class.equals(clazz)) {
            return Contract.CachedUriResource.PROJECTION_ALL;
        } else if (Progress.class.equals(clazz)) {
            return Contract.Progress.PROJECTION_ALL;
        } else if (Answer.class.equals(clazz)) {
            return Contract.Answer.PROJECTION_ALL;
        }

        return super.getFullProjection(clazz);
    }

    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(final Object obj) {
        if (obj instanceof Course) {
            return this.getPrimaryKeyWhere(Course.class, ((Course) obj).getId());
        } else if (obj instanceof Permission) {
            final Permission permission = (Permission) obj;
            return this.getPrimaryKeyWhere(Permission.class, permission.getGuid(), permission.getCourseId());
        } else if (obj instanceof Answer) {
            final Answer answer = (Answer) obj;
            return this.getPrimaryKeyWhere(Answer.class, answer.getGuid(), answer.getCourseId(), answer.getQuestionId(),
                                           answer.getAnswerId());
        } else if (obj instanceof Progress) {
            return this.getPrimaryKeyWhere(Progress.class, ((Progress) obj).getGuid(), ((Progress) obj).getCourseId(),
                                           ((Progress) obj).getContentId());
        } else if (obj instanceof CachedUriResource) {
            return this.getPrimaryKeyWhere(CachedUriResource.class, ((CachedUriResource) obj).getCourseId(),
                                           ((CachedUriResource) obj).getUri());
        } else if (obj instanceof CachedFileResource) {
            return this.getPrimaryKeyWhere(CachedFileResource.class, ((CachedFileResource) obj).getCourseId(),
                                           ((CachedFileResource) obj).getSha1());
        }

        return super.getPrimaryKeyWhere(obj);
    }

    @Override
    protected Pair<String, String[]> getPrimaryKeyWhere(final Class<?> clazz, final Object... key) {
        // generate the where clause for the specified class
        final String where;
        if (Course.class.equals(clazz)) {
            if (key.length != 1) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Course.SQL_WHERE_PRIMARY_KEY;
        } else if (Permission.class.equals(clazz)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Permission.SQL_WHERE_PRIMARY_KEY;
        } else if (Answer.class.equals(clazz)) {
            if (key.length != 4) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Answer.SQL_WHERE_PRIMARY_KEY;
        } else if (Progress.class.equals(clazz)) {
            if (key.length != 3) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.Progress.SQL_WHERE_PRIMARY_KEY;
        } else if (CachedFileResource.class.equals(clazz)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.CachedFileResource.SQL_WHERE_PRIMARY_KEY;
        } else if (CachedUriResource.class.equals(clazz)) {
            if (key.length != 2) {
                throw new IllegalArgumentException("invalid key for " + clazz);
            }
            where = Contract.CachedUriResource.SQL_WHERE_PRIMARY_KEY;
        } else {
            return super.getPrimaryKeyWhere(clazz, key);
        }
        final String[] whereBindValues = new String[key.length];
        for (int i = 0; i < key.length; i++) {
            whereBindValues[i] = key[i].toString();
        }

        // return where clause pair
        return Pair.create(where, whereBindValues);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Mapper<T> getMapper(final Class<T> clazz) {
        if (Course.class.equals(clazz)) {
            return (Mapper<T>) COURSE_MAPPER;
        } else if (Permission.class.equals(clazz)) {
            return (Mapper<T>) ACCESS_MAPPER;
        } else if (Resource.class.equals(clazz)) {
            return (Mapper<T>) RESOURCE_MAPPER;
        } else if (CachedFileResource.class.equals(clazz)) {
            return (Mapper<T>) CACHED_RESOURCE_MAPPER;
        } else if (CachedUriResource.class.equals(clazz)) {
            return (Mapper<T>) CACHED_URI_RESOURCE_MAPPER;
        } else if (Progress.class.equals(clazz)) {
            return (Mapper<T>) PROGRESS_MAPPER;
        } else if (Answer.class.equals(clazz)) {
            return (Mapper<T>) ANSWER_MAPPER;
        }

        return super.getMapper(clazz);
    }

    /* Course methods, these are resource aware */

    public Course findCourse(final long courseId, final boolean loadResources) {
        final Course course = this.find(Course.class, courseId);
        if (loadResources) {
            this.loadResources(course);
        }
        return course;
    }

    public List<Course> getCourses(final String whereClause, final String[] whereBindValues, final String orderBy,
                                   final boolean loadResources) {
        final List<Course> courses = this.get(Course.class, whereClause, whereBindValues, orderBy);
        if (loadResources) {
            for (final Course course : courses) {
                this.loadResources(course);
            }
        }
        return courses;
    }

    private void loadResources(final Course course) {
        if (course != null) {
            // clear the current resources
            course.setResources(null);

            // load the resources
            final HashMap<String, List<Resource>> childResources = new HashMap<>();
            for (final Resource resource : this.get(Resource.class,
                                                    Contract.Course.Resource.COLUMN_NAME_COURSE_ID + " = ?",
                                                    new String[] {Long.toString(course.getId())})) {
                // is this resource a child of a dynamic resource
                final String parent = resource.getParentId();
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
            for (final Map.Entry<String, List<Resource>> entry : childResources.entrySet()) {
                final Resource resource = course.getResource(entry.getKey());
                if (resource != null && resource.isDynamic()) {
                    resource.setResources(entry.getValue());
                }
            }
        }
    }

    public void insertResources(final Course course) {
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
            resource.setParentId(parent != null ? parent.getId() : null);
            this.insert(resource);

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

    public void deleteResources(final Course course) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(this.getTable(Resource.class), Contract.Course.Resource.COLUMN_NAME_COURSE_ID + " = ?",
                      new String[] {Long.toString(course.getId())});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void clearProgress(final long courseId) {
        final SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(this.getTable(Progress.class), Contract.Progress.COLUMN_COURSE_ID + " = ?",
                      new String[] {Long.toString(courseId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
