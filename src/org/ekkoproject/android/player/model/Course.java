package org.ekkoproject.android.player.model;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import java.io.IOException;
import java.util.Date;

import org.appdev.entity.Resource;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.ekkoproject.android.player.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Course extends org.appdev.entity.Course {
    private final long id;
    private int version = 0;

    private String manifestFile;
    private int manifestVersion = 0;

    private Date lastSynced = new Date(0);

    public Course(final long id) {
        this.id = id;
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
        return new Course(courseId).parse(parser, schemaVersion);
    }

    private Course parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
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
                    this.parseMeta(parser, schemaVersion);
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
