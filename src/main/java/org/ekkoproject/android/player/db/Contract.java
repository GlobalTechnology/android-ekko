package org.ekkoproject.android.player.db;

import android.provider.BaseColumns;

public final class Contract {
    private Contract() {
    }

    private static abstract class Base implements BaseColumns {
    }

    public static final class Course extends Base {
        protected static final String TABLE_NAME = "course";
        public static final String COLUMN_NAME_COURSE_ID = _ID;
        protected static final String COLUMN_NAME_VERSION = "version";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BANNER_RESOURCE = "bannerResource";
        public static final String COLUMN_NAME_MANIFEST_FILE = "manifestFile";
        public static final String COLUMN_NAME_MANIFEST_VERSION = "manifestVersion";
        public static final String COLUMN_NAME_ACCESSIBLE = "accessible";
        protected static final String COLUMN_NAME_LAST_SYNCED = "lastSynced";

        protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_VERSION, COLUMN_NAME_TITLE,
                COLUMN_NAME_BANNER_RESOURCE, COLUMN_NAME_MANIFEST_FILE, COLUMN_NAME_MANIFEST_VERSION,
                COLUMN_NAME_ACCESSIBLE, COLUMN_NAME_LAST_SYNCED };
        public static final String[] PROJECTION_UPDATE_EKKOHUB = { COLUMN_NAME_VERSION, COLUMN_NAME_TITLE,
                COLUMN_NAME_BANNER_RESOURCE, COLUMN_NAME_ACCESSIBLE, COLUMN_NAME_LAST_SYNCED };

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_NAME_COURSE_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER PRIMARY KEY," + COLUMN_NAME_VERSION + " INTEGER," + COLUMN_NAME_TITLE + " TEXT,"
                + COLUMN_NAME_BANNER_RESOURCE + " TEXT," + COLUMN_NAME_MANIFEST_FILE + " TEXT,"
                + COLUMN_NAME_MANIFEST_VERSION + " INTEGER, " + COLUMN_NAME_ACCESSIBLE + " INTEGER,"
                + COLUMN_NAME_LAST_SYNCED + " INTEGER)";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V7 updates */
        @Deprecated
        protected static final String SQL_V7_ALTER_ACCESSIBLE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN "
                + COLUMN_NAME_ACCESSIBLE + " INTEGER";
        @Deprecated
        protected static final String SQL_V7_DEFAULT_ACCESSIBLE = "UPDATE " + TABLE_NAME + " SET "
                + COLUMN_NAME_ACCESSIBLE + " = 1";

        private Course() {
        }

        protected static final class Resource extends Base {
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
            protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

            /**
             * an index on course id's is unnecessary because the primary key
             * covers this index
             */
            @Deprecated
            protected static final String SQL_DROP_INDEX_COURSE_ID = "DROP INDEX IF EXISTS " + TABLE_NAME + "_"
                    + COLUMN_NAME_COURSE_ID;
        }
    }

    public static final class CachedResource extends Base {
        protected static final String TABLE_NAME = "cachedResources";
        public static final String COLUMN_NAME_COURSE_ID = "courseId";
        public static final String COLUMN_NAME_SHA1 = "sha1";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_LAST_ACCESSED = "lastAccessed";

        protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_SHA1, COLUMN_NAME_SIZE,
                COLUMN_NAME_PATH, COLUMN_NAME_LAST_ACCESSED };

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_NAME_COURSE_ID + " = ? AND " + COLUMN_NAME_SHA1 + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER," + COLUMN_NAME_SHA1 + " TEXT," + COLUMN_NAME_SIZE + " INTEGER," + COLUMN_NAME_PATH
                + " TEXT," + COLUMN_NAME_LAST_ACCESSED + " INTEGER, PRIMARY KEY(" + COLUMN_NAME_COURSE_ID + ", "
                + COLUMN_NAME_SHA1 + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * an index on course id's is unnecessary because the primary key covers
         * this index
         */
        @Deprecated
        protected static final String SQL_DROP_INDEX_COURSE_ID = "DROP INDEX IF EXISTS " + TABLE_NAME + "_"
                + COLUMN_NAME_COURSE_ID;
    }

    public static final class CachedUriResource extends Base {
        protected static final String TABLE_NAME = "cachedUriResources";
        public static final String COLUMN_NAME_COURSE_ID = "courseId";
        public static final String COLUMN_NAME_URI = "uri";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_EXPIRES = "expires";
        public static final String COLUMN_NAME_LAST_MODIFIED = "lastModified";
        public static final String COLUMN_NAME_LAST_ACCESSED = "lastAccessed";

        protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_URI, COLUMN_NAME_SIZE,
                COLUMN_NAME_PATH, COLUMN_NAME_EXPIRES, COLUMN_NAME_LAST_MODIFIED, COLUMN_NAME_LAST_ACCESSED };

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_NAME_COURSE_ID + " = ? AND " + COLUMN_NAME_URI + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER," + COLUMN_NAME_URI + " TEXT," + COLUMN_NAME_SIZE + " INTEGER," + COLUMN_NAME_PATH
                + " TEXT," + COLUMN_NAME_EXPIRES + " INTEGER," + COLUMN_NAME_LAST_MODIFIED + " INTEGER,"
                + COLUMN_NAME_LAST_ACCESSED + " INTEGER, PRIMARY KEY(" + COLUMN_NAME_COURSE_ID + ", " + COLUMN_NAME_URI
                + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * an index on course id's in unnecessary because the primary key covers
         * this index
         */
        @Deprecated
        protected static final String SQL_DROP_INDEX_COURSE_ID = "DROP INDEX IF EXISTS " + TABLE_NAME + "_"
                + COLUMN_NAME_COURSE_ID;
    }

    public static final class Progress extends Base {
        protected static final String TABLE_NAME = "progress";
        public static final String COLUMN_NAME_COURSE_ID = "courseId";
        public static final String COLUMN_NAME_CONTENT_ID = "contentId";
        public static final String COLUMN_NAME_COMPLETED = "completed";

        protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_CONTENT_ID,
                COLUMN_NAME_COMPLETED };

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_NAME_COURSE_ID + " = ? AND " + COLUMN_NAME_CONTENT_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER," + COLUMN_NAME_CONTENT_ID + " TEXT," + COLUMN_NAME_COMPLETED + " INTEGER, PRIMARY KEY("
                + COLUMN_NAME_COURSE_ID + ", " + COLUMN_NAME_CONTENT_ID + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * an index on course id's in unnecessary because the primary key covers
         * this index
         */
        @Deprecated
        protected static final String SQL_DROP_INDEX_COURSE_ID = "DROP INDEX IF EXISTS " + TABLE_NAME + "_"
                + COLUMN_NAME_COURSE_ID;
    }

    public static final class Answer extends Base {
        protected static final String TABLE_NAME = "quizMultipleChoiceAnswers";
        public static final String COLUMN_NAME_COURSE_ID = "courseId";
        public static final String COLUMN_NAME_QUESTION_ID = "questionId";
        public static final String COLUMN_NAME_ANSWER_ID = "answerId";
        public static final String COLUMN_NAME_ANSWERED = "answered";

        protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_QUESTION_ID,
                COLUMN_NAME_ANSWER_ID, COLUMN_NAME_ANSWERED };

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_NAME_COURSE_ID + " = ? AND " + COLUMN_NAME_QUESTION_ID + " = ? AND " + COLUMN_NAME_ANSWER_ID +
                        " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_NAME_COURSE_ID
                + " INTEGER," + COLUMN_NAME_QUESTION_ID + " TEXT," + COLUMN_NAME_ANSWER_ID + " TEXT,"
                + COLUMN_NAME_ANSWERED + " INTEGER, PRIMARY KEY(" + COLUMN_NAME_COURSE_ID + ", "
                + COLUMN_NAME_QUESTION_ID + ", " + COLUMN_NAME_ANSWER_ID + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
