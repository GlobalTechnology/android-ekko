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
	
	/**
	 * Lesson class
	 */

	public static final String ID = "_id";
	public static final String VISITED = "visited";
	public static final String LAST	= "last_visited";

	
	private String id;
	private String question_title;
	private int visited;
	private String last_visited;
	private String question_type;

    private final List<String> options = new ArrayList<String>();

	public Question(){
		
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


	public String getQuestionTitle() {
		return question_title;
	}

	public void setQestionTitle(String question_title) {
		this.question_title = question_title;
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

	public static Question parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Question().parseInternal(parser, schemaVersion);
    }

    private Question parseInternal(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT_QUESTION);

        this.id = parser.getAttributeValue(null, XML.ATTR_QUESTION_ID);
        this.setQuestionType(parser.getAttributeValue(null, XML.ATTR_QUESTION_TYPE));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
            	if(XML.ELEMENT_CONTENT_QUESTION_TEXT.equals(name)){
            		
            	}else if (XML.ELEMENT_CONTENT_QUESTION_OPTIONS.equals(name)) {

                    continue;
                } 
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
