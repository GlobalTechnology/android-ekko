package org.appdev.entity;

import java.io.IOException;
import java.io.InputStream;

import org.appdev.app.AppException;
import org.ekkoproject.android.player.model.Manifest;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class CourseManifest extends Entity{
    public static Manifest parse(InputStream inputStream) throws IOException, AppException {
        try {
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(inputStream, UTF8);
            parser.nextTag();
            return Manifest.fromXml(parser);
        } catch (final XmlPullParserException e) {
            throw AppException.xml(e);
        }
	}
}
