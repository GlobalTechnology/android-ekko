package org.ekkoproject.android.player.util;

import static org.appdev.entity.Resource.PROVIDER_UNKNOWN;
import static org.appdev.entity.Resource.PROVIDER_VIMEO;
import static org.appdev.entity.Resource.PROVIDER_YOUTUBE;

import java.util.Locale;

import org.appdev.entity.Resource;

import android.content.Intent;
import android.net.Uri;

public final class ResourceUtils {
    private static final Uri URI_VIMEO_BASE = Uri.parse("http://player.vimeo.com/video");

    public static final Intent providerIntent(final Resource resource) {
        if (resource != null) {
            switch (resource.getProvider()) {
            case PROVIDER_VIMEO:
                return new Intent(Intent.ACTION_VIEW, convertToVimeoPlayerUri(Uri.parse(resource.getUri())));
            case PROVIDER_YOUTUBE:
            case PROVIDER_UNKNOWN:
                return new Intent(Intent.ACTION_VIEW, Uri.parse(resource.getUri()));
            }
        }
        return null;
    }

    private static final Uri convertToVimeoPlayerUri(final Uri orig) {
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
}
