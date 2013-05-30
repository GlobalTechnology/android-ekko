package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appdev.app.AppException;
import org.appdev.utils.StringUtils;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

/**
 * Courselist
 */
public class CourseList extends Entity{

	public final static int CATALOG_ALL = 1;
	
	public final static String NODE_ROOT = "courses";
	public final static String NODE_ROOT_ATTR_START = "start";
	public final static String NODE_ROOT_ATTR_LIMIT = "limit";
	public final static String NODE_ROOT_ATTR_HASMORE = "hasMore";
	public final static String NODE_ROOT_ATTR_DEBUG = "debug";
	public final static String NODE_ROOT_ATTR_MORE_URI = "moreUri";
	
	public final static String NODE_COURSE = "course";
	public final static String NODE_COURSE_ATTR_ID = "id";
	public final static String NODE_COURSE_ATTR_VER = "version";
	public final static String NODE_COURSE_ATTR_URI = "uri";
	public final static String NODE_COURSE_ATTR_ZIP_URI = "zipUri";
	
	public final static String NODE_META = "meta";
	public final static String NODE_META_TITLE = "title";
	public final static String NODE_META_BANNER = "banner";
	public final static String NODE_META_DESC = "description";
	public final static String NODE_META_COPYRIGHT = "copyright";
	
	public final static String NODE_META_AUTHOR = "author";
	public final static String NODE_META_AUTHOR_NAME = "name";
	public final static String NODE_META_AUTHOR_EMAIL = "email";
	public final static String NODE_META_AUTHOR_URL = "url";
	
	public final static String NODE_MEDIA_ATTR_RESOURCE = "resource";
	public final static String NODE_MEDIA_ATTR_THUMBNAIL = "thumbnail";
	
	public final static String NODE_RESOURCES = "resources";
	public final static String NODE_RESOURCE = "resource";
	
	public final static String RESOURCETYPE_IMAGE_PNG = "image/png";
	public final static String RESOURCETYPE_VIDEO_MP4 = "video/mp4";
	public final static String RESOURCETYPE_IMAGE_JPEG = "image/jpeg";

	
	public final static String NODE_RESOURCE_ATTR_ID = "id";
	public final static String NODE_RESOURCE_ATTR_FILE = "file";
	public final static String NODE_RESOURCE_ATTR_SHA1 = "sha1";
	public final static String NODE_RESOURCE_ATTR_SIZE = "size";
	public final static String NODE_RESOURCE_ATTR_TYPE = "type";
	public final static String NODE_RESOURCE_ATTR_MIME_TYPE = "mimeType";

	private int catalog;
	private int start;
	private int limit;
	private boolean hasMore;
	private String moreURI;
	private boolean debug;

	private List<Course> courseList = new ArrayList<Course>();
	
	public int getCatalog() {
		return catalog;
	}

	public List<Course> getCourselist() {
		return courseList;
	}
	
	public static CourseList parse(InputStream inputStream) throws IOException, AppException {
		CourseList courselist=null;
		Course course = null;
		HashMap<String, Resource> resourceMap = new HashMap<String, Resource>();
		Resource resource = null;
        //get XmlPullParser
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //get the event type
            int evtType=xmlParser.getEventType();
			//loop until the end of the XML doc
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(tag.equalsIgnoreCase(NODE_ROOT)) 
			    		{
			    			courselist = new CourseList();
			    			courselist.setStart(StringUtils.toInt(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_START)));
			    			courselist.setLimit(StringUtils.toInt(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_LIMIT)));
			    			courselist.setHasMore(StringUtils.toBool(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_HASMORE)));
			    			courselist.setMoreURI(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_MORE_URI));
			    			courselist.setDebug(StringUtils.toBool(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_DEBUG)));
			    		}
			    		else if(tag.equalsIgnoreCase(NODE_COURSE)) 
			    		{
			    			course = new Course();
			    			course.setCourseGUID(xmlParser.getAttributeValue(null, NODE_COURSE_ATTR_ID));
			    			course.setCourseVersion(xmlParser.getAttributeValue(null, NODE_COURSE_ATTR_VER));
			    			course.setCourseURI(xmlParser.getAttributeValue(null, NODE_COURSE_ATTR_URI));
			    			course.setCourseZipUri(xmlParser.getAttributeValue(null, NODE_COURSE_ATTR_ZIP_URI));
			    			
			    		}
			    		else if(course != null) {
			    			 if(tag.equalsIgnoreCase(NODE_META_TITLE))
					            {			      
					            	course.setCourseTitle(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_META_AUTHOR_NAME))
					            {			            	
					            	course.setAuthorName(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_META_AUTHOR_EMAIL))
					            {			            	
					            	course.setAuthorEmail(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_META_AUTHOR_URL))
					            {			            	
					            	course.setAuthorUrl(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_META_BANNER))
					            {		
					            	course.setCourseBanner(xmlParser.getAttributeValue(null, NODE_MEDIA_ATTR_RESOURCE));				            					            	
					            } 
					            else if(tag.equalsIgnoreCase(NODE_META_DESC))
					            {
					            	course.setCourseDescription(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_META_COPYRIGHT))
					            {
					            	course.setCourseCopyright(xmlParser.nextText());
					            }
					            else if(tag.equalsIgnoreCase(NODE_RESOURCES))
					            {
					            	resourceMap = new HashMap<String, Resource>();
					            }
					            else if(tag.equalsIgnoreCase(NODE_RESOURCE))
					            {
					            	resource = new Resource();
					            	resource.setId(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_ID));
					            	resource.setResourceSha1(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_SHA1));
					            	resource.setResourceFile(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_FILE));
					            	resource.setResourceType(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_TYPE));
					            	resource.setResourceMimeType(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_MIME_TYPE));
					            	resource.setResourceSize(StringUtils.toLong(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_SIZE)));
					            	resourceMap.put(xmlParser.getAttributeValue(null, NODE_RESOURCE_ATTR_ID), resource);
					            
					            	resource=null;
					            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//end tag, and add the object to the course list
				       	if (tag.equalsIgnoreCase(NODE_COURSE) && course != null) { 
				    	   courselist.courseList.add(course); 
				    	   
				       	}
				       	else if(tag.equalsIgnoreCase(NODE_RESOURCES)){
				       		course.setResourceMap(resourceMap);
				       		resourceMap = null;
				       	}				       
				       	else if(tag.equalsIgnoreCase(NODE_ROOT) && courselist !=null){
				       		
				       	}
				       	break; 
			    }
			    //go to the next node of XML
			    int a =xmlParser.next();
			    evtType=a;
			}		
        } catch (XmlPullParserException e) {
        	e.printStackTrace();
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }  
        Log.i("CourseList", courselist.toString());
        return courselist;       
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getMoreURI() {
		return moreURI;
	}

	public void setMoreURI(String moreURI) {
		this.moreURI = moreURI;
	}

    public static CourseList parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        return new CourseList().parseInternal(parser);
    }

    private CourseList parseInternal(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_HUB, XML.ELEMENT_COURSES);

        this.start = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSES_START));
        this.limit = StringUtils.toInt(parser.getAttributeValue(null, XML.ATTR_COURSES_LIMIT));
        this.hasMore = StringUtils.toBool(parser.getAttributeValue(null, XML.ATTR_COURSES_HASMORE));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_HUB.equals(ns) && XML.ELEMENT_COURSE.equals(name)) {
                final Course course = Course.parse(parser);
                this.courseList.add(course);
                continue;
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return this;
    }
}
