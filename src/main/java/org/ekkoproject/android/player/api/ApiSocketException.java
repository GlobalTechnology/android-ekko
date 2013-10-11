package org.ekkoproject.android.player.api;

public class ApiSocketException extends ApiException {
    private static final long serialVersionUID = 1L;

    public ApiSocketException() {
        super();
    }

    public ApiSocketException(final Throwable throwable) {
        super(throwable);
    }
}
