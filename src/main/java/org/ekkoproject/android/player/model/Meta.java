package org.ekkoproject.android.player.model;

import org.ccci.gto.android.common.util.XmlUtils;
import org.ekkoproject.android.player.Constants;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Meta {
    private String title;
    private String banner;
    private String authorName;
    private String description;
    private String copyright;

    public String getTitle() {
        return this.title;
    }

    public String getBanner() {
        return this.banner;
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCopyright() {
        return this.copyright;
    }

    public static Meta fromXml(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        switch (schemaVersion) {
            case 1:
                return new Meta().parse(parser, schemaVersion);
            default:
                return null;
        }
    }

    private Meta parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, Constants.XML.NS_EKKO, Constants.XML.ELEMENT_META);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (Constants.XML.NS_EKKO.equals(ns)) {
                switch (name) {
                    case Constants.XML.ELEMENT_META_TITLE:
                        this.title = XmlUtils.safeNextText(parser);
                        parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO, Constants.XML.ELEMENT_META_TITLE);
                        continue;
                    case Constants.XML.ELEMENT_META_BANNER:
                        this.banner = parser.getAttributeValue(null, Constants.XML.ATTR_RESOURCE);
                        ParserUtils.skip(parser);
                        continue;
                    case Constants.XML.ELEMENT_META_AUTHOR:
                        parseAuthor(parser, schemaVersion);
                        continue;
                    case Constants.XML.ELEMENT_META_DESCRIPTION:
                        this.description = XmlUtils.safeNextText(parser);
                        parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO,
                                       Constants.XML.ELEMENT_META_DESCRIPTION);
                        continue;
                    case Constants.XML.ELEMENT_META_COPYRIGHT:
                        this.copyright = XmlUtils.safeNextText(parser);
                        parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO,
                                       Constants.XML.ELEMENT_META_COPYRIGHT);
                        continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    private void parseAuthor(final XmlPullParser parser, final int schemaVersion)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, Constants.XML.NS_EKKO, Constants.XML.ELEMENT_META_AUTHOR);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (Constants.XML.NS_EKKO.equals(ns)) {
                switch (name) {
                    case Constants.XML.ELEMENT_META_AUTHOR_NAME:
                        this.authorName = XmlUtils.safeNextText(parser);
                        parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO,
                                       Constants.XML.ELEMENT_META_AUTHOR_NAME);
                        continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }
    }
}
