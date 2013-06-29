package org.ekkoproject.android.player.util;

public final class StringUtils {
    private static final char[] HEXCHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    public static String bytesToHex(final byte[] bytes) {
        final char[] hexOut = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexOut[j * 2] = HEXCHAR[v >>> 4];
            hexOut[j * 2 + 1] = HEXCHAR[v & 0x0F];
        }

        return new String(hexOut);
    }

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
