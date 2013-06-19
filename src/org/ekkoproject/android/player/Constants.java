package org.ekkoproject.android.player;

import android.net.Uri;

public final class Constants {
    public final static long THEKEY_CLIENTID = 85613451684391165L;
    public final static Uri EKKOHUB_URI = Uri.parse("https://servicesdev.gcx.org/ekko/");

    public final static long INVALID_ID = Long.MIN_VALUE;
    public final static long INVALID_COURSE = -1;
    public final static int DEFAULT_LAYOUT = 0;

    /** common intent extras */
    public static final String EXTRA_COURSEID = Constants.class.getPackage().getName() + ".EXTRA_COURSEID";

    /** XML constants */
    public final static class XML {
        public static final String NS_EKKO = "https://ekkoproject.org/manifest";
        public static final String NS_HUB = "https://ekkoproject.org/hub";

        public static final String ELEMENT_COURSES = "courses";
        public static final String ELEMENT_COURSE = "course";
        public static final String ELEMENT_MANIFEST = "course";
        public static final String ELEMENT_RESOURCES = "resources";
        public static final String ELEMENT_RESOURCE = "resource";

        /** meta elements */
        public static final String ELEMENT_META = "meta";
        public static final String ELEMENT_META_TITLE = "title";
        public static final String ELEMENT_META_AUTHOR = "author";
        public static final String ELEMENT_META_AUTHOR_NAME = "name";
        public static final String ELEMENT_META_AUTHOR_EMAIL = "email";
        public static final String ELEMENT_META_AUTHOR_URL = "url";
        public static final String ELEMENT_META_BANNER = "banner";
        public static final String ELEMENT_META_DESCRIPTION = "description";
        public static final String ELEMENT_META_COPYRIGHT = "copyright";

        /** content elements */
        public static final String ELEMENT_CONTENT = "content";
        public static final String ELEMENT_CONTENT_LESSON = "lesson";
        public static final String ELEMENT_CONTENT_QUIZ = "quiz";
        public static final String ELEMENT_LESSON_MEDIA = "media";
        public static final String ELEMENT_LESSON_TEXT = "text";
        public static final String ELEMENT_QUIZ_QUESTION = "question";
        public static final String ELEMENT_QUIZ_QUESTION_TEXT = "text";
        public static final String ELEMENT_QUIZ_QUESTION_OPTIONS = "options";
        public static final String ELEMENT_QUIZ_QUESTION_OPTION = "option";

        /** manifest attributes */
        public static final String ATTR_SCHEMAVERSION = "schemaVersion";

        /** generic attributes */
        public static final String ATTR_RESOURCE = "resource";
        public static final String ATTR_THUMBNAIL = "thumbnail";

        /** course list attributes */
        public static final String ATTR_COURSES_START = "start";
        public static final String ATTR_COURSES_LIMIT = "limit";
        public static final String ATTR_COURSES_HASMORE = "hasMore";

        /** course attributes */
        public static final String ATTR_COURSE_ID = "id";
        public static final String ATTR_COURSE_VERSION = "version";
        @Deprecated
        public static final String ATTR_COURSE_URI = "uri";
        @Deprecated
        public static final String ATTR_COURSE_ZIPURI = "zipUri";

        /** lesson attributes */
        public static final String ATTR_LESSON_ID = "id";
        public static final String ATTR_LESSON_TITLE = "title";

        /** quiz attributes */
        public static final String ATTR_QUIZ_ID = "id";
        public static final String ATTR_QUIZ_TITLE = "title";

        /** question attributes */
        public static final String ATTR_QUESTION_ID = "id";
        public static final String ATTR_QUESTION_TYPE = "type";

        /** option attributes */
        public static final String ATTR_OPTION_ID = "id";
        public static final String ATTR_OPTION_ANSWER = "answer";

        /** content attributes */
        public static final String ATTR_MEDIA_ID = "id";
        public static final String ATTR_MEDIA_TYPE = "type";

        /** resource attributes */
        public static final String ATTR_RESOURCE_ID = "id";
        public static final String ATTR_RESOURCE_TYPE = "type";
        public static final String ATTR_RESOURCE_SHA1 = "sha1";
        public static final String ATTR_RESOURCE_SIZE = "size";
        public static final String ATTR_RESOURCE_FILE = "file";
        public static final String ATTR_RESOURCE_MIMETYPE = "mimeType";
        public static final String ATTR_RESOURCE_URI = "uri";
        public static final String ATTR_RESOURCE_PROVIDER = "provider";
    }
}
