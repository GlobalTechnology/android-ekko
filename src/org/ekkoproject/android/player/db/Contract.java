package org.ekkoproject.android.player.db;

import android.provider.BaseColumns;

public final class Contract {
    private Contract() {
    }

    private static abstract class Base implements BaseColumns {
    }

    public static final class Course extends Base {
        public static final String TABLE_NAME = "course";
        public static final String COLUMN_NAME_COURSE_ID = _ID;
        public static final String COLUMN_NAME_VERSION = "version";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BANNER_RESOURCE = "bannerResource";
        public static final String COLUMN_NAME_LAST_SYNCED = "lastSynced";

        public static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_VERSION, COLUMN_NAME_TITLE,
                COLUMN_NAME_BANNER_RESOURCE, COLUMN_NAME_LAST_SYNCED };

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER PRIMARY KEY," + COLUMN_NAME_VERSION + " INTEGER," + COLUMN_NAME_TITLE + " TEXT,"
                + COLUMN_NAME_BANNER_RESOURCE + " TEXT," + COLUMN_NAME_LAST_SYNCED + " INTEGER)";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private Course() {
        }

        public static final class Resource extends Base {
            protected static final String TABLE_NAME = "courseResources";
            protected static final String COLUMN_NAME_COURSE_ID = "courseId";
            protected static final String COLUMN_NAME_RESOURCE_ID = "resourceId";
            protected static final String COLUMN_NAME_PARENT_RESOURCE = "parentId";
            protected static final String COLUMN_NAME_TYPE = "type";
            protected static final String COLUMN_NAME_SHA1 = "sha1";
            protected static final String COLUMN_NAME_SIZE = "size";
            protected static final String COLUMN_NAME_PROVIDER = "provider";
            protected static final String COLUMN_NAME_URI = "uri";
            protected static final String COLUMN_NAME_MIMETYPE = "mimeType";

            protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_RESOURCE_ID,
                    COLUMN_NAME_PARENT_RESOURCE, COLUMN_NAME_TYPE, COLUMN_NAME_SHA1, COLUMN_NAME_SIZE,
                    COLUMN_NAME_PROVIDER, COLUMN_NAME_URI, COLUMN_NAME_MIMETYPE };

            protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                    + COLUMN_NAME_COURSE_ID + " INTEGER," + COLUMN_NAME_RESOURCE_ID + " TEXT,"
                    + COLUMN_NAME_PARENT_RESOURCE + " TEXT," + COLUMN_NAME_TYPE + " TEXT," + COLUMN_NAME_SHA1
                    + " TEXT," + COLUMN_NAME_SIZE + " INTEGER," + COLUMN_NAME_PROVIDER + " TEXT," + COLUMN_NAME_URI
                    + " TEXT," + COLUMN_NAME_MIMETYPE + " TEXT)";
            protected static final String SQL_INDEX_COURSE_ID = "CREATE INDEX " + TABLE_NAME + "_"
                    + COLUMN_NAME_COURSE_ID + " ON " + TABLE_NAME + "(" + COLUMN_NAME_COURSE_ID + ")";
            protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        }
    }
}
