package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@Deprecated
public abstract class Course {
	private String course_title;
	private String course_banner;
	private String course_description;
	private String course_copyright;
	
	private String author_name;
	private String author_email;
	private String author_url;

    private final HashMap<String, Resource> resources = new HashMap<String, Resource>();

	public Resource getResource(String resourceId) {
		return resources.get(resourceId);
	}

	public String getCourseTitle() {
		return course_title;
	}

	public void setCourseTitle(String course_title) {
		this.course_title = course_title;
	}

	public String getCourseBanner() {
		return course_banner;
	}

	public void setCourseBanner(String course_banner) {
		this.course_banner = course_banner;
	}

	public String getCourseDescription() {
		return course_description;
	}

	public String getCourseCopyright() {
		return course_copyright;
	}

	public String getAuthorName() {
		return author_name;
	}

	public String getAuthorEmail() {
		return author_email;
	}

	public String getAuthorUrl() {
		return author_url;
	}

    public Collection<Resource> getResources() {
        return Collections.unmodifiableCollection(this.resources.values());
    }

    public void setResources(final Collection<Resource> resources) {
        this.resources.clear();
        this.addResources(resources);
    }

    public void addResource(final Resource resource) {
        if (resource != null) {
            this.resources.put(resource.getId(), resource);
        }
    }

    public void addResources(final Collection<Resource> resources) {
        if (resources != null) {
            for (final Resource resource : resources) {
                this.addResource(resource);
            }
        }
    }

    protected Course parseMeta(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_META);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_META_AUTHOR.equals(name)) {
                    this.parseMetaAuthor(parser, schemaVersion);
                    continue;
                } else if (XML.ELEMENT_META_TITLE.equals(name)) {
                    this.course_title = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_TITLE);
                    continue;
                } else if (XML.ELEMENT_META_BANNER.equals(name)) {
                    this.course_banner = parser.getAttributeValue(null, XML.ATTR_RESOURCE);
                    ParserUtils.skip(parser);
                    continue;
                } else if (XML.ELEMENT_META_DESCRIPTION.equals(name)) {
                    this.course_description = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_DESCRIPTION);
                    continue;
                } else if (XML.ELEMENT_META_COPYRIGHT.equals(name)) {
                    this.course_copyright = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_COPYRIGHT);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    protected Course parseMetaAuthor(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_META_AUTHOR);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_META_AUTHOR_NAME.equals(name)) {
                    this.author_name = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_AUTHOR_NAME);
                    continue;
                } else if (XML.ELEMENT_META_AUTHOR_EMAIL.equals(name)) {
                    this.author_email = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_AUTHOR_EMAIL);
                    continue;
                } else if (XML.ELEMENT_META_AUTHOR_URL.equals(name)) {
                    this.author_url = parser.nextText();
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_META_AUTHOR_URL);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
