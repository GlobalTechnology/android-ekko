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
    private String description;

    public String getTitle() {
        return this.title;
    }

    public String getBanner() {
        return this.banner;
    }

    public String getDescription() {
        return this.description;
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

    protected Meta parse(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, Constants.XML.NS_EKKO, Constants.XML.ELEMENT_META);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (Constants.XML.NS_EKKO.equals(ns)) {
                if (Constants.XML.ELEMENT_META_TITLE.equals(name)) {
                    this.title = XmlUtils.safeNextText(parser);
                    parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO, Constants.XML.ELEMENT_META_TITLE);
                    continue;
                } else if (Constants.XML.ELEMENT_META_BANNER.equals(name)) {
                    this.banner = parser.getAttributeValue(null, Constants.XML.ATTR_RESOURCE);
                    ParserUtils.skip(parser);
                    continue;
                } else if (Constants.XML.ELEMENT_META_DESCRIPTION.equals(name)) {
                    this.description = XmlUtils.safeNextText(parser);
                    parser.require(XmlPullParser.END_TAG, Constants.XML.NS_EKKO,
                                   Constants.XML.ELEMENT_META_DESCRIPTION);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
