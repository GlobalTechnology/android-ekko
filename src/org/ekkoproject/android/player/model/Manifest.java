package org.ekkoproject.android.player.model;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.appdev.entity.Course;
import org.appdev.entity.CourseContent;
import org.appdev.entity.Lesson;
import org.appdev.entity.Quiz;
import org.appdev.entity.Resource;
import org.appdev.utils.StringUtils;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Manifest extends Course {
    private long courseId;
    private int version;

    private final List<CourseContent> content = new ArrayList<CourseContent>();

    public Manifest() {
        super(INVALID_COURSE);
    }

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

    public void setContent(final List<CourseContent> content) {
        this.content.clear();
        this.addContent(content);
    }

    public void addContent(final CourseContent content) {
        if (content != null) {
            this.content.add(content);
        }
    }

    public void addContent(final List<CourseContent> content) {
        if (content != null) {
            for (final CourseContent item : content) {
                this.addContent(item);
            }
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
                    this.setResources(Resource.parseResources(parser, schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    private Course parseContent(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
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
