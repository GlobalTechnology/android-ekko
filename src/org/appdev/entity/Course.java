package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	
    private final HashMap<String, Resource> resourceMap = new HashMap<String, Resource>();
	
    private List<CourseContent> lessonList = new ArrayList<CourseContent>();

    private Date lastSynced = new Date(0);

    public Course(final long id) {
        this.id = id;
    }

    @Deprecated
    public void addLesson(Lesson lesson) {
        this.addContent(lesson);
    }

    @Deprecated
    public List<CourseContent> getLessonList() {
        return this.getContent();
    }

    @Deprecated
    public void setLessonList(ArrayList<CourseContent> lessonList) {
        this.setContent(lessonList);
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

    @Deprecated
	public String getCourseGUID() {
        return Long.toString(this.getId());
	}

    @Deprecated
	public HashMap<String, Resource> getResourceMap() {
		return resourceMap;
	}

    @Deprecated
	public void setResourceMap(HashMap<String, Resource> resourceMap) {
        this.setResources(resourceMap != null ? resourceMap.values() : null);
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
	
    @Deprecated
    public List<CourseContent> getCourseContent() {
        return this.getContent();
    }

    public List<CourseContent> getContent() {
        return Collections.unmodifiableList(this.lessonList);
    }

    public void setContent(final List<CourseContent> content) {
        this.lessonList.clear();
        this.addContent(content);
    }

    public void addContent(final CourseContent content) {
        if (content != null) {
            this.lessonList.add(content);
        }
    }

    public void addContent(final List<CourseContent> content) {
        if (content != null) {
            for (final CourseContent item : content) {
                this.addContent(item);
            }
        }
    }

    public Collection<Resource> getResources() {
        return Collections.unmodifiableCollection(this.resourceMap.values());
    }

    public void setResources(final Collection<Resource> resources) {
        this.resourceMap.clear();
        this.addResources(resources);
    }

    public void addResource(final Resource resource) {
        if (resource != null) {
            this.resourceMap.put(resource.getId(), resource);
        }
    }

    public void addResources(final Collection<Resource> resources) {
        if (resources != null) {
            for (final Resource resource : resources) {
                this.addResource(resource);
            }
        }
    }

    public long getLastSynced() {
        return this.lastSynced.getTime();
    }

    public Date getLastSyncedDate() {
        return this.lastSynced;
    }

    public void setLastSynced() {
        this.lastSynced = new Date();
    }

    public void setLastSynced(final long lastSynced) {
        this.lastSynced = new Date(lastSynced);
    }

    public void setLastSynced(final Date lastSynced) {
        if (lastSynced != null) {
            this.lastSynced = lastSynced;
        } else {
            this.lastSynced = new Date(0);
        }
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
                    this.setResources(Resource.parseResources(parser, schemaVersion));
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

        this.lessonList = new ArrayList<CourseContent>();

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
                    this.lessonList.add(Quiz.fromXml(parser, schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }


}
