package com.akshare.core.http;

import java.util.Map;

/**
 * HTTP Client interface for AKShare
 * Provides basic HTTP operations
 */
public interface HttpClient {

    /**
     * Execute HTTP GET request
     *
     * @param url request URL
     * @param params query parameters
     * @param headers request headers
     * @return response body as string
     */
    String get(String url, Map<String, String> params, Map<String, String> headers);

    /**
     * Execute HTTP POST request
     *
     * @param url request URL
     * @param body request body
     * @param headers request headers
     * @return response body as string
     */
    String post(String url, String body, Map<String, String> headers);

    /**
     * Set request timeout
     *
     * @param timeoutMs timeout in milliseconds
     */
    void setTimeout(int timeoutMs);

    /**
     * Set proxy configuration
     *
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    void setProxy(String proxyHost, int proxyPort);

    /**
     * Set retry configuration
     *
     * @param maxRetries maximum number of retries
     * @param retryDelayMs delay between retries in milliseconds
     */
    void setRetryConfig(int maxRetries, long retryDelayMs);

    /**
     * Clear cookies
     */
    void clearCookies();
}
