package org.appdev.entity;

import java.io.IOException;

import org.ekkoproject.android.player.Constants.XML;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Option extends Entity {
    private String id;
    private boolean answer = false;
    private String value;

    public String getId() {
        return this.id;
    }

    public boolean isAnswer() {
        return this.answer;
    }

    public String getValue() {
        return this.value;
    }

    public static Option fromXml(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Option().parse(parser, schemaVersion);
    }

    private Option parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_QUIZ_QUESTION_OPTION);

        this.id = parser.getAttributeValue(null, XML.ATTR_OPTION_ID);
        this.answer = parser.getAttributeValue(null, XML.ATTR_OPTION_ANSWER) != null;
        this.value = parser.nextText();

        parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_QUIZ_QUESTION_OPTION);

        return this;
    }
}
