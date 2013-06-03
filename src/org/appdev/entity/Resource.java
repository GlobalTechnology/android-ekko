package org.appdev.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.appdev.utils.StringUtils;
import org.ekkoproject.android.player.Constants.XML;
import org.ekkoproject.android.player.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Resource implements Serializable{


	//define the mimeType the player supported
	public static final String[] imageTypeSupported = {"image/png", "image/jpg", "image/jpeg"};
	public static final String[] videoTypeSupported = {"video/mp4"};
	

	private String id;
	private String sha1;
	private long size;
	private String file;
	private String type;
	private String provider;
	private String mimeType;
	private List<Resource> items;

	private static boolean isContainsItemFromList(String input, String[] items)
	{
	    for(int i =0; i < items.length; i++)
	    {
	        if(input.contains(items[i]))
	        {
	            return true;
	        }
	    }
	    return false;
	}
	
	/**
	 * Image type: such as "image/png"
	 */
	public  boolean isSupportedImageType(){
		if(StringUtils.isEmpty(mimeType)) return false;
		return isContainsItemFromList(mimeType, imageTypeSupported);
		
	}

	
	/**
	 * Image type: such as "image/png"
	 */
	public  boolean isSupportedVideoType(){
		if(StringUtils.isEmpty(this.mimeType)) return false;
		return isContainsItemFromList(mimeType, videoTypeSupported);
		
	}

	public  String getResourceURI(String courseBaseHub){
		String url = null;
		url = courseBaseHub + "/resources/resource/" + this.sha1;
		return url;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceSha1() {
		return sha1;
	}

	public void setResourceSha1(String sha1) {
		this.sha1 = sha1;
	}

	public long getResourceSize() {
		return size;
	}

	public void setResourceSize(long size) {
		this.size = size;
	}

	public String getResourceFile() {
		return file;
	}

	public void setResourceFile(String file) {
		this.file = file;
	}

	public String getResourceType() {
		return type;
	}

	public void setResourceType(String type) {
		this.type = type;
	}

	public List<Resource> getItems() {
		return items;
	}

	public void setItems(List<Resource> items) {
		this.items = items;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getResourceMimeType() {
		return mimeType;
	}

	public void setResourceMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

    public boolean isDynamic() {
        return "dynamic".equals(this.type);
    }

    public static List<Resource> parseResources(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCES);
        return parseResourceNodes(parser, schemaVersion);
    }

    public static Resource parse(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        return new Resource().parseInternal(parser, schemaVersion);
    }

    private Resource parseInternal(final XmlPullParser parser, final int schemaVersion) throws XmlPullParserException,
            IOException {
        parser.require(XmlPullParser.START_TAG, XML.NS_EKKO, XML.ELEMENT_RESOURCE);

        this.id = parser.getAttributeValue(null, XML.ATTR_RESOURCE_ID);
        this.type = parser.getAttributeValue(null, XML.ATTR_RESOURCE_TYPE);
        this.sha1 = parser.getAttributeValue(null, XML.ATTR_RESOURCE_SHA1);
        this.file = parser.getAttributeValue(null, XML.ATTR_RESOURCE_FILE);
        this.mimeType = parser.getAttributeValue(null, XML.ATTR_RESOURCE_MIMETYPE);
        this.provider = parser.getAttributeValue(null, XML.ATTR_RESOURCE_PROVIDER);

        // handle any nested resources
        if (this.isDynamic()) {
            this.items = parseResourceNodes(parser, schemaVersion);
        } else {
            ParserUtils.skip(parser);
        }

        return this;
    }

    private static List<Resource> parseResourceNodes(final XmlPullParser parser, final int schemaVersion)
            throws XmlPullParserException, IOException {
        final List<Resource> resources = new ArrayList<Resource>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            final String ns = parser.getNamespace();
            final String name = parser.getName();
            if (XML.NS_EKKO.equals(ns) && XML.ELEMENT_RESOURCE.equals(name)) {
                resources.add(Resource.parse(parser, schemaVersion));
                continue;
            }

            // skip unrecognized nodes
            ParserUtils.skip(parser);
        }

        return resources;
    }
}
