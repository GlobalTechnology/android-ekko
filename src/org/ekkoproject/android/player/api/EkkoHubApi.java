package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.ekkoproject.android.player.Constants.EKKOHUB_URI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.ccci.gto.android.thekey.TheKey;
import org.ccci.gto.android.thekey.TheKeySocketException;

import android.net.Uri;
import android.net.Uri.Builder;

public final class EkkoHubApi {
    private final TheKey thekey;
    private final Uri hubUri;

    private String sessionId = null;

    public EkkoHubApi(final TheKey thekey) {
        this(thekey, EKKOHUB_URI);
    }

    public EkkoHubApi(final TheKey thekey, final String hubUri) {
        this(thekey, hubUri, null);
    }

    public EkkoHubApi(final TheKey thekey, final Uri hubUri) {
        this(thekey, hubUri, null);
    }

    public EkkoHubApi(final TheKey thekey, final String hubUri, final String sessionId) {
        this(thekey, Uri.parse(hubUri.endsWith("/") ? hubUri : hubUri + "/"), sessionId);
    }

    public EkkoHubApi(final TheKey thekey, final Uri hubUri, final String sessionId) {
        this.thekey = thekey;
        this.hubUri = hubUri;
        this.sessionId = sessionId;
    }

    public synchronized String getSessionId() {
        return this.sessionId;
    }

    private synchronized void establishSession() throws ApiSocketException {
        try {
            // get the service to retrieve a ticket for
            final String service = this.getService();

            // get a ticket for the specified service
            final String ticket = this.thekey.getTicket(service);

            // login to the hub
            this.sessionId = this.login(ticket);
        } catch (TheKeySocketException e) {
            throw new ApiSocketException(e);
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
                    synchronized (this) {
                        if (this.sessionId == null) {
                            this.establishSession();
                        }
                        sessionId = this.sessionId;
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
                    synchronized (this) {
                        // only reset if this is still the same session
                        if (sessionId.equals(this.sessionId)) {
                            this.sessionId = null;
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
