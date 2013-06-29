package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.ekkoproject.android.player.Constants.EKKOHUB_URI;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.appdev.entity.Resource;
import org.ccci.gto.android.thekey.TheKey;
import org.ccci.gto.android.thekey.TheKeySocketException;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.CourseList;
import org.ekkoproject.android.player.util.IOUtils;
import org.ekkoproject.android.player.util.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.util.Xml;

public final class EkkoHubApi {
    private static final Logger LOG = LoggerFactory.getLogger(EkkoHubApi.class);

    /** Broadcast actions */
    public static final String ACTION_ERROR_CONNECTION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_CONNECTION";
    public static final String ACTION_ERROR_INVALIDSESSION = "org.ekkoproject.android.player.api.EkkoHubApi.ERROR_INVALIDSESSION";

    private static final Object LOCK_SESSION = new Object();

    private static final String PREFFILE_EKKOHUB = "ekkoHubApi";
    private static final String PREF_SESSIONID = "session_id";
    private static final String PREF_SESSIONGUID = "session_guid";

    private final Context context;
    private final TheKey thekey;
    private final Uri hubUri;

    public EkkoHubApi(final Context context) {
        this(context, EKKOHUB_URI);
    }

    public EkkoHubApi(final Context context, final String hubUri) {
        this(context, Uri.parse(hubUri.endsWith("/") ? hubUri : hubUri + "/"));
    }

    public EkkoHubApi(final Context context, final Uri hubUri) {
        this.context = context;
        this.thekey = new TheKey(this.context, THEKEY_CLIENTID);
        this.hubUri = hubUri;
    }

    public static void broadcastConnectionError(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_ERROR_CONNECTION));
    }

    public static void broadcastInvalidSession(final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent().setAction(ACTION_ERROR_INVALIDSESSION));
    }

    private SharedPreferences getPrefs() {
        return this.context.getSharedPreferences(PREFFILE_EKKOHUB, Context.MODE_PRIVATE);
    }

    private String getSessionId() {
        synchronized (LOCK_SESSION) {
            return this.getPrefs().getString(PREF_SESSIONID, null);
        }
    }

    private String getSessionGuid() {
        synchronized (LOCK_SESSION) {
            return this.getPrefs().getString(PREF_SESSIONGUID, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void setSessionId(final String sessionId) {
        synchronized (LOCK_SESSION) {
            final Editor prefs = this.getPrefs().edit();
            prefs.putString(PREF_SESSIONID, sessionId);
            prefs.putString(PREF_SESSIONGUID, this.thekey.getGuid());

            // store updates
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                prefs.apply();
            } else {
                prefs.commit();
            }
        }
    }

    private void establishSession() throws ApiSocketException {
        synchronized (LOCK_SESSION) {
            try {
                // get the service to retrieve a ticket for
                final String service = this.getService();

                // get a ticket for the specified service
                final String ticket = this.thekey.getTicket(service);

                // login to the hub
                this.setSessionId(this.login(ticket));
            } catch (TheKeySocketException e) {
                throw new ApiSocketException(e);
            }
        }
    }

    private HttpURLConnection apiGetRequest(final String path) throws ApiSocketException, InvalidSessionApiException {
        return this.apiGetRequest(path, Collections.<Pair<String, String>> emptyList(), false);
    }

    private HttpURLConnection apiGetRequest(final boolean useSession, final String path) throws ApiSocketException,
            InvalidSessionApiException {
        return this.apiGetRequest(useSession, path, Collections.<Pair<String, String>> emptyList(), false);
    }

    private HttpURLConnection apiGetRequest(final String path, final Collection<Pair<String, String>> params,
            final boolean replaceParams) throws ApiSocketException, InvalidSessionApiException {
        return this.apiGetRequest(true, path, params, replaceParams);
    }

    private HttpURLConnection apiGetRequest(final boolean useSession, final String path,
            final Collection<Pair<String, String>> params, final boolean replaceParams) throws ApiSocketException,
            InvalidSessionApiException {
        return this.apiGetRequest(useSession, path, params, replaceParams, 3);
    }

    private HttpURLConnection apiGetRequest(final boolean useSession, final String path,
            final Collection<Pair<String, String>> params, final boolean replaceParams, final int attempts)
            throws ApiSocketException, InvalidSessionApiException {
        try {
            try {
                // build the request uri
                String sessionId = null;
                final Builder uri = this.hubUri.buildUpon();
                if (useSession) {
                    // get the session, establish a session if one doesn't exist
                    synchronized (LOCK_SESSION) {
                        sessionId = this.getSessionId();
                        final String guid = this.getSessionGuid();
                        if (sessionId == null || guid == null || !guid.equals(this.thekey.getGuid())) {
                            this.establishSession();
                            sessionId = this.getSessionId();
                        }
                    }

                    // use the current sessionId in the url
                    uri.appendPath(sessionId);
                }
                uri.appendEncodedPath(path);
                if (params.size() > 0) {
                    if (replaceParams) {
                        final List<String> keys = new ArrayList<String>();
                        for (final Pair<String, String> param : params) {
                            keys.add(param.first);
                        }
                        UriUtils.removeQueryParams(uri, keys.toArray(new String[keys.size()]));
                    }
                    for (final Pair<String, String> param : params) {
                        uri.appendQueryParameter(param.first, param.second);
                    }
                }

                // open the connection
                final HttpURLConnection conn = (HttpURLConnection) new URL(uri.build().toString()).openConnection();
                conn.setInstanceFollowRedirects(false);

                // check for an expired session
                if (useSession && conn.getResponseCode() == HTTP_UNAUTHORIZED) {
                    // determine the type of auth requested
                    String auth = conn.getHeaderField("WWW-Authenticate");
                    if (auth != null) {
                        auth = auth.trim();
                        int i = auth.indexOf(" ");
                        if (i != -1) {
                            auth = auth.substring(0, i);
                        }
                        auth = auth.toUpperCase(Locale.US);
                    } else {
                        // there isn't an auth header, so assume it is the CAS
                        // scheme
                        auth = "CAS";
                    }

                    // the 401 is requesting CAS auth, assume session is invalid
                    if ("CAS".equals(auth)) {
                        // reset the session
                        synchronized (LOCK_SESSION) {
                            // only reset if this is still the same session
                            if (sessionId.equals(this.getSessionId())) {
                                this.setSessionId(null);
                            }
                        }

                        // throw an invalid session exception
                        throw new InvalidSessionApiException();
                    }
                }

                // return the connection for method specific handling
                return conn;
            } catch (final MalformedURLException e) {
                throw new RuntimeException("unexpected exception", e);
            } catch (final IOException e) {
                throw new ApiSocketException(e);
            }
        } catch (final InvalidSessionApiException e) {
            // retry request on invalid session exceptions
            if (attempts > 0) {
                return this.apiGetRequest(useSession, path, params, replaceParams, attempts - 1);
            }

            // propagate the exception
            throw e;
        } catch (final ApiSocketException e) {
            // retry request on socket exceptions (maybe spotty internet)
            if (attempts > 0) {
                return this.apiGetRequest(useSession, path, params, replaceParams, attempts - 1);
            }

            // propagate the exception
            throw e;
        }
    }

    public String getService() throws ApiSocketException {
        HttpURLConnection conn = null;
        try {
            conn = this.apiGetRequest(false, "auth/service");

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                return IOUtils.readString(conn.getInputStream());
            }
        } catch (final InvalidSessionApiException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }

        return null;
    }

    public String login(final String ticket) throws ApiSocketException {
        // don't attempt to login if we don't have a ticket
        if (ticket == null) {
            return null;
        }

        HttpURLConnection conn = null;
        try {
            // issue login request
            final String uri = this.hubUri.buildUpon().appendEncodedPath("auth/login").build().toString();
            conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            final byte[] data = ("ticket=" + URLEncoder.encode(ticket, "UTF-8")).getBytes("UTF-8");
            conn.setFixedLengthStreamingMode(data.length);
            conn.getOutputStream().write(data);

            // was this a valid login
            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                // the sessionId is returned as the body of the response
                return IOUtils.readString(conn.getInputStream());
            }

            return null;
        } catch (final MalformedURLException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            IOUtils.closeQuietly(conn);
        }
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
