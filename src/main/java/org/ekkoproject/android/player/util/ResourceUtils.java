package org.ekkoproject.android.player.util;

import static org.appdev.entity.Resource.PROVIDER_UNKNOWN;
import static org.appdev.entity.Resource.PROVIDER_VIMEO;
import static org.appdev.entity.Resource.PROVIDER_YOUTUBE;

import android.content.Intent;
import android.net.Uri;

import org.appdev.entity.Resource;

import java.util.List;
import java.util.Locale;

public final class ResourceUtils {
    private static final Uri URI_VIMEO_BASE = Uri.parse("http://player.vimeo.com/video");

    public static Intent providerIntent(final Resource resource) {
        if (resource != null) {
            final Uri uri = Uri.parse(resource.getUri());
            switch (resource.getProvider()) {
                case PROVIDER_VIMEO:
                    return new Intent(Intent.ACTION_VIEW, convertToVimeoPlayerUri(uri));
                case PROVIDER_YOUTUBE:
                case PROVIDER_UNKNOWN:
                    return new Intent(Intent.ACTION_VIEW, uri);
            }
        }
        return null;
    }

    private static Uri convertToVimeoPlayerUri(final Uri orig) {
        try {
            final String scheme = orig.getScheme().toLowerCase(Locale.US);
            if (scheme.equals("http") || scheme.equals("https")) {
                final String host = orig.getHost().toLowerCase(Locale.US);
                if (host.equals("vimeo.com") || host.equals("www.vimeo.com")) {
                    final String path = orig.getEncodedPath();
                    return URI_VIMEO_BASE.buildUpon()
                            .appendEncodedPath(path.startsWith("/") ? path.substring(1) : path).build();
                }
            }
        } catch (final Exception e) {
        }

        return orig;
    }

    public static String youtubeExtractVideoId(final Uri uri) {
        if (uri != null) {
            final String scheme = uri.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme)) {
                final String host = uri.getHost();
                final List<String> path = uri.getPathSegments();
                assert path != null;
                if ("www.youtube.com".equals(host) || "youtube.com".equals(host) || "m.youtube.com".equals(host) ||
                        "youtube.googleapis.com".equals(host)) {
                    switch (path.size()) {
                        case 1:
                            if ("watch".equals(path.get(0)) || "ytscreeningroom".equals(path.get(0))) {
                                return uri.getQueryParameter("v");
                            }
                            break;
                        case 2:
                            if ("embed".equals(path.get(0)) || "v".equals(path.get(0))) {
                                return path.get(1);
                            }
                            break;
                    }
                } else if ("youtu.be".equals(host)) {
                    if (path.size() == 1) {
                        return path.get(0);
                    }
                }
            }
        }

        return null;
    }
}
