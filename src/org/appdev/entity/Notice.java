package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.appdev.app.AppException;
import org.appdev.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;


public class Notice implements Serializable {
	
	public final static String UTF8 = "UTF-8";
	public final static String NODE_ROOT = "ekko";
	
	public final static int	TYPE_ATME = 1;
	public final static int	TYPE_MESSAGE = 2;
	public final static int	TYPE_COMMENT = 3;

	private int atmeCount;
	private int msgCount;
	private int reviewCount;
	
	public int getAtmeCount() {
		return atmeCount;
	}
	public void setAtmeCount(int atmeCount) {
		this.atmeCount = atmeCount;
	}
	public int getMsgCount() {
		return msgCount;
	}
	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}
	public int getReviewCount() {
		return reviewCount;
	}
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	
	public static Notice parse(InputStream inputStream) throws IOException, AppException {
		Notice notice = null;
    
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
       
            int evtType=xmlParser.getEventType();
			   
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:			    		
			           
			            if(tag.equalsIgnoreCase("notice"))
			    		{
			            	notice = new Notice();
			    		}
			            else if(notice != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				notice.setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	notice.setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	notice.setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				   
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:		    		
				       	break; 
			    }
			    
			    evtType=xmlParser.next();
			}		
        } catch (XmlPullParserException e) {
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }      
        return notice;       
	}
}
