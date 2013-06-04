package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.ekkoproject.android.player.Constants.EKKOHUB_URI;
import static org.ekkoproject.android.player.Constants.THEKEY_CLIENTID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.appdev.entity.CourseList;
import org.ccci.gto.android.thekey.TheKey;
import org.ccci.gto.android.thekey.TheKeySocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.util.Xml;

public final class EkkoHubApi {
    private static final Logger LOG = LoggerFactory.getLogger(EkkoHubApi.class);

    private static final Object LOCK_SESSION = new Object();

    private static final String PREFFILE_EKKOHUB = "ekkoHubApi";
    private static final String PREF_SESSIONID = "session_id";

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

    private SharedPreferences getPrefs() {
        return this.context.getSharedPreferences(PREFFILE_EKKOHUB, Context.MODE_PRIVATE);
    }

    private String getSessionId() {
        synchronized (LOCK_SESSION) {
            return this.getPrefs().getString(PREF_SESSIONID, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void setSessionId(final String sessionId) {
        synchronized (LOCK_SESSION) {
            final Editor prefs = this.getPrefs().edit();
            prefs.putString(PREF_SESSIONID, sessionId);

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
        return this.apiGetRequest(true, path);
    }

    private HttpURLConnection apiGetRequest(final boolean useSession, final String path) throws ApiSocketException,
            InvalidSessionApiException {
        return this.apiGetRequest(useSession, path, 3);
    }

    private HttpURLConnection apiGetRequest(final boolean useSession, final String path, final int attempts)
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
                        if (sessionId == null) {
                            this.establishSession();
                            sessionId = this.getSessionId();
                        }
                    }

                    // use the current sessionId in the url
                    uri.appendPath(sessionId);
                }
                uri.appendEncodedPath(path);

                // open the connection
                final HttpURLConnection conn = (HttpURLConnection) new URL(uri.build().toString()).openConnection();
                conn.setInstanceFollowRedirects(false);

                // check for an expired session
                // TODO: find a way to identify an expired session
                if (useSession && conn.getResponseCode() == HTTP_UNAUTHORIZED) {
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
                return this.apiGetRequest(useSession, path, attempts - 1);
            }

            // propagate the exception
            throw e;
        } catch (final ApiSocketException e) {
            // retry request on socket exceptions (maybe spotty internet)
            if (attempts > 0) {
                return this.apiGetRequest(useSession, path, attempts - 1);
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
                return this.toString(conn.getInputStream());
            }
        } catch (final InvalidSessionApiException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            this.closeQuietly(conn);
        }

        return null;
    }

    public String login(final String ticket) throws ApiSocketException {
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
                return this.toString(conn.getInputStream());
            }

            return null;
        } catch (final MalformedURLException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            this.closeQuietly(conn);
        }
    }

    public CourseList getCourseList() throws ApiSocketException, InvalidSessionApiException {
        return this.getCourseList(0, 10);
    }

    public CourseList getCourseList(final int start, final int limit) throws ApiSocketException,
            InvalidSessionApiException {
        HttpURLConnection conn = null;
        try {
            conn = this.apiGetRequest("courses");

            if (conn != null && conn.getResponseCode() == HTTP_OK) {
                try {
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(conn.getInputStream(), "UTF-8");
                    parser.nextTag();
                    return CourseList.parse(parser);
                } catch (final XmlPullParserException e) {
                    LOG.error("course list parsing error", e);
                    return null;
                }
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        } finally {
            this.closeQuietly(conn);
        }

        return null;
    }

    private String toString(final InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        final StringBuilder data = new StringBuilder();
        String buffer;
        while ((buffer = reader.readLine()) != null) {
            data.append(buffer);
        }
        return data.toString();
    }

    private void closeQuietly(final HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }
}
