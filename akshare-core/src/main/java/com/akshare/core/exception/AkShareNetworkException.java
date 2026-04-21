package com.akshare.core.exception;

/**
 * Exception thrown when network request fails
 * Includes connection timeout, read timeout, DNS failure, etc.
 */
public class AkShareNetworkException extends AkShareException {

    private final String url;
    private final int retryCount;

    public AkShareNetworkException(String url, String message) {
        super("NETWORK_ERROR", String.format("Network error for URL [%s]: %s", url, message));
        this.url = url;
        this.retryCount = 0;
    }

    public AkShareNetworkException(String url, String message, Throwable cause) {
        super("NETWORK_ERROR", String.format("Network error for URL [%s]: %s", url, message), cause);
        this.url = url;
        this.retryCount = 0;
    }

    public AkShareNetworkException(String url, String message, int retryCount, Throwable cause) {
        super("NETWORK_ERROR", String.format("Network error for URL [%s] after %d retries: %s", url, retryCount, message), cause);
        this.url = url;
        this.retryCount = retryCount;
    }

    public String getUrl() {
        return url;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
