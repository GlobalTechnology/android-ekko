package org.ekkoproject.android.player.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.appdev.entity.Media;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Lesson extends CourseContent {
    private String id;
    private String title;

    private final List<Media> media = new ArrayList<Media>();
    private final List<Text> text = new ArrayList<Text>();

    @Override
    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public List<Media> getMedia() {
        return Collections.unmodifiableList(this.media);
    }

    public Media getMedia(final String mediaId) {
        if (mediaId != null) {
            for (final Media media : this.media) {
                if (mediaId.equals(media.getId())) {
                    return media;
                }
            }
        }
        return null;
    }

    public List<Text> getText() {
        return Collections.unmodifiableList(this.text);
    }

    public Text getText(final String textId) {
        if (textId != null) {
            for (final Text text : this.text) {
                if (textId.equals(text.getId())) {
                    return text;
                }
            }
        }
        return null;
    }

    public static Lesson fromXml(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Lesson().parse(parser, schemaVersion);
    }

    private Lesson parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT_LESSON);

        this.id = parser.getAttributeValue(null, XML.ATTR_LESSON_ID);
        this.title = parser.getAttributeValue(null, XML.ATTR_LESSON_TITLE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_LESSON_MEDIA.equals(name)) {
                    this.media.add(Media.parse(parser, schemaVersion));
                    continue;
                } else if (XML.ELEMENT_LESSON_TEXT.equals(name)) {
                    final String id = parser.getAttributeValue(null, XML.ATTR_TEXT_ID);
                    final String content = parser.nextText();
                    this.text.add(new Text(id, content));
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_LESSON_TEXT);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
