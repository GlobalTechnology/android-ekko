package org.ekkoproject.android.player.db;

import android.provider.BaseColumns;

import org.ccci.gto.android.common.util.StringUtils;

public final class Contract {
    private Contract() {
    }

    private static abstract class Base implements BaseColumns {
    }

    public static final class Permission extends Base {
        protected static final String TABLE_NAME = "permission";
        public static final String COLUMN_GUID = "guid";
        protected static final String COLUMN_COURSE_ID = _ID;
        public static final String COLUMN_ADMIN = "admin";
        public static final String COLUMN_ENROLLED = "enrolled";
        public static final String COLUMN_PENDING = "pending";
        public static final String COLUMN_CONTENT_VISIBLE = "contentVisible";
        public static final String COLUMN_HIDDEN = "hidden";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_GUID, COLUMN_ADMIN, COLUMN_ENROLLED, COLUMN_PENDING, COLUMN_CONTENT_VISIBLE,
                        COLUMN_HIDDEN};

        private static final String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT";
        private static final String SQL_COLUMN_COURSE_ID = COLUMN_COURSE_ID + " TEXT";
        private static final String SQL_COLUMN_ADMIN = COLUMN_ADMIN + " INTEGER";
        private static final String SQL_COLUMN_ENROLLED = COLUMN_ENROLLED + " INTEGER";
        private static final String SQL_COLUMN_PENDING = COLUMN_PENDING + " INTEGER";
        private static final String SQL_COLUMN_CONTENT_VISIBLE = COLUMN_CONTENT_VISIBLE + " INTEGER";
        private static final String SQL_COLUMN_HIDDEN = COLUMN_HIDDEN + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_GUID + "," + COLUMN_COURSE_ID + ")";

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        protected static final String SQL_JOIN_COURSE =
                SQL_PREFIX + COLUMN_COURSE_ID + " = " + Course.SQL_PREFIX + Course.COLUMN_NAME_COURSE_ID;

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_GUID + " = ? AND " + COLUMN_COURSE_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_GUID, SQL_COLUMN_COURSE_ID, SQL_COLUMN_ADMIN, SQL_COLUMN_ENROLLED,
                      SQL_COLUMN_PENDING, SQL_COLUMN_CONTENT_VISIBLE, SQL_COLUMN_HIDDEN, SQL_PRIMARY_KEY) + ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V11 updates */
        @Deprecated
        protected static final String SQL_V11_ALTER_HIDDEN =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_HIDDEN;
        @Deprecated
        protected static final String SQL_V11_DEFAULT_HIDDEN =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_HIDDEN + " = 0";
    }

    public static final class Course extends Base {
        // removed columns, we are temporarily keeping these columns because there isn't a simple way to drop columns
        @Deprecated
        private static final String COLUMN_NAME_ACCESSIBLE = "accessible";
        @Deprecated
        private static final String SQL_COLUMN_ACCESSIBLE = COLUMN_NAME_ACCESSIBLE + " INTEGER";

        protected static final String TABLE_NAME = "course";
        public static final String COLUMN_NAME_COURSE_ID = _ID;
        protected static final String COLUMN_NAME_VERSION = "version";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BANNER_RESOURCE = "bannerResource";
        public static final String COLUMN_DESCRIPTION = "description";
        protected static final String COLUMN_PUBLIC = "public";
        public static final String COLUMN_ENROLLMENT_TYPE = "enrollmentType";
        public static final String COLUMN_NAME_MANIFEST_FILE = "manifestFile";
        public static final String COLUMN_NAME_MANIFEST_VERSION = "manifestVersion";
        protected static final String COLUMN_NAME_LAST_SYNCED = "lastSynced";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_NAME_COURSE_ID, COLUMN_NAME_VERSION, COLUMN_NAME_TITLE, COLUMN_NAME_BANNER_RESOURCE,
                        COLUMN_DESCRIPTION, COLUMN_PUBLIC, COLUMN_ENROLLMENT_TYPE, COLUMN_NAME_MANIFEST_FILE,
                        COLUMN_NAME_MANIFEST_VERSION, COLUMN_NAME_LAST_SYNCED,};
        public static final String[] PROJECTION_UPDATE_EKKOHUB =
                {COLUMN_NAME_VERSION, COLUMN_NAME_TITLE, COLUMN_NAME_BANNER_RESOURCE, COLUMN_DESCRIPTION, COLUMN_PUBLIC,
                        COLUMN_ENROLLMENT_TYPE, COLUMN_NAME_LAST_SYNCED};

        public static final String SQL_PREFIX = TABLE_NAME + ".";

        private static final String SQL_COLUMN_COURSE_ID = COLUMN_NAME_COURSE_ID + " INTEGER";
        private static final String SQL_COLUMN_VERSION = COLUMN_NAME_VERSION + " INTEGER";
        private static final String SQL_COLUMN_TITLE = COLUMN_NAME_TITLE + " TEXT";
        private static final String SQL_COLUMN_BANNER_RESOURCE = COLUMN_NAME_BANNER_RESOURCE + " TEXT";
        private static final String SQL_COLUMN_DESCRIPTION = COLUMN_DESCRIPTION + " TEXT";
        private static final String SQL_COLUMN_PUBLIC = COLUMN_PUBLIC + " INTEGER";
        private static final String SQL_COLUMN_ENROLLMENT_TYPE = COLUMN_ENROLLMENT_TYPE + " INTEGER";
        private static final String SQL_COLUMN_MANIFEST_FILE = COLUMN_NAME_MANIFEST_FILE + " TEXT";
        private static final String SQL_COLUMN_MANIFEST_VERSION = COLUMN_NAME_MANIFEST_VERSION + " INTEGER";
        private static final String SQL_COLUMN_LAST_SYNCED = COLUMN_NAME_LAST_SYNCED + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_NAME_COURSE_ID + ")";

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_NAME_COURSE_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_VERSION, SQL_COLUMN_TITLE, SQL_COLUMN_BANNER_RESOURCE,
                      SQL_COLUMN_DESCRIPTION, SQL_COLUMN_PUBLIC, SQL_COLUMN_ENROLLMENT_TYPE, SQL_COLUMN_MANIFEST_FILE,
                      SQL_COLUMN_MANIFEST_VERSION, SQL_COLUMN_ACCESSIBLE, SQL_COLUMN_LAST_SYNCED, SQL_PRIMARY_KEY) +
                ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V7 updates */
        @Deprecated
        protected static final String SQL_V7_ALTER_ACCESSIBLE =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_ACCESSIBLE;
        @Deprecated
        protected static final String SQL_V7_DEFAULT_ACCESSIBLE = "UPDATE " + TABLE_NAME + " SET "
                + COLUMN_NAME_ACCESSIBLE + " = 1";

        /* V9 updates */
        @Deprecated
        protected static final String SQL_V9_ALTER_PUBLIC =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_PUBLIC;
        @Deprecated
        protected static final String SQL_V9_ALTER_ENROLLMENT_TYPE =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_ENROLLMENT_TYPE;
        @Deprecated
        protected static final String SQL_V9_DEFAULT_PUBLIC_ENROLLMENT_TYPE =
                "UPDATE " + TABLE_NAME + " SET " + COLUMN_PUBLIC + " = 0, " + COLUMN_ENROLLMENT_TYPE + " = 0";

        /* V10 updates */
        @Deprecated
        protected static final String SQL_V10_ALTER_DESCRIPTION =
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_DESCRIPTION;

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
        public static final String COLUMN_COURSE_ID = "courseId";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_EXPIRES = "expires";
        public static final String COLUMN_LAST_MODIFIED = "lastModified";
        public static final String COLUMN_LAST_ACCESSED = "lastAccessed";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_URI, COLUMN_SIZE, COLUMN_PATH, COLUMN_EXPIRES, COLUMN_LAST_MODIFIED,
                        COLUMN_LAST_ACCESSED};

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_COURSE_ID + " = ? AND " + COLUMN_URI + " = ?";

        protected static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_COURSE_ID + " INTEGER," + COLUMN_URI + " TEXT," +
                        COLUMN_SIZE + " INTEGER," + COLUMN_PATH + " TEXT," + COLUMN_EXPIRES + " INTEGER," +
                        COLUMN_LAST_MODIFIED + " INTEGER," + COLUMN_LAST_ACCESSED + " INTEGER, PRIMARY KEY(" +
                        COLUMN_COURSE_ID + ", " + COLUMN_URI + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * an index on course id's in unnecessary because the primary key covers
         * this index
         */
        @Deprecated
        protected static final String SQL_DROP_INDEX_COURSE_ID =
                "DROP INDEX IF EXISTS " + TABLE_NAME + "_" + COLUMN_COURSE_ID;
    }

    public static final class Progress extends Base {
        protected static final String TABLE_NAME = "progress";
        public static final String COLUMN_COURSE_ID = "courseId";
        public static final String COLUMN_CONTENT_ID = "contentId";
        public static final String COLUMN_COMPLETED = "completed";

        protected static final String[] PROJECTION_ALL = {COLUMN_COURSE_ID, COLUMN_CONTENT_ID, COLUMN_COMPLETED};

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_COURSE_ID + " = ? AND " + COLUMN_CONTENT_ID + " = ?";

        protected static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_COURSE_ID + " INTEGER," + COLUMN_CONTENT_ID + " TEXT," +
                        COLUMN_COMPLETED + " INTEGER, PRIMARY KEY(" + COLUMN_COURSE_ID + ", " + COLUMN_CONTENT_ID +
                        "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * an index on course id's in unnecessary because the primary key covers
         * this index
         */
        @Deprecated
        protected static final String SQL_DROP_INDEX_COURSE_ID =
                "DROP INDEX IF EXISTS " + TABLE_NAME + "_" + COLUMN_COURSE_ID;
    }

    public static final class Answer extends Base {
        protected static final String TABLE_NAME = "quizMultipleChoiceAnswers";
        public static final String COLUMN_COURSE_ID = "courseId";
        public static final String COLUMN_QUESTION_ID = "questionId";
        public static final String COLUMN_ANSWER_ID = "answerId";
        public static final String COLUMN_ANSWERED = "answered";

        protected static final String[] PROJECTION_ALL = {COLUMN_COURSE_ID, COLUMN_QUESTION_ID,
                COLUMN_ANSWER_ID, COLUMN_ANSWERED};

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_COURSE_ID + " = ? AND " + COLUMN_QUESTION_ID + " = ? AND " + COLUMN_ANSWER_ID + " = ?";

        protected static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_COURSE_ID + " INTEGER," + COLUMN_QUESTION_ID + " TEXT," +
                        COLUMN_ANSWER_ID + " TEXT," + COLUMN_ANSWERED + " INTEGER, PRIMARY KEY(" + COLUMN_COURSE_ID +
                        ", " + COLUMN_QUESTION_ID + ", " + COLUMN_ANSWER_ID + "))";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
