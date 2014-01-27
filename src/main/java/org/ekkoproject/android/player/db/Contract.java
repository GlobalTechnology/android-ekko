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

        /* V8 versions */
        @Deprecated
        protected static final String SQL_V8_CREATE_TABLE = "CREATE TABLE permission (guid TEXT, _id TEXT," +
                "admin INTEGER, enrolled INTEGER, pending INTEGER, contentVisible INTEGER, PRIMARY KEY(guid, _id))";

        /* V11 updates */
        @Deprecated
        protected static final String SQL_V11_ALTER_HIDDEN = "ALTER TABLE permission ADD COLUMN hidden INTEGER";
        @Deprecated
        protected static final String SQL_V11_DEFAULT_HIDDEN = "UPDATE permission SET hidden = 0";
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
            protected static final String COLUMN_VIDEO_ID = "videoId";
            protected static final String COLUMN_REF_ID = "refId";

            protected static final String[] PROJECTION_ALL = { COLUMN_NAME_COURSE_ID, COLUMN_NAME_RESOURCE_ID,
                    COLUMN_NAME_PARENT_RESOURCE, COLUMN_NAME_TYPE, COLUMN_NAME_SHA1, COLUMN_NAME_SIZE,
                    COLUMN_NAME_PROVIDER, COLUMN_NAME_URI, COLUMN_NAME_MIMETYPE, COLUMN_VIDEO_ID, COLUMN_REF_ID};

            private static final String SQL_COLUMN_COURSE_ID = COLUMN_NAME_COURSE_ID + " INTEGER";
            private static final String SQL_COLUMN_RESOURCE_ID = COLUMN_NAME_RESOURCE_ID + " TEXT";
            private static final String SQL_COLUMN_PARENT_RESOURCE = COLUMN_NAME_PARENT_RESOURCE + " TEXT";
            private static final String SQL_COLUMN_TYPE = COLUMN_NAME_TYPE + " TEXT";
            private static final String SQL_COLUMN_SHA1 = COLUMN_NAME_SHA1 + " TEXT";
            private static final String SQL_COLUMN_SIZE = COLUMN_NAME_SIZE + " INTEGER";
            private static final String SQL_COLUMN_PROVIDER = COLUMN_NAME_PROVIDER + " TEXT";
            private static final String SQL_COLUMN_URI = COLUMN_NAME_URI + " TEXT";
            private static final String SQL_COLUMN_MIMETYPE = COLUMN_NAME_MIMETYPE + " TEXT";
            private static final String SQL_COLUMN_VIDEO_ID = COLUMN_VIDEO_ID + " INTEGER";
            private static final String SQL_COLUMN_REF_ID = COLUMN_REF_ID + " TEXT";

            protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                    .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_RESOURCE_ID, SQL_COLUMN_PARENT_RESOURCE,
                          SQL_COLUMN_TYPE, SQL_COLUMN_SHA1, SQL_COLUMN_SIZE, SQL_COLUMN_PROVIDER, SQL_COLUMN_URI,
                          SQL_COLUMN_MIMETYPE, SQL_COLUMN_VIDEO_ID, SQL_COLUMN_REF_ID) + ")";
            protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

            /* V14 updates */
            @Deprecated
            protected static final String SQL_V14_ALTER_VIDEO_ID =
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_VIDEO_ID;

            /* V16 updates */
            @Deprecated
            static final String SQL_V16_ALTER_REF_ID =
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + SQL_COLUMN_REF_ID;
        }
    }

    protected static class CachedResource extends Base {
        protected static final String COLUMN_COURSE_ID = "courseId";
        protected static final String COLUMN_PATH = "path";
        protected static final String COLUMN_SIZE = "size";
        protected static final String COLUMN_LAST_ACCESSED = "lastAccessed";

        protected static final String SQL_COLUMN_COURSE_ID = COLUMN_COURSE_ID + " INTEGER";
        protected static final String SQL_COLUMN_PATH = COLUMN_PATH + " TEXT";
        protected static final String SQL_COLUMN_SIZE = COLUMN_SIZE + " INTEGER";
        protected static final String SQL_COLUMN_LAST_ACCESSED = COLUMN_LAST_ACCESSED + " INTEGER";
    }

    protected static final class CachedFileResource extends CachedResource {
        protected static final String TABLE_NAME = "cachedResources";
        protected static final String COLUMN_SHA1 = "sha1";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_SHA1, COLUMN_SIZE, COLUMN_PATH, COLUMN_LAST_ACCESSED};

        private static final String SQL_COLUMN_SHA1 = COLUMN_SHA1 + " TEXT";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_COURSE_ID + "," + COLUMN_SHA1 + ")";

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_COURSE_ID + " = ? AND " + COLUMN_SHA1 + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_SHA1, SQL_COLUMN_PATH, SQL_COLUMN_SIZE,
                      SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    protected static final class CachedEcvResource extends CachedResource {
        static final String TABLE_NAME = "cachedEcvResources";
        static final String COLUMN_VIDEO_ID = "videoId";
        static final String COLUMN_THUMBNAIL = "thumb";

        static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_VIDEO_ID, COLUMN_THUMBNAIL, COLUMN_PATH, COLUMN_SIZE, COLUMN_LAST_ACCESSED};

        private static final String SQL_COLUMN_VIDEO_ID = COLUMN_VIDEO_ID + " INTEGER";
        private static final String SQL_COLUMN_THUMBNAIL = COLUMN_THUMBNAIL + " INTEGER";
        private static final String SQL_PRIMARY_KEY =
                "PRIMARY KEY(" + COLUMN_COURSE_ID + "," + COLUMN_VIDEO_ID + "," + COLUMN_THUMBNAIL + ")";

        static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_COURSE_ID + " = ? AND " + COLUMN_VIDEO_ID + " = ? AND " + COLUMN_THUMBNAIL + " = ?";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_VIDEO_ID, SQL_COLUMN_THUMBNAIL, SQL_COLUMN_PATH,
                      SQL_COLUMN_SIZE, SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V15 updates */
        @Deprecated
        static final String SQL_V15_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_VIDEO_ID, SQL_COLUMN_THUMBNAIL, SQL_COLUMN_PATH,
                      SQL_COLUMN_SIZE, SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
    }

    protected static final class CachedArclightResource extends CachedResource {
        static final String TABLE_NAME = "cachedArclightResources";
        static final String COLUMN_REF_ID = "refId";
        static final String COLUMN_THUMBNAIL = "thumb";

        static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_REF_ID, COLUMN_THUMBNAIL, COLUMN_PATH, COLUMN_SIZE, COLUMN_LAST_ACCESSED};

        private static final String SQL_COLUMN_REF_ID = COLUMN_REF_ID + " TEXT";
        private static final String SQL_COLUMN_THUMBNAIL = COLUMN_THUMBNAIL + " INTEGER";
        private static final String SQL_PRIMARY_KEY =
                "PRIMARY KEY(" + COLUMN_COURSE_ID + "," + COLUMN_REF_ID + "," + COLUMN_THUMBNAIL + ")";

        static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_COURSE_ID + " = ? AND " + COLUMN_REF_ID + " = ? AND " + COLUMN_THUMBNAIL + " = ?";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_REF_ID, SQL_COLUMN_THUMBNAIL, SQL_COLUMN_PATH,
                      SQL_COLUMN_SIZE, SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
        static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V17 updates */
        @Deprecated
        static final String SQL_V17_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, COLUMN_REF_ID, SQL_COLUMN_THUMBNAIL, SQL_COLUMN_PATH,
                      SQL_COLUMN_SIZE, SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
    }

    protected static final class CachedUriResource extends CachedResource {
        protected static final String TABLE_NAME = "cachedUriResources";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_EXPIRES = "expires";
        public static final String COLUMN_LAST_MODIFIED = "lastModified";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_COURSE_ID, COLUMN_URI, COLUMN_SIZE, COLUMN_PATH, COLUMN_EXPIRES, COLUMN_LAST_MODIFIED,
                        COLUMN_LAST_ACCESSED};

        private static final String SQL_COLUMN_URI = COLUMN_URI + " TEXT";
        private static final String SQL_COLUMN_EXPIRES = COLUMN_EXPIRES + " INTEGER";
        private static final String SQL_COLUMN_LAST_MODIFIED = COLUMN_LAST_MODIFIED + " INTEGER";
        private static final String SQL_PRIMARY_KEY = "PRIMARY KEY(" + COLUMN_COURSE_ID + "," + COLUMN_URI + ")";

        protected static final String SQL_WHERE_PRIMARY_KEY = COLUMN_COURSE_ID + " = ? AND " + COLUMN_URI + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_COURSE_ID, SQL_COLUMN_URI, SQL_COLUMN_PATH, SQL_COLUMN_SIZE, SQL_COLUMN_EXPIRES,
                      SQL_COLUMN_LAST_MODIFIED, SQL_COLUMN_LAST_ACCESSED, SQL_PRIMARY_KEY) + ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class Progress extends Base {
        protected static final String TABLE_NAME = "progress";
        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_COURSE_ID = "courseId";
        public static final String COLUMN_CONTENT_ID = "contentId";
        public static final String COLUMN_COMPLETED = "completed";

        protected static final String[] PROJECTION_ALL =
                {COLUMN_GUID, COLUMN_COURSE_ID, COLUMN_CONTENT_ID, COLUMN_COMPLETED};

        private static final String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT";
        private static final String SQL_COLUMN_COURSE_ID = COLUMN_COURSE_ID + " INTEGER";
        private static final String SQL_COLUMN_CONTENT_ID = COLUMN_CONTENT_ID + " TEXT";
        private static final String SQL_COLUMN_COMPLETED = COLUMN_COMPLETED + " INTEGER";
        private static final String SQL_PRIMARY_KEY =
                "PRIMARY KEY(" + COLUMN_GUID + "," + COLUMN_COURSE_ID + "," + COLUMN_CONTENT_ID + ")";

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_GUID + " = ? AND " + COLUMN_COURSE_ID + " = ? AND " + COLUMN_CONTENT_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_GUID, SQL_COLUMN_COURSE_ID, SQL_COLUMN_CONTENT_ID, SQL_COLUMN_COMPLETED,
                      SQL_PRIMARY_KEY) + ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /* V12 updates */
        @Deprecated
        private static final String V12_TABLE_NAME = "progress_v12";
        @Deprecated
        protected static final String SQL_V12_RENAME_TABLE = "ALTER TABLE progress RENAME TO " + V12_TABLE_NAME;
        @Deprecated
        protected static final String SQL_V12_CREATE_TABLE = "CREATE TABLE progress (guid TEXT, courseId INTEGER," +
                "contentId TEXT, completed INTEGER, PRIMARY KEY(guid, courseId, contentId))";
        @Deprecated
        private static final String V12_FIELDS = "courseId, contentId, completed";
        @Deprecated
        protected static final String SQL_V12_MIGRATE_DATA =
                "INSERT INTO progress (guid," + V12_FIELDS + ") SELECT ?, " + V12_FIELDS + " FROM " + V12_TABLE_NAME;
        @Deprecated
        protected static final String SQL_V12_DELETE_TABLE = "DROP TABLE IF EXISTS " + V12_TABLE_NAME;
    }

    public static final class Answer extends Base {
        protected static final String TABLE_NAME = "quizMultipleChoiceAnswers";
        public static final String COLUMN_GUID = "guid";
        public static final String COLUMN_COURSE_ID = "courseId";
        public static final String COLUMN_QUESTION_ID = "questionId";
        public static final String COLUMN_ANSWER_ID = "answerId";
        protected static final String COLUMN_ANSWERED = "answered";

        protected static final String[] PROJECTION_ALL = {COLUMN_GUID, COLUMN_COURSE_ID, COLUMN_QUESTION_ID,
                COLUMN_ANSWER_ID, COLUMN_ANSWERED};

        private static final String SQL_COLUMN_GUID = COLUMN_GUID + " TEXT";
        private static final String SQL_COLUMN_COURSE_ID = COLUMN_COURSE_ID + " INTEGER";
        private static final String SQL_COLUMN_QUESTION_ID = COLUMN_QUESTION_ID + " TEXT";
        private static final String SQL_COLUMN_ANSWER_ID = COLUMN_ANSWER_ID + " TEXT";
        private static final String SQL_COLUMN_ANSWERED = COLUMN_ANSWERED + " INTEGER";
        private static final String SQL_PRIMARY_KEY =
                "PRIMARY KEY(" + COLUMN_GUID + "," + COLUMN_COURSE_ID + ", " + COLUMN_QUESTION_ID + "," +
                        COLUMN_ANSWER_ID + ")";

        protected static final String SQL_WHERE_PRIMARY_KEY =
                COLUMN_GUID + " = ? AND " + COLUMN_COURSE_ID + " = ? AND " + COLUMN_QUESTION_ID + " = ? AND " +
                        COLUMN_ANSWER_ID + " = ?";

        protected static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + StringUtils
                .join(",", SQL_COLUMN_GUID, SQL_COLUMN_COURSE_ID, SQL_COLUMN_QUESTION_ID, SQL_COLUMN_ANSWER_ID,
                      SQL_COLUMN_ANSWERED, SQL_PRIMARY_KEY) + ")";
        protected static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        @Deprecated
        private static final String V13_TABLE_NAME = "answers_v13";
        @Deprecated
        protected static final String SQL_V13_RENAME_TABLE =
                "ALTER TABLE quizMultipleChoiceAnswers RENAME TO " + V13_TABLE_NAME;
        @Deprecated
        protected static final String SQL_V13_CREATE_TABLE = "CREATE TABLE quizMultipleChoiceAnswers (guid TEXT," +
                "courseId INTEGER, questionId TEXT, answerId TEXT, answered INTEGER, PRIMARY KEY(guid, courseId," +
                "questionId, answerId))";
        @Deprecated
        private static final String V13_FIELDS = "courseId,questionId,answerId,answered";
        @Deprecated
        protected static final String SQL_V13_MIGRATE_DATA =
                "INSERT INTO quizMultipleChoiceAnswers (guid, " + V13_FIELDS + ") SELECT ?, " + V13_FIELDS + " FROM " +
                        V13_TABLE_NAME;
        @Deprecated
        protected static final String SQL_V13_DELETE_TABLE = "DROP TABLE IF EXISTS " + V13_TABLE_NAME;
    }
}
