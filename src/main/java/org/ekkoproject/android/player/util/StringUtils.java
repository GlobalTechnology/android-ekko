package org.ekkoproject.android.player.util;

public final class StringUtils {
    public static boolean toBool(final Object obj, final boolean defValue) {
        try {
            return Boolean.parseBoolean(obj.toString());
        } catch (final Exception e) {
        }
        return defValue;
    }

    public static int toInt(final Object obj, final int defValue) {
        try {
            return Integer.parseInt(obj.toString());
        } catch (final Exception e) {
        }
        return defValue;
    }

    public static long toLong(final Object obj, final long defValue) {
        try {
            return Long.parseLong(obj.toString());
        } catch (final Exception e) {
        }
        return defValue;
    }

    public static CharSequence trim(final CharSequence source) {
        if (source != null) {
            int start = 0;
            int end = source.length() - 1;

            // find start index
            for (; start <= end && Character.isWhitespace(source.charAt(start)); start++) {
            }

            // find end index
            for (; end >= start && Character.isWhitespace(source.charAt(end)); end--) {
            }

            // handle edge cases
            // no whitespace
            if (start == 0 && end == source.length() - 1) {
                return source;
            }
            // string is only whitespace
            else if (start > end) {
                return "";
            }

            return source.subSequence(start, end + 1);
        }

        return source;
    }
}
