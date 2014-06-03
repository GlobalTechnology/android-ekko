package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.util.Xml;

import org.ccci.gto.android.common.api.AbstractGtoSmxApi;
import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.util.IOUtils;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.services.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import me.thekey.android.lib.TheKeyImpl;

public final class EkkoHubApi extends AbstractGtoSmxApi {
    private static final Logger LOG = LoggerFactory.getLogger(EkkoHubApi.class);

    private static final String HEADER_STREAM_URI = "X-Ekko-Stream-Uri";

    /** Broadcast actions */
    public static final String ACTION_ERROR_CONNECTION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_CONNECTION";
    public static final String ACTION_ERROR_INVALIDSESSION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_INVALIDSESSION";

    private static final String PREFFILE_EKKOHUB = "ekkoHubApi";

    private static final Object LOCK_INSTANCE = new Object();
    private static final Map<String, EkkoHubApi> instances = new HashMap<>();

    private EkkoHubApi(final Context context, final String guid) {
        super(context, TheKeyImpl.getInstance(context, THEKEY_CLIENTID), PREFFILE_EKKOHUB, R.string.ekkoSmxUri, guid);
        this.setIncludeAppVersion(true);
        this.setAllowGuest(true);
    }

    public static EkkoHubApi getInstance(final Context context) {
        return EkkoHubApi.getInstance(context, null);
    }

    public static EkkoHubApi getInstance(final Context context, final String guid) {
        synchronized (LOCK_INSTANCE) {
            if(!instances.containsKey(guid)) {
                instances.put(guid, new EkkoHubApi(context.getApplicationContext(), guid));
            }
        }

        return instances.get(guid);
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
            final Request request = new Request("courses");
            request.replaceParams = true;
            request.params.add(Pair.create("start", Integer.toString(start)));
            request.params.add(Pair.create("limit", Integer.toString(limit)));
            request.accept = Request.MediaType.APPLICATION_XML;
            conn = this.sendRequest(request);

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
            final Request request = new Request("courses/course/" + Long.toString(id));
            request.accept = Request.MediaType.APPLICATION_XML;
            conn = this.sendRequest(request);

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

    public Course enroll(final long id) throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            final Request request = new Request("courses/course/" + Long.toString(id) + "/enroll");
            request.method = "POST";
            request.accept = Request.MediaType.APPLICATION_XML;
            conn = this.sendRequest(request);

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

    public boolean unenroll(final long id) throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            final Request request = new Request("courses/course/" + Long.toString(id) + "/unenroll");
            request.method = "POST";
            conn = this.sendRequest(request);

            if(conn != null && conn.getResponseCode() == HTTP_OK) {
                return true;
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return false;
    }

    public long downloadManifest(final long id, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            final Request request = new Request("courses/course/" + Long.toString(id) + "/manifest");
            request.accept = Request.MediaType.APPLICATION_XML;
            conn = this.sendRequest(request);

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

    public long downloadResource(final Resource resource, final boolean thumbnail, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        if (resource != null) {
            if (resource.isFile()) {
                return this.downloadFileResource(resource, out);
            } else if (resource.isEcv()) {
                return this.downloadEcvResource(resource, thumbnail, out);
            }
        }

        return -1;
    }

    public long downloadFileResource(final Resource resource, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        return resource != null && resource.isFile() ?
                this.downloadFileResource(resource.getCourseId(), resource.getResourceSha1(), out) : -1;
    }

    private long downloadFileResource(final long courseId, final String sha1, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = this.sendRequest(
                    new Request("courses/course/" + Long.toString(courseId) + "/resources/resource/" + sha1));

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                in = conn.getInputStream();
                return IOUtils.copy(in, out);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(conn);
        }

        return -1;
    }

    private Request ecvResourceRequest(final Resource resource, final String type) {
        if (resource != null && resource.isEcv()) {
            return new Request("courses/course/" + Long.toString(resource.getCourseId()) + "/resources/video/" +
                                       Long.toString(resource.getVideoId()) + "/" + type);
        }
        return null;
    }

    public long downloadEcvResource(final Resource resource, final boolean thumbnail, final OutputStream out)
            throws ApiSocketException, InvalidSessionApiException {
        // generate request
        final Request request = ecvResourceRequest(resource, (thumbnail ? "thumbnail" : "download"));
        if (request == null) {
            return -1;
        }
        request.followRedirects = true;

        // process request
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = this.sendRequest(request);

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                in = conn.getInputStream();
                return IOUtils.copy(in, out);
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(conn);
        }

        return -1;
    }

    public Uri getEcvStreamUri(final Resource resource, final ResourceManager.StreamType type)
            throws ApiSocketException, InvalidSessionApiException {
        // determine what stream format to use
        final String path;
        final String format;
        switch (type) {
            case MP4:
                path = "stream";
                format = "MP4";
                break;
            case SINGLE_HLS:
                path = "stream.m3u8";
                format = "HLS_1M";
                break;
            case HLS:
            default:
                path = "stream.m3u8";
                format = "HLS";
                break;
        }

        // generate request
        final Request request = ecvResourceRequest(resource, path);
        if (request == null) {
            return null;
        }
        request.params.add(Pair.create("type", format));

        // process request
        HttpURLConnection conn = null;
        try {
            conn = this.sendRequest(request);

            if (conn != null) {
                final int code = conn.getResponseCode();
                if (code == HTTP_OK || (code >= 300 && code < 400)) {
                    final String streamUri = conn.getHeaderField(HEADER_STREAM_URI);
                    if (streamUri != null) {
                        final Uri uri = Uri.parse(streamUri);
                        if (uri.isAbsolute()) {
                            LOG.debug("returning stream uri {}", uri);

                            return uri;
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }
}
