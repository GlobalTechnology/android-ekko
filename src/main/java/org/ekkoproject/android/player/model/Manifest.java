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

    private String completionMessage = null;

    public long getCourseId() {
        return this.courseId;
    }

    public int getCourseVersion() {
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

    private void addContent(final CourseContent content) {
        if (content != null) {
            this.content.add(content);
        }
    }

    public String getCompletionMessage() {
        return this.completionMessage;
    }

    public static Manifest fromXml(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_MANIFEST);
        final int schemaVersion = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_SCHEMAVERSION), 1);
        switch (schemaVersion) {
        case 1:
            return new Manifest().parse_v1(parser);
        default:
            return null;
        }
    }

    /**
     * parse a manifest using schema version 1
     * 
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Manifest parse_v1(final XmlPullParser parser) throws XmlPullParserException, IOException {
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
                    this.parseMeta(parser, 1);
                    continue;
                } else if (XML.ELEMENT_CONTENT.equals(name)) {
                    this.parseContent(parser, 1);
                    continue;
                } else if (XML.ELEMENT_COMPLETION.equals(name)) {
                    this.parseCompletion(parser, 1);
                    continue;
                } else if (XML.ELEMENT_RESOURCES.equals(name)) {
                    this.setResources(Resource.parseResources(parser, this.getCourseId(), 1));
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
                    this.addContent(Lesson.fromXml(parser, schemaVersion));
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

    private void parseCompletion(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_COMPLETION);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_COMPLETION_MESSAGE.equals(name)) {
                    this.completionMessage = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_COMPLETION_MESSAGE);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }
    }
}
