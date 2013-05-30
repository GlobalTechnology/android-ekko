package org.appdev.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Lesson extends Entity{
	
	/**
	 * Lesson class
	 */

	public static final String ID = "_id";
	public static final String NAME = "lesson_name";
	public static final String VISITED = "visited";
	public static final String LAST	= "last_visited";
	public static final String COURSE_ID = "course_id";
	public static final String TABLE_NAME = "lessons";	
	
	private String guid;
	private String lesson_title;
	private int visited;
	private String last_visited;
	private String course_id;

    private final List<Media> media = new ArrayList<Media>();
    private final List<String> text = new ArrayList<String>();

	public Lesson(){
		
	}
	
	public static Lesson getNumbLesson(){
		Lesson lesson = null;
		lesson = new Lesson();
		lesson.setLesson_title("no content");
		
		//add the text elements
        lesson.text.add("Empty content!");
		
		//add default media element
		Media media = new Media();
		media.setMediaThumbnailID(""); //to do: add a default media later
        lesson.media.add(media);
		return lesson;
	}

	public String getCourseId(){
		return this.course_id;
	}
	
	public void setCourseId(String course_id){
		this.course_id = course_id;
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

	public String getLesson_title() {
		return lesson_title;
	}

	public void setLesson_title(String lesson_title) {
		this.lesson_title = lesson_title;
	}

    public List<Media> getMedia() {
        return Collections.unmodifiableList(this.media);
    }

    public List<String> getText() {
        return Collections.unmodifiableList(this.text);
    }

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

    public static Lesson parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Lesson().parseInternal(parser, schemaVersion);
    }

    private Lesson parseInternal(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT_LESSON);

        this.lesson_title = parser.getAttributeValue(null, XML.ATTR_LESSON_TITLE);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns)) {
                if (XML.ELEMENT_CONTENT_MEDIA.equals(name)) {
                    this.media.add(Media.parse(parser, schemaVersion));
                    continue;
                } else if (XML.ELEMENT_CONTENT_TEXT.equals(name)) {
                    // TODO: we don't capture the text id currently
                    this.text.add(parser.nextText());
                    parser.require(XmlPullParser.END_TAG, XML.NS_EKKO, XML.ELEMENT_CONTENT_TEXT);
                    continue;
                }
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
