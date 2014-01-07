package org.ekkoproject.android.player.model;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Resource {
    public static final int PROVIDER_UNKNOWN = -1;
    public static final int PROVIDER_NONE = 0;
    public static final int PROVIDER_YOUTUBE = 1;
    public static final int PROVIDER_VIMEO = 2;

    public final static long INVALID_VIDEO = -1;

    private final long courseId;
    private final String id;

	private String sha1;
	private long size;
	private String file;
	private String type;
	private String provider;
    private String uri;
	private String mimeType;
    private long videoId;
    private final List<Resource> resources = new ArrayList<Resource>();

    private String parentId;

    public Resource(final long courseId, final String id) {
        this.courseId = courseId;
        this.id = id;
    }

    public long getCourseId() {
        return this.courseId;
    }

    public String getId() {
        return this.id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    public long getVideoId() {
        return this.videoId;
    }

    public void setVideoId(final long videoId) {
        this.videoId = videoId;
    }

    public String getResourceSha1() {
		return sha1;
	}

	public void setResourceSha1(String sha1) {
		this.sha1 = sha1;
	}

	public long getResourceSize() {
		return size;
	}

	public void setResourceSize(long size) {
		this.size = size;
	}

	public String getResourceFile() {
		return file;
	}

	public void setResourceFile(String file) {
		this.file = file;
	}

	public String getResourceType() {
		return type;
	}

	public void setResourceType(String type) {
		this.type = type;
	}

	public String getResourceMimeType() {
		return mimeType;
	}

	public void setResourceMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

    /**
     * @return the uri for uri type resources
     */
    public String getUri() {
        return this.uri;
    }

    public int getProvider() {
        // handle known providers
        if (this.provider == null) {
            return PROVIDER_NONE;
        } else if (this.provider.equals("youtube")) {
            return PROVIDER_YOUTUBE;
        } else if (this.provider.equals("vimeo")) {
            return PROVIDER_VIMEO;
        }

        return PROVIDER_UNKNOWN;
    }

    public String getProviderName() {
        return this.provider;
    }

    /**
     * @param uri
     *            the uri for uri type resources
     */
    public void setUri(final String uri) {
        this.uri = uri;
    }

    public void setProvider(final String provider) {
        this.provider = provider;
    }

    public Collection<Resource> getResources() {
        return Collections.unmodifiableCollection(this.resources);
    }

    public void addResource(final Resource resource) {
        if (resource != null) {
            this.resources.add(resource);
        }
    }

    public void addResources(final Collection<Resource> resources) {
        if (resources != null) {
            for (final Resource resource : resources) {
                this.addResource(resource);
            }
        }
    }

    public void setResources(final Collection<Resource> resources) {
        this.resources.clear();
        this.addResources(resources);
    }

    public boolean isDynamic() {
        return "dynamic".equals(this.type);
    }

    public boolean isEcv() {
        return "ecv".equals(this.type);
    }

    public boolean isFile() {
        return "file".equals(this.type);
    }

    public boolean isUri() {
        return "uri".equals(this.type);
    }

    public static List<Resource> parseResources(final XmlPullParser parser, final long courseId, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCES);
        return parseResourceNodes(parser, courseId, schemaVersion);
    }

    public static Resource fromXml(final XmlPullParser parser, final long courseId, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);
        final String id = parser.getAttributeValue(null, XML.ATTR_RESOURCE_ID);
        return new Resource(courseId, id).parse(parser, schemaVersion);
    }

    public static Resource fromXml(final XmlPullParser parser, final Course course, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);
        final String id = parser.getAttributeValue(null, XML.ATTR_RESOURCE_ID);
        return new Resource(course.getId(), id).parse(parser, schemaVersion);
    }

    public static Resource fromXml(final XmlPullParser parser, final Manifest manifest, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);
        final String id = parser.getAttributeValue(null, XML.ATTR_RESOURCE_ID);
        return new Resource(manifest.getCourseId(), id).parse(parser, schemaVersion);
    }

    public static Resource fromXml(final XmlPullParser parser, final Resource resource, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);
        final String id = parser.getAttributeValue(null, XML.ATTR_RESOURCE_ID);
        return new Resource(resource.getCourseId(), id).parse(parser, schemaVersion);
    }

    private Resource parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);

        this.type = parser.getAttributeValue(null, XML.ATTR_RESOURCE_TYPE);
        this.sha1 = parser.getAttributeValue(null, XML.ATTR_RESOURCE_SHA1);
        this.size = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_RESOURCE_SIZE), -1);
        this.file = parser.getAttributeValue(null, XML.ATTR_RESOURCE_FILE);
        this.mimeType = parser.getAttributeValue(null, XML.ATTR_RESOURCE_MIMETYPE);
        this.provider = parser.getAttributeValue(null, XML.ATTR_RESOURCE_PROVIDER);
        this.uri = parser.getAttributeValue(null, XML.ATTR_RESOURCE_URI);
        this.videoId = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_RESOURCE_VIDEO_ID), INVALID_VIDEO);

        // handle any nested resources
        if (this.isDynamic()) {
            this.setResources(parseResourceNodes(parser, this.getCourseId(), schemaVersion));
        } else {
            ParserUtils.skip(parser);
        }

        return this;
    }

    private static List<Resource> parseResourceNodes(final XmlPullParser parser, final long courseId,
            final int schemaVersion) throws XmlPullParserException, IOException {
        final List<Resource> resources = new ArrayList<Resource>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns) && XML.ELEMENT_RESOURCE.equals(name)) {
                resources.add(Resource.fromXml(parser, courseId, schemaVersion));
                continue;
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return resources;
    }
}
