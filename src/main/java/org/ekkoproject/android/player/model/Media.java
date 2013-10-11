package org.ekkoproject.android.player.model;

import java.io.IOException;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Media {
    private String id;
    private String type;

    private String resource;
    private String thumbnail;

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public boolean isAudio() {
        return "audio".equals(this.type);
    }

    public boolean isImage() {
        return "image".equals(this.type);
    }

    public boolean isVideo() {
        return "video".equals(this.type);
    }

    public String getResource() {
        return this.resource;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public static Media fromXml(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Media().parse(parser, schemaVersion);
    }

    private Media parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_LESSON_MEDIA);

        this.id = parser.getAttributeValue(null, XML.ATTR_MEDIA_ID);
        this.type = parser.getAttributeValue(null, XML.ATTR_MEDIA_TYPE);
        this.resource = parser.getAttributeValue(null, XML.ATTR_RESOURCE);
        this.thumbnail = parser.getAttributeValue(null, XML.ATTR_THUMBNAIL);

        // discard any nested nodes
        ParserUtils.skip(parser);

        return this;
    }
}
