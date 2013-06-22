package org.ekkoproject.android.player.model;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.appdev.entity.Course;
import org.appdev.entity.Resource;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Manifest extends Course {
    private long courseId;
    private int version;

    private final List<CourseContent> content = new ArrayList<CourseContent>();

    @Deprecated
    public long getId() {
        return this.courseId;
    }

    public long getCourseId() {
        return this.courseId;
    }

    public int getVersion() {
        return this.version;
    }

    public List<CourseContent> getContent() {
        return Collections.unmodifiableList(this.content);
    }

    /**
     * find the index of the requested content, return -1 if it doesn't exist
     * 
     * @param contentId
     *            the id of the content being searched for
     * @return the index of the content item
     */
    public int findContent(final String contentId) {
        if (contentId != null) {
            int i = 0;
            for (final CourseContent content : this.content) {
                if (content.getId().equals(contentId)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    public CourseContent getContent(final String contentId) {
        if (contentId != null) {
            for (final CourseContent content : this.content) {
                if (contentId.equals(content.getId())) {
                    return content;
                }
            }
        }
        return null;
    }

    public Lesson getLesson(final String lessonId) {
        final CourseContent lesson = this.getContent(lessonId);
        if (lesson instanceof Lesson) {
            return (Lesson) lesson;
        }
        return null;
    }

    public Quiz getQuiz(final String quizId) {
        final CourseContent quiz = this.getContent(quizId);
        if (quiz instanceof Quiz) {
            return (Quiz) quiz;
        }
        return null;
    }

    protected void setContent(final List<CourseContent> content) {
        this.content.clear();
        this.addContent(content);
    }

    protected void addContent(final List<CourseContent> content) {
        if (content != null) {
            for (final CourseContent item : content) {
                this.addContent(item);
            }
        }
    }

    protected void addContent(final CourseContent content) {
        if (content != null) {
            this.content.add(content);
        }
    }

    public static Manifest fromXml(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_MANIFEST);
        final int schemaVersion = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_SCHEMAVERSION), 1);
        return new Manifest().parse(parser, schemaVersion);
    }

    private Manifest parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_MANIFEST);

        this.courseId = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_COURSE_ID), INVALID_COURSE);
        this.version = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSE_VERSION), 0);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_META.equals(name)) {
                    this.parseMeta(parser, schemaVersion);
                    continue;
                } else if (XML.ELEMENT_CONTENT.equals(name)) {
                    this.parseContent(parser, schemaVersion);
                    continue;
                } else if (XML.ELEMENT_RESOURCES.equals(name)) {
                    this.setResources(Resource.parseResources(parser, this.getCourseId(), schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    private Manifest parseContent(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_CONTENT_LESSON.equals(name)) {
                    this.addContent(Lesson.parse(parser, schemaVersion));
                    continue;
                } else if (XML.ELEMENT_CONTENT_QUIZ.equals(name)) {
                    this.addContent(Quiz.fromXml(parser, schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
