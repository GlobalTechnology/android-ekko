package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Courselist
 */
public class CourseList {

	public final static int CATALOG_ALL = 1;
	
	public final static String RESOURCETYPE_IMAGE_PNG = "image/png";
	public final static String RESOURCETYPE_VIDEO_MP4 = "video/mp4";
	public final static String RESOURCETYPE_IMAGE_JPEG = "image/jpeg";

	private int catalog;
	private int start;
	private int limit;
	private boolean hasMore;
	private String moreURI;
	private boolean debug;

	private List<Course> courseList = new ArrayList<Course>();
	
	public int getCatalog() {
		return catalog;
	}

	public List<Course> getCourselist() {
		return courseList;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getMoreURI() {
		return moreURI;
	}

	public void setMoreURI(String moreURI) {
		this.moreURI = moreURI;
	}

    public static CourseList parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        return new CourseList().parseInternal(parser);
    }

    private CourseList parseInternal(final XmlPullParser parser) throws XmlPullParserException, IOException {
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
                this.courseList.add(course);
                continue;
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
