package org.ekkoproject.android.player.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public final class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int EOF = -1;

    public static final void closeQuietly(final Closeable handle) {
        if (handle != null) {
            try {
                handle.close();
            } catch (final IOException e) {
                // suppress the error
            }
        }
    }

    public static final long copy(final InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (EOF != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static final String readString(final InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), DEFAULT_BUFFER_SIZE);
        final StringBuilder out = new StringBuilder();
        final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while (EOF != (n = reader.read(buffer, 0, buffer.length))) {
            out.append(buffer, 0, n);
        }
        return out.toString();
    }
}