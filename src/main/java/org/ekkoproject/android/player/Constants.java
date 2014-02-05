package org.ekkoproject.android.player;

import com.thinkfree.showlicense.License;
import com.thinkfree.showlicense.LicensedProject;

public final class Constants {
    public final static long THEKEY_CLIENTID = 85613451684391165L;

    public final static long INVALID_ID = Long.MIN_VALUE;
    public final static long INVALID_COURSE = -1;
    public final static int DEFAULT_LAYOUT = 0;

    public final static String GUID_GUEST = "GUEST";

    /** common arguments */
    public static final String ARG_LAYOUT = Constants.class.getPackage().getName() + ".ARG_LAYOUT";

    /** common intent extras */
    public static final String EXTRA_COURSEID = Constants.class.getPackage().getName() + ".EXTRA_COURSEID";
    public static final String EXTRA_GUID = Constants.class.getPackage().getName() + ".EXTRA_GUID";

    /** common saved state data */
    public static final String STATE_GUID =  Constants.class.getPackage().getName() + ".STATE_GUID";

    public static final LicensedProject[] LICENSED_PROJECTS = new LicensedProject[] {
            new LicensedProject("Android Support Library", null,
                                "http://developer.android.com/tools/support-library/index.html", License.APACHE2),
            new LicensedProject("Guava", null, "https://code.google.com/p/guava-libraries/", License.APACHE2),
            new LicensedProject("showlicense", null, "https://github.com/behumble/showlicense", License.APACHE2),
            new LicensedProject("SLF4J", null, "http://www.slf4j.org/", License.MIT),
            new LicensedProject("ViewPagerIndicator", null, "http://viewpagerindicator.com/", License.APACHE2),
            // This is a best guess about the license, I couldn't find a license for the library, just for code samples
            new LicensedProject("YouTube Android Player API", null,
                                "https://developers.google.com/youtube/android/player/", License.APACHE2),
    };

    /** XML constants */
    public final static class XML {
        public static final String NS_EKKO = "https://ekkoproject.org/manifest";
        public static final String NS_HUB = "https://ekkoproject.org/hub";

        public static final String ELEMENT_COURSES = "courses";
        public static final String ELEMENT_COURSE = "course";
        public static final String ELEMENT_PERMISSION = "permission";
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

        /* complete elements */
        public static final String ELEMENT_COMPLETION = "complete";
        public static final String ELEMENT_COMPLETION_MESSAGE = "message";

        /** access attributes */
        public static final String ATTR_PERMISSION_GUID = "guid";
        public static final String ATTR_PERMISSION_ADMIN = "admin";
        public static final String ATTR_PERMISSION_ENROLLED = "enrolled";
        public static final String ATTR_PERMISSION_PENDING = "pending";
        public static final String ATTR_PERMISSION_CONTENT_VISIBLE = "contentVisible";

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
        public static final String ATTR_COURSE_ENROLLMENT_TYPE = "enrollmentType";
        public static final String ATTR_COURSE_PUBLIC = "public";

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
        public static final String ATTR_TEXT_ID = "id";

        /** resource attributes */
        public static final String ATTR_RESOURCE_ID = "id";
        public static final String ATTR_RESOURCE_TYPE = "type";
        public static final String ATTR_RESOURCE_SHA1 = "sha1";
        public static final String ATTR_RESOURCE_SIZE = "size";
        public static final String ATTR_RESOURCE_FILE = "file";
        public static final String ATTR_RESOURCE_MIMETYPE = "mimeType";
        public static final String ATTR_RESOURCE_URI = "uri";
        public static final String ATTR_RESOURCE_PROVIDER = "provider";
        public static final String ATTR_RESOURCE_VIDEO_ID = "videoId";
        public static final String ATTR_RESOURCE_REF_ID = "refId";
    }
}
