package org.appdev.entity;

import java.io.IOException;
import java.io.Serializable;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Media implements Serializable{
    private String id;
    private String type;

	private String media_resource;
	private String media_thumbnail;	

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

    public String getMediaResourceID() {
		return media_resource;
	}
	
	public void setMediaResourcelID(String media_resource) {
		this.media_resource = media_resource;
	}
	
	public String getMediaThumbnailID() {
		return media_thumbnail;
	}
	
	public void setMediaThumbnailID(String media_thumbnail) {
		this.media_thumbnail = media_thumbnail;
	}

    public static Media parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Media().parseInternal(parser, schemaVersion);
    }

    private Media parseInternal(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_LESSON_MEDIA);

        this.id = parser.getAttributeValue(null, XML.ATTR_MEDIA_ID);
        this.type = parser.getAttributeValue(null, XML.ATTR_MEDIA_TYPE);
        this.media_resource = parser.getAttributeValue(null, XML.ATTR_RESOURCE);
        this.media_thumbnail = parser.getAttributeValue(null, XML.ATTR_THUMBNAIL);

        // discard any nested nodes
        ParserUtils.skip(parser);

        return this;
    }
}
