package org.ekkoproject.android.player.model;

import org.ccci.gto.android.common.util.XmlUtils;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    private String id;
    private String type;

    private String question = "";
    private final List<Option> options = new ArrayList<Option>();

    public String getId() {
        return this.id;
    }

    public String getType() {
        return type;
    }

    public String getQuestion() {
        return this.question;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    public static Question fromXml(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Question().parse(parser, schemaVersion);
    }

    private Question parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_QUIZ_QUESTION);

        this.id = parser.getAttributeValue(null, XML.ATTR_QUESTION_ID);
        this.type = parser.getAttributeValue(null, XML.ATTR_QUESTION_TYPE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_QUIZ_QUESTION_TEXT.equals(name)) {
                    this.question = XmlUtils.safeNextText(parser);
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_QUIZ_QUESTION_TEXT);
                    continue;
                } else if (XML.ELEMENT_QUIZ_QUESTION_OPTIONS.equals(name)) {
                    this.parseOptions(parser, schemaVersion);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }

    private Question parseOptions(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_QUIZ_QUESTION_OPTIONS);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_QUIZ_QUESTION_OPTION.equals(name)) {
                    this.options.add(Option.fromXml(parser, schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
