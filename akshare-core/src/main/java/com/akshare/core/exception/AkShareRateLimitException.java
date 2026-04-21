package com.akshare.core.exception;

import java.time.Duration;
import java.time.Instant;

/**
 * Exception thrown when rate limit is exceeded
 * Includes API rate limit, IP ban, etc.
 */
public class AkShareRateLimitException extends AkShareException {

    private final String apiName;
    private final Instant retryAfter;
    private final Duration waitTime;

    public AkShareRateLimitException(String apiName, String message) {
        super("RATE_LIMIT_ERROR", String.format("Rate limit exceeded for API [%s]: %s", apiName, message));
        this.apiName = apiName;
        this.retryAfter = null;
        this.waitTime = null;
    }

    public AkShareRateLimitException(String apiName, Duration waitTime) {
        super("RATE_LIMIT_ERROR", String.format("Rate limit exceeded for API [%s]. Please wait %d seconds before retry.", apiName, waitTime.getSeconds()));
        this.apiName = apiName;
        this.waitTime = waitTime;
        this.retryAfter = Instant.now().plus(waitTime);
    }

    public AkShareRateLimitException(String apiName, Instant retryAfter) {
        super("RATE_LIMIT_ERROR", String.format("Rate limit exceeded for API [%s]. Retry after: %s", apiName, retryAfter));
        this.apiName = apiName;
        this.retryAfter = retryAfter;
        this.waitTime = Duration.between(Instant.now(), retryAfter);
    }

    public String getApiName() {
        return apiName;
    }

    public Instant getRetryAfter() {
        return retryAfter;
    }

    public Duration getWaitTime() {
        return waitTime;
    }
}
