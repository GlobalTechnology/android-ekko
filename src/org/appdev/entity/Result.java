package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;

import org.appdev.app.AppException;
import org.appdev.utils.StringUtils;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * Data processing result
 */
public class Result {

	private int errorCode;
	private String errorMessage;

	
	public boolean OK() {
		return errorCode == 1;
	}

	/**
	 * Result parsing
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static Result parse(InputStream stream) throws IOException, AppException {
		Result res = null;
	      
		// get XmlPullParser parserr
		XmlPullParser xmlParser = Xml.newPullParser();

		try {
			xmlParser.setInput(stream, Base.UTF8);
			// get event type of parsed xml¡£
			int evtType = xmlParser.getEventType();
	
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {

				case XmlPullParser.START_TAG:
					// if it is start tag, init Result instance
					if (tag.equalsIgnoreCase("result")) 
					{
						res = new Result();
					} 
					else if (res != null) 
					{ 
						if (tag.equalsIgnoreCase("errorCode")) 
						{
							res.errorCode = StringUtils.toInt(xmlParser.nextText(), -1);
						} 
						else if (tag.equalsIgnoreCase("errorMessage")) 
						{
							res.errorMessage = xmlParser.nextText().trim();
						}
					}
		
					break;
				case XmlPullParser.END_TAG:

					break;
				}
				// If XML is not the end, go to the next node
				evtType = xmlParser.next();
			}

		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}

		return res;

	}

	public int getErrorCode() {
		return errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	@Override
	public String toString(){
		return String.format("RESULT: CODE:%d,MSG:%s", errorCode, errorMessage);
	}

}
