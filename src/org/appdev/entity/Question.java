package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Question extends Entity{
	private String id;
	private int visited;
	private String last_visited;
	private String question_type;

    private String question = "";
    private final List<Option> options = new ArrayList<Option>();

    public String getId() {
        return this.id;
    }

	public int getVisited(){
		return visited;
	}
	
	public void setVisited(int visited){
		this.visited = visited;
	}
	
	public String getLastVisited(){
		return this.last_visited;
	}
	
	public void setLastVisited(String last_visited){
		this.last_visited = last_visited;
	}

	public String getGuid() {
		return id;
	}

	public void setGuid(String guid) {
		this.id = guid;
	}

    public String getQuestionType() {
		return question_type;
	}

	public void setQuestionType(String question_type) {
		this.question_type = question_type;
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
        this.question_type = parser.getAttributeValue(null, XML.ATTR_QUESTION_TYPE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_QUIZ_QUESTION_TEXT.equals(name)) {
                    this.question = parser.nextText();
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
