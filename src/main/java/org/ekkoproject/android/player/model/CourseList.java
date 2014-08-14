package org.ekkoproject.android.player.model;

import com.google.common.collect.ForwardingList;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseList extends ForwardingList<Course> {
    private final ArrayList<Course> mCourses = new ArrayList<>();

    private int start;
    private int limit;
    private boolean hasMore;

    @Override
    protected final List<Course> delegate() {
        return mCourses;
    }

    public int getStart() {
        return this.start;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean hasMore() {
        return this.hasMore;
    }

    public static CourseList fromXml(final XmlPullParser parser) throws XmlPullParserException, IOException {
        return new CourseList().parse(parser);
    }

    private CourseList parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSES);

        this.start = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSES_START), 0);
        this.limit = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSES_LIMIT), 10);
        this.hasMore = StringUtils.toBool(parser.getAttributeValue(null, XML.ATTR_COURSES_HASMORE), false);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_HUB.equals(ns) && XML.ELEMENT_COURSE.equals(name)) {
                final Course course = Course.fromXml(parser);
                if (course != null) {
                    mCourses.add(course);
                }
                continue;
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
