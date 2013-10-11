package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.ekkoproject.android.player.Constants.EKKOHUB_URI;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.util.Xml;

import org.appdev.entity.Resource;
import org.ccci.gto.android.common.api.AbstractGtoSmxApi;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.thekey.TheKeyImpl;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public final class EkkoHubApi extends AbstractGtoSmxApi {
    private static final Logger LOG = LoggerFactory.getLogger(EkkoHubApi.class);

    /** Broadcast actions */
    public static final String ACTION_ERROR_CONNECTION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_CONNECTION";
    public static final String ACTION_ERROR_INVALIDSESSION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_INVALIDSESSION";

    private static final String PREFFILE_EKKOHUB = "ekkoHubApi";

    public EkkoHubApi(final Context context) {
        this(context, EKKOHUB_URI);
    }

    public EkkoHubApi(final Context context, final String hubUri) {
        this(context, Uri.parse(hubUri.endsWith("/") ? hubUri : hubUri + "/"));
    }

    public EkkoHubApi(final Context context, final Uri hubUri) {
        super(context, new TheKeyImpl(context, THEKEY_CLIENTID), PREFFILE_EKKOHUB, hubUri);
    }

    public static void broadcastConnectionError(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_ERROR_CONNECTION));
    }

    public static void broadcastInvalidSession(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_ERROR_INVALIDSESSION));
    }

    public CourseList getCourseList() throws ApiSocketException, InvalidSessionApiException {
        return this.getCourseList(0, 10);
    }

    public CourseList getCourseList(final int start, final int limit) throws ApiSocketException,
            InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            final List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
            params.add(Pair.create("start", Integer.toString(start)));
            params.add(Pair.create("limit", Integer.toString(limit)));
            conn = this.apiGetRequest("courses", params, true);

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                try {
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(conn.getInputStream(), "UTF-8");
                    parser.nextTag();
                    return CourseList.fromXml(parser);
                } catch (final XmlPullParserException e) {
                    LOG.error("course list parsing error", e);
                    return null;
                }
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public Course getCourse(final long id) throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            conn = this.apiGetRequest("courses/course/" + Long.toString(id));

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                try {
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(conn.getInputStream(), "UTF-8");
                    parser.nextTag();
                    return Course.fromXml(parser);
                } catch (final XmlPullParserException e) {
                    LOG.error("course list parsing error", e);
                    return null;
                }
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public long streamManifest(final long id, final OutputStream out) throws ApiSocketException,
            InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            conn = this.apiGetRequest("courses/course/" + Long.toString(id) + "/manifest");

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                return IOUtils.copy(conn.getInputStream(), out);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return -1;
    }

    public long streamResource(final Resource resource, final OutputStream out) throws ApiSocketException,
            InvalidSessionApiException {
        return this.streamResource(resource.getCourseId(), resource.getResourceSha1(), out);
    }

    public long streamResource(final long courseId, final String sha1, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            conn = this.apiGetRequest("courses/course/" + Long.toString(courseId) + "/resources/resource/" + sha1);

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                return IOUtils.copy(conn.getInputStream(), out);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return -1;
    }
}
