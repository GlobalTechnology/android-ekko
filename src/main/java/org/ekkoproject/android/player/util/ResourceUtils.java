package org.ekkoproject.android.player.util;

import static org.appdev.entity.Resource.PROVIDER_UNKNOWN;
import static org.appdev.entity.Resource.PROVIDER_VIMEO;
import static org.appdev.entity.Resource.PROVIDER_YOUTUBE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.android.youtube.player.YouTubeStandalonePlayer;

import org.appdev.entity.Resource;
import org.ekkoproject.android.player.R;

import java.util.List;
import java.util.Locale;

public final class ResourceUtils {
    private static final Uri URI_VIMEO_BASE = Uri.parse("http://player.vimeo.com/video");

    public static Intent providerIntent(final Activity activity, final Resource resource) {
        if (resource != null) {
            final Uri uri = Uri.parse(resource.getUri());
            switch (resource.getProvider()) {
                case PROVIDER_VIMEO:
                    return new Intent(Intent.ACTION_VIEW, convertToVimeoPlayerUri(uri));
                case PROVIDER_YOUTUBE:
                    final String videoId = youtubeExtractVideoId(uri);
                    if (videoId != null) {
                        return YouTubeStandalonePlayer.createVideoIntent(activity,
                                                                         activity.getString(R.string.youTubeApiKey),
                                                                         videoId, 0, true, false);
                    }
                case PROVIDER_UNKNOWN:
                    return new Intent(Intent.ACTION_VIEW, uri);
            }
        }
        return null;
    }

    private static Uri convertToVimeoPlayerUri(final Uri orig) {
        try {
            switch (orig.getScheme().toLowerCase(Locale.US)) {
                case "http":
                case "https":
                    switch (orig.getHost().toLowerCase(Locale.US)) {
                        case "vimeo.com":
                        case "www.vimeo.com":
                            final String path = orig.getEncodedPath();
                            return URI_VIMEO_BASE.buildUpon().appendEncodedPath(
                                    path.startsWith("/") ? path.substring(1) : path).build();
                    }
            }
        } catch (final Exception ignored) {
        }

        return orig;
    }

    public static String youtubeExtractVideoId(final Uri uri) {
        try {
            assert uri != null;
            assert uri.getScheme() != null;
            assert uri.getHost() != null;
            assert uri.getPathSegments() != null;

            switch (uri.getScheme().toLowerCase(Locale.US)) {
                case "http":
                case "https":
                    final List<String> path = uri.getPathSegments();
                    switch (uri.getHost().toLowerCase(Locale.US)) {
                        case "www.youtube.com":
                        case "youtube.com":
                        case "m.youtube.com":
                        case "youtube.googleapis.com":
                            switch (path.get(0).toLowerCase(Locale.US)) {
                                case "watch":
                                case "ytscreeningroom":
                                    if (path.size() == 1) {
                                        return uri.getQueryParameter("v");
                                    }
                                    break;
                                case "embed":
                                case "v":
                                    if (path.size() == 2) {
                                        return path.get(1);
                                    }
                                    break;
                            }
                            break;
                        case "youtu.be":
                            if (path.size() == 1) {
                                return path.get(0);
                            }
                            break;
                    }
                }
        } catch (final Exception ignored) {
        }

        return null;
    }
}
