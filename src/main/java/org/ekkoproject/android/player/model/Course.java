package org.ekkoproject.android.player.model;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import org.appdev.entity.Resource;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Course {
    private final long id;
    private int version = 0;

    private String manifestFile;
    private int manifestVersion = 0;

    private String title;
    private String banner;

    private final HashMap<String, Resource> resources = new HashMap<String, Resource>();

    /** flag that indicates this course is inaccessible for some reason */
    private boolean accessible = true;

    private Date lastSynced = new Date(0);

    public Course(final long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getBanner() {
        return this.banner;
    }

    public void setBanner(final String resourceId) {
        this.banner = resourceId;
    }

    public String getManifestFile() {
        return this.manifestFile;
    }

    public int getManifestVersion() {
        return this.manifestVersion;
    }

    public void setManifestFile(final String fileName) {
        this.manifestFile = fileName;
    }

    public void setManifestVersion(final int version) {
        this.manifestVersion = version;
    }

    public Resource getResource(final String resourceId) {
        return resources.get(resourceId);
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

    public boolean isAccessible() {
        return this.accessible;
    }

    public void setAccessible(final boolean accessible) {
        this.accessible = accessible;
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

    public static Course fromXml(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSE);
        final int schemaVersion = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_SCHEMAVERSION), 1);
        final long courseId = StringUtils.toLong(parser.getAttributeValue(null, XML.ATTR_COURSE_ID), INVALID_COURSE);
        switch (schemaVersion) {
        case 1:
            return new Course(courseId).parse(parser, schemaVersion);
            default:
            return null;
        }
    }

    /**
     * parse course xml
     * 
     * @param parser
     * @param schemaVersion
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Course parse(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSE);

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
                    final Meta meta = Meta.fromXml(parser, schemaVersion);
                    if (meta != null) {
                        this.title = meta.getTitle();
                        this.banner = meta.getBanner();
                    }
                    continue;
                } else if (XML.ELEMENT_RESOURCES.equals(name)) {
                    this.setResources(Resource.parseResources(parser, this.getId(), schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }
        return this;
    }
}
