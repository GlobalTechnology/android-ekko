package org.ekkoproject.android.player.api;

public class ApiException extends Exception {
    private static final long serialVersionUID = 1L;

    public ApiException() {
        super();
    }

    public ApiException(final Throwable throwable) {
        super(throwable);
    }
}
