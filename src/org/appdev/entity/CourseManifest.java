package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appdev.app.AppException;
import org.appdev.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

public class CourseManifest extends Entity{

	public final static String NODE_ROOT ="course";
	public final static String NODE_ROOT_ATTR_ID = "id";
	public final static String NODE_ROOT_ATTR_VER = "version";
	public final static String NODE_ROOT_ATTR_URI = "uri";
	public final static String NODE_ROOT_ATTR_SCHEMA_VER = "schemaVersion";
	public final static String NODE_ROOT_ATTR_ZIP_URI= "zipUri";

	public final static String NODE_META = "meta";
	public final static String NODE_META_TITLE = "title";
	public final static String NODE_META_BANNER = "banner";
	public final static String NODE_META_DESC = "description";
	public final static String NODE_META_COPYRIGHT = "copyright";
	
	public final static String NODE_META_AUTHOR = "author";
	public final static String NODE_META_AUTHOR_NAME = "name";
	public final static String NODE_META_AUTHOR_EMAIL = "email";
	public final static String NODE_META_AUTHOR_URL = "url";
	
	public final static String NODE_LESSONS = "content";
	public final static String NODE_LESSON_ATTR_ID = "id";
	public final static String NODE_LESSON_ATTR_TITLE = "title";
	
	public final static String NODE_LESSON = "lesson";
	public final static String NODE_LESSON_TEXT = "text";
	public final static String NODE_LESSON_MEDIA = "media";
	
	public final static String NODE_RESOURCES = "resources";
	public final static String NODE_RESOURCE = "resource";
	
	public final static String NODE_MEDIA_ATTR_RESOURCE = "resource";
	public final static String NODE_MEDIA_ATTR_THUMBNAIL = "thumbnail";
	
	
	public final static String RESOURCETYPE_IMAGE_PNG = "image/png";
	public final static String RESOURCETYPE_VIDEO_MP4 = "video/mp4";
	public final static String RESOURCETYPE_IMAGE_JPEG = "image/jpeg";
	
	public final static String RESOURCETYPE_DYNAMIC = "dynamic";
	
	public final static String RESOURCESTYPE_MIMETYPE = "mimeType";
	
		
	public final static String NODE_RESOURCE_ATTR_ID = "id";
	public final static String NODE_RESOURCE_ATTR_FILE = "file";
	public final static String NODE_RESOURCE_ATTR_SHA1 = "sha1";
	public final static String NODE_RESOURCE_ATTR_SIZE = "size";
	public final static String NODE_RESOURCE_ATTR_TYPE = "type";
	public final static String NODE_RESOURCE_ATTR_PROVIDER = "provider";
	public final static String NODE_RESOURCE_ATTR_MIME_TYPE = "mimeType";
	
		
	public static Course parse(InputStream inputStream) throws IOException, AppException {
		
		Course course = null;
		Lesson lesson = null;
		MediaElements mediaElements = null;
		TextElements textElements = null;
		ArrayList <Lesson> lessonList = new ArrayList<Lesson>();
		HashMap<String, Resource> resourceMap = null;
				       
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
          
            int evtType=xmlParser.getEventType();
		  
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(tag.equalsIgnoreCase(NODE_ROOT))
			    		{
			    			course = new Course();
			    			course.setCourseGUID(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_ID));
			    			course.setCourseVersion(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_VER));		
			    			course.setCourseZipUri(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_ZIP_URI));
			    			course.setCourseURI(xmlParser.getAttributeValue(null, NODE_ROOT_ATTR_URI));
			    			
			    		}
			    		else if(course != null)
			    		{	
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
				            else if(tag.equalsIgnoreCase(NODE_LESSON))
				            {
				            	lesson = new Lesson();
				            	lesson.setGuid(xmlParser.getAttributeValue(null, NODE_LESSON_ATTR_ID));
				            	lesson.setLesson_title(xmlParser.getAttributeValue(null, NODE_LESSON_ATTR_TITLE));
				            	mediaElements = new MediaElements();
				            	textElements = new TextElements();
				            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_LESSON_MEDIA))
				            {
				            	Media media = new Media();
				            	media.setMediaResourcelID(xmlParser.getAttributeValue(null, NODE_MEDIA_ATTR_RESOURCE));
				            	media.setMediaThumbnailID(xmlParser.getAttributeValue(null, NODE_MEDIA_ATTR_THUMBNAIL));				            	
				            	mediaElements.addElement(media);
				            	media=null;	
				            }
				            else if(tag.equalsIgnoreCase(NODE_LESSON_TEXT))
				            {
				            	textElements.addElement(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_RESOURCES))
				            {
				            	resourceMap = new HashMap<String, Resource>();
				            }
				            else if(tag.equalsIgnoreCase(NODE_RESOURCE))
				            {
				            	Resource resource = new Resource();
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
				       	if (tag.equalsIgnoreCase(NODE_LESSON) && course!=null && lesson!=null) { 
				       		lesson.setLessonMedia(mediaElements);
				       		lesson.setPagedTextList(textElements);
				       		lessonList.add(lesson);
				       		
				       		lesson = null; 
				       		mediaElements = null;
				       		textElements = null;				  
				       	} else if (tag.equalsIgnoreCase(NODE_ROOT)){
				       		course.setLessonList(lessonList);
				       	}
				       	else if(tag.equalsIgnoreCase(NODE_RESOURCES)){
				       		course.setResourceMap(resourceMap);
				       	}
				       	break; 
			    }			
			    evtType=xmlParser.next();
			}		
        } catch (XmlPullParserException e) {
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }      
        return course;       
	}
}
