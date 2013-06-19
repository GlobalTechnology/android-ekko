package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Quiz  extends CourseContent{
	
	/**
	 * Quiz class
	 */

	public static final String ID = "_id";
	public static final String NAME = "quiz_title";
	public static final String VISITED = "visited";
	public static final String LAST	= "last_visited";
	public static final String QUIZ_ID = "quiz_id";
	
	private String quiz_id;
	private String quiz_title;
	private int visited;
	private String last_visited;

    private final List<Question> questions = new ArrayList<Question>();

    @Override
    public String getId() {
        return this.quiz_id;
    }

    @Deprecated
    public String getQuizId() {
        return this.getId();
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

	public String getQuizTitle() {
		return quiz_title;
	}

	public void setQuizTitle(String quiz_title) {
		this.quiz_title = quiz_title;
	}

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(this.questions);
    }

    public Question getQuestion(final String questionId) {
        if (questionId != null) {
            for (final Question question : this.questions) {
                if (questionId.equals(question.getId())) {
                    return question;
                }
            }
        }
        return null;
    }

    public static Quiz fromXml(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Quiz().parse(parser, schemaVersion);
    }

    private Quiz parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT_QUIZ);

        this.quiz_id = parser.getAttributeValue(null, XML.ATTR_QUIZ_ID);
        this.quiz_title = parser.getAttributeValue(null, XML.ATTR_QUIZ_TITLE);
        // this.quiz_title = "Quiz"; //for testing

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_QUIZ_QUESTION.equals(name)) {
                    this.questions.add(Question.fromXml(parser, schemaVersion));
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
