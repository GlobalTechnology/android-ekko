package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.appdev.app.AppContext;
import org.appdev.utils.StringUtils;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Course extends Entity {
	
	public static final String ID = "_id";
	public static final String NAME = "course_name";
	public static final String VISITED = "visited";

	public static final String DIR_NAME ="dir_name";
	public static final String COURSE_BANNER = "course_banner";
	public static final String TABLE_NAME = "courses";  //for now, we didn't use the sqlite to manage data, for simplicity we just serialize the entity
	
    private final long id;
    private int version = 0;

	private String course_title;
	private String course_banner;
	private String course_description;
	private String course_copyright;
	private String course_uri;
	private String course_zipuri;
	
	private String author_name;
	private String author_email;
	private String author_url;

	private String visited;
	private String dir_name;
	private int lessonIndex=0;
	private int lessonProgressIndex = 0;
	private int progress = 0;
	
	private String course_guid;
	private HashMap<String, Resource> resourceMap;
	
	private ArrayList<Lesson> lessonList;
	
    public Course(final long id) {
        this.id = id;
    }

    public void addLesson(Lesson lesson) {
		this.lessonList.add(lesson);
	}
	
	public ArrayList<Lesson> getLessonList(){
		return this.lessonList;
	}
	
	public void setLessonList(ArrayList<Lesson> lessonList) {
		this.lessonList = lessonList;
	}

    public long getId() {
        return this.id;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

	public Resource getResource(String resourceId) {
		return resourceMap.get(resourceId);
	}
	public String getVisited(){
		return visited;
	}
	
	public void setVisited(String visited){
		this.visited = visited;
	}	


	public void setDirName(String dir_name) {
		// TODO Auto-generated method stub
		this.dir_name = dir_name;		
	}
	
	public String getDirName() {
		return dir_name;
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

	public void setCourseDescription(String course_description) {
		this.course_description = course_description;
	}

	public String getCourseCopyright() {
		return course_copyright;
	}

	public void setCourseCopyright(String course_copyright) {
		this.course_copyright = course_copyright;
	}

	public String getAuthorName() {
		return author_name;
	}

	public void setAuthorName(String author_name) {
		this.author_name = author_name;
	}

	public String getAuthorEmail() {
		return author_email;
	}

	public void setAuthorEmail(String author_email) {
		this.author_email = author_email;
	}

	public String getAuthorUrl() {
		return author_url;
	}

	public void setAuthorUrl(String author_url) {
		this.author_url = author_url;
	}

	public String getCourseGUID() {
		return course_guid;
	}

	public void setCourseGUID(String course_guid) {
		this.course_guid = course_guid;
	}

	public HashMap<String, Resource> getResourceMap() {
		return resourceMap;
	}

	public void setResourceMap(HashMap<String, Resource> resourceMap) {
		this.resourceMap = resourceMap;
	}

	public int getLessonIndex() {
		return lessonIndex;
	}

	public void setLessonIndex(int lessonIndex) {
		this.lessonIndex = lessonIndex;
	}

	public String getCourseURI() {
		return course_uri;
	}

	public void setCourseURI(String course_uri) {
		this.course_uri = course_uri;
	}

	public String getCourseZipUri() {
		return course_zipuri;
	}

	public void setCourseZipUri(String course_zipuri) {
		this.course_zipuri = course_zipuri;
	}

	public int getLessonProgressIndex() {
		return lessonProgressIndex;
	}

	public void setLessonProgressIndex(int lessonProgressIndex) {
		this.lessonProgressIndex = lessonProgressIndex;
	}
	
	public int getProgress(){
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}
	
    public static Course parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSE);
        final int schemaVersion = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_SCHEMAVERSION), 1);
        final long courseId = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_COURSE_ID), -1);
        return new Course(courseId).parseInternal(parser, schemaVersion);
    }

    // XXX: maybe this should be handled with a separate object
    public static Course parseManifest(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_MANIFEST);
        final int schemaVersion = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_SCHEMAVERSION), 1);
        final long courseId = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_COURSE_ID), -1);
        return new Course(courseId).parseManifestInternal(parser, schemaVersion);
    }

    private Course parseInternal(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSE);

        this.course_guid = parser.getAttributeValue(null, XML.ATTR_COURSE_ID);
        this.version = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSE_VERSION), 0);
        this.course_uri = parser.getAttributeValue(null, XML.ATTR_COURSE_URI);
        this.course_zipuri = parser.getAttributeValue(null, XML.ATTR_COURSE_ZIPURI);

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
                } else if (XML.ELEMENT_RESOURCES.equals(name)) {
                    this.resourceMap = new HashMap<String, Resource>();
                    for (final Resource resource : Resource.parseResources(parser, schemaVersion)) {
                        this.resourceMap.put(resource.getId(), resource);
                    }
                    continue;
                }

            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }
        return this;
    }

    private Course parseManifestInternal(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_MANIFEST);

        this.course_guid = parser.getAttributeValue(null, XML.ATTR_COURSE_ID);
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
                    this.resourceMap = new HashMap<String, Resource>();
                    for (final Resource resource : Resource.parseResources(parser, schemaVersion)) {
                        this.resourceMap.put(resource.getId(), resource);
                    }
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    private Course parseMeta(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
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

    private Course parseMetaAuthor(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
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

    private Course parseContent(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT);

        this.lessonList = new ArrayList<Lesson>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_CONTENT_LESSON.equals(name)) {
                    this.lessonList.add(Lesson.parse(parser, schemaVersion));
                    continue;
                } else if (XML.ELEMENT_CONTENT_QUIZ.equals(name)) {
                    // TODO
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }


}
