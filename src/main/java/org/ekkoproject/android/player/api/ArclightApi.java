package org.ekkoproject.android.player.api;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.ekkoproject.android.player.BuildConfig.ARCLIGHT_API_KEY;
import static org.ekkoproject.android.player.BuildConfig.ARCLIGHT_API_URI;

import android.net.Uri;
import android.util.Pair;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArclightApi {
    private static final Logger LOG = LoggerFactory.getLogger(ArclightApi.class);

    private static final Uri API_URI = Uri.parse(ARCLIGHT_API_URI);
    private static final int DEFAULT_ATTEMPTS = 3;

    private static final ArclightApi INSTANCE = new ArclightApi();

    private ArclightApi() {
    }

    public static ArclightApi getInstance() {
        return INSTANCE;
    }

    private JSONObject apiRequest(final String service, final List<Pair<String, String>> params)
            throws ApiSocketException {
        return this.apiRequest(service, params, DEFAULT_ATTEMPTS);
    }

    private JSONObject apiRequest(final String service, final List<Pair<String, String>> params, final int attempts)
            throws ApiSocketException {
        try {
            HttpURLConnection conn = null;
            try {
                // generate uri for this request
                final Uri.Builder uri = API_URI.buildUpon();
                uri.appendPath(service);
                uri.appendQueryParameter("apiKey", ARCLIGHT_API_KEY);
                uri.appendQueryParameter("responseType", "JSON");
                for (final Pair<String, String> param : params) {
                    uri.appendQueryParameter(param.first, param.second);
                }

                // build base request object
                conn = (HttpURLConnection) new URL(uri.build().toString()).openConnection();
                conn.setInstanceFollowRedirects(false);

                // no need to explicitly execute, accessing the response triggers the execute

                // was this a valid response
                if (conn.getResponseCode() == HTTP_OK) {
                    try {
                        // parse the response as JSON
                        final BufferedReader reader =
                                new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        final StringBuilder resp = new StringBuilder();
                        String buffer;
                        while ((buffer = reader.readLine()) != null) {
                            resp.append(buffer);
                        }

                        return new JSONObject(resp.toString());
                    } catch (final Exception e) {
                        // parsing error, don't retry the request
                        LOG.error("error parsing response JSON", e);
                    }
                } else {
                    LOG.error("Response code {} for {}", conn.getResponseCode(), conn.getURL());
                }

                return null;
            } catch (final MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (final IOException e) {
                throw new ApiSocketException(e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (final ApiSocketException e) {
            if (attempts > 0) {
                // retry this request
                return this.apiRequest(service, params, attempts - 1);
            }

            throw e;
        }
    }

    public JSONObject getAssetDetails(final String refId, final boolean requestPlayer, final boolean downloadUrls)
            throws ApiSocketException {
        final List<Pair<String, String>> params = new ArrayList<>();
        params.add(Pair.create("refId", refId));
        if (requestPlayer) {
            params.add(Pair.create("requestPlayer", "Android"));
        }
        if (downloadUrls) {
            params.add(Pair.create("getDownloadUrl", "true"));
        }

        try {
            final JSONObject json = this.apiRequest("getAssetDetails", params);
            if (json != null) {
                return json.getJSONArray("assetDetails").getJSONObject(0);
            }
        } catch (final JSONException e) {
            LOG.debug("JSON parsing error", e);
        }

        return null;
    }

    public Uri getAssetStreamUri(final String refId) throws ApiSocketException {
        final JSONObject json = this.getAssetDetails(refId, true, false);

        try {
            final JSONArray renditions = json.getJSONArray("playerCode");
            int bandwidth = 0;
            Uri uri = null;
            for (int i = 0; i < renditions.length(); i++) {
                final JSONObject rendition = renditions.getJSONObject(i).getJSONObject("rendition");
                final int bitrate = rendition.getInt("videoBitrate");
                if (bitrate > bandwidth) {
                    try {
                        uri = Uri.parse(rendition.getString("uri"));
                        bandwidth = bitrate;
                    } catch (final Exception ignored) {
                    }
                }
            }
            return uri;
        } catch (final Exception ignored) {
        }

        return null;
    }

    public long downloadAsset(final String refId, final boolean thumb, final OutputStream out)
            throws ApiSocketException {
        // short-circuit if we fail to retrieve asset details
        final JSONObject json = this.getAssetDetails(refId, false, !thumb);
        if (json == null) {
            return -1;
        }

        try {
            // fetch the url to download
            final List<URL> urls = new ArrayList<>();
            if (thumb) {
                // try using the videoStillUrl first
                try {
                    urls.add(new URL(json.optString("videoStillUrl", null)));
                } catch (final MalformedURLException ignored) {
                }

                // only use box art if we don't have a video still
                if (urls.isEmpty()) {
                    // parse the attached box art
                    final Map<String, URL> boxArt = this.parseUris(json.optJSONArray("boxArtUrls"), "url");

                    // try using box art ordered by quality
                    for (final String quality : new String[] {"Mobile cinematic high", "HD", "Large", "Medium",
                            "Mobile cinematic low", "Small"}) {
                        final URL url = boxArt.get(quality);
                        if (url != null) {
                            urls.add(url);
                            break;
                        }
                    }
                }
            } else {
                // parse the download urls
                final Map<String, URL> downloadUrls = this.parseUris(json.optJSONArray("downloadUrls"), "url");

                // try using download urls in order of quality
                for (final String quality : new String[] {"high", "low"}) {
                    final URL url = downloadUrls.get(quality);
                    if (url != null) {
                        urls.add(url);
                        break;
                    }
                }
            }

            // try downloading first possible URL
            for (final URL url : urls) {
                // skip null URLs
                if (url == null) {
                    continue;
                }

                HttpURLConnection conn = null;
                InputStream in = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(true);

                    // no need to explicitly execute, accessing the response triggers the execute

                    // was this a valid response
                    if (conn.getResponseCode() == HTTP_OK) {
                        in = conn.getInputStream();
                        return IOUtils.copy(in, out);
                    }
                } finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(conn);
                }
            }
        } catch (final IOException e) {
            throw new ApiSocketException(e);
        }

        return -1;
    }

    private Map<String, URL> parseUris(final JSONArray json, final String container) {
        if (json != null && json.length() > 0) {
            final Map<String, URL> urls = new HashMap<>();
            for (int i = 0; i < json.length(); i++) {
                try {
                    final JSONObject url = json.getJSONObject(i).getJSONObject(container);
                    urls.put(url.optString("type", null), new URL(url.optString("uri", null)));
                } catch (final Exception ignored) {
                }
            }

            return Collections.unmodifiableMap(urls);
        }

        return Collections.emptyMap();
    }
}
