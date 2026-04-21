package com.akshare.core.http;

import com.akshare.core.exception.AkShareNetworkException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client implementation based on OkHttp
 * Features:
 * - Automatic retry with exponential backoff
 * - User-Agent rotation
 * - Cookie persistence
 * - Proxy support
 */
@Slf4j
public class AkShareHttpClient implements HttpClient {

    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0"
    };

    private OkHttpClient client;
    private final CookieJar cookieJar;
    private final Random random;

    private int timeoutMs = 30000;
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
    private String proxyHost = null;
    private int proxyPort = 0;

    public AkShareHttpClient() {
        this.cookieJar = new PersistentCookieJar();
        this.random = new Random();
        buildClient();
    }

    private void buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .cookieJar(cookieJar)
                .followRedirects(true)
                .followSslRedirects(true);

        if (proxyHost != null && proxyPort > 0) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            log.info("Using proxy: {}:{}", proxyHost, proxyPort);
        }

        this.client = builder.build();
    }

    @Override
    public String get(String url, Map<String, String> params, Map<String, String> headers) {
        // Build URL with query parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        // Build request - 不设置 Accept-Encoding，让 OkHttp 自动处理 GZIP 压缩
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", getRandomUserAgent())
                .header("Accept", "application/json, text/html, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Connection", "keep-alive");

        // Add custom headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        return executeWithRetry(request);
    }

    @Override
    public String post(String url, String body, Map<String, String> headers) {
        MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = body != null ?
                RequestBody.create(jsonMediaType, body) :
                RequestBody.create(jsonMediaType, "");

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("User-Agent", getRandomUserAgent())
                .header("Accept", "application/json, text/html, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Content-Type", "application/json; charset=utf-8");

        // Add custom headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        return executeWithRetry(request);
    }

    @Override
    public void setTimeout(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        buildClient();
    }

    @Override
    public void setProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        buildClient();
    }

    @Override
    public void setRetryConfig(int maxRetries, long retryDelayMs) {
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public void clearCookies() {
        if (cookieJar instanceof PersistentCookieJar) {
            ((PersistentCookieJar) cookieJar).clear();
        }
    }

    private String executeWithRetry(Request request) {
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    long delay = retryDelayMs * (1L << (attempt - 1)); // Exponential backoff
                    log.debug("Retry attempt {} after {} ms", attempt, delay);
                    Thread.sleep(delay);
                }

                log.debug("Executing request: {} {}", request.method(), request.url());

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 429) {
                            throw new com.akshare.core.exception.AkShareRateLimitException(
                                    request.url().toString(),
                                    "Rate limit exceeded (HTTP 429)"
                            );
                        }
                        throw new IOException("Unexpected response code: " + response.code());
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        return "";
                    }

                    String result = body.string();
                    log.debug("Response received, length: {} bytes", result.length());
                    return result;
                }

            } catch (IOException e) {
                lastException = e;
                log.warn("Request failed (attempt {}): {}", attempt + 1, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AkShareNetworkException(request.url().toString(), "Request interrupted", e);
            }
        }

        throw new AkShareNetworkException(
                request.url().toString(),
                "Request failed after " + maxRetries + " retries",
                maxRetries,
                lastException
        );
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    /**
     * Simple cookie jar implementation
     */
    private static class PersistentCookieJar implements CookieJar {
        private final java.util.concurrent.ConcurrentHashMap<String, java.util.List<Cookie>> cookieStore =
                new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, java.util.List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public java.util.List<Cookie> loadForRequest(HttpUrl url) {
            java.util.List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new java.util.ArrayList<>();
        }

        public void clear() {
            cookieStore.clear();
        }
    }
}
