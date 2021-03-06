package org.ekkoproject.android.player.util;

import java.util.regex.Pattern;

import android.net.Uri;
import android.net.Uri.Builder;

public final class UriUtils {
    public static final Builder removeQueryParams(final Builder uri, final String... keys) {
        if (keys.length > 0) {
            final Uri uriTemp = uri.build();
            String query = uriTemp.getEncodedQuery();
            if (query != null) {
                for (final String key : keys) {
                    // remove all values for key from query
                    final String encodedKey = Uri.encode(key);
                    query = query.replaceAll("&?" + Pattern.quote(encodedKey) + "=[^&]*&?", "&");
                }

                // strip leading/trailing &
                while (query.startsWith("&")) {
                    query = query.substring(1);
                }
                while (query.endsWith("&")) {
                    query = query.substring(0, query.length() - 1);
                }

                // replace query
                uri.encodedQuery(query);
            }
        }

        // return the Builder to allow chaining
        return uri;
    }

    public static final Builder replaceQueryParam(final Builder uri, final String key, final String... values) {
        // remove all values for key from query
        removeQueryParams(uri, key);

        // append all specified values
        for (final String value : values) {
            uri.appendQueryParameter(key, value);
        }

        // return the Builder to allow chaining
        return uri;
    }
}
