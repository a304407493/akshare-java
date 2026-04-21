package com.akshare.core.cache;

import com.akshare.core.dataframe.DataFrame;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache Manager based on Caffeine
 * Provides interface-level caching for AKShare data
 */
@Slf4j
public class CacheManager {

    private final Cache<String, DataFrame> cache;
    private final boolean enabled;

    public CacheManager() {
        this(true, Duration.ofMinutes(5), 1000);
    }

    public CacheManager(boolean enabled) {
        this(enabled, Duration.ofMinutes(5), 1000);
    }

    public CacheManager(Duration expireAfterWrite, int maximumSize) {
        this(true, expireAfterWrite, maximumSize);
    }

    public CacheManager(boolean enabled, Duration expireAfterWrite, int maximumSize) {
        this.enabled = enabled;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite)
                .maximumSize(maximumSize)
                .recordStats()
                .build();
    }

    /**
     * Generate cache key from method name and parameters
     *
     * @param methodName method name
     * @param params parameters
     * @return cache key (MD5 hash)
     */
    public String generateKey(String methodName, Object... params) {
        StringBuilder sb = new StringBuilder(methodName);
        if (params != null) {
            for (Object param : params) {
                sb.append(":");
                sb.append(param != null ? param.toString() : "null");
            }
        }
        return md5(sb.toString());
    }

    /**
     * Get data from cache
     *
     * @param key cache key
     * @return cached DataFrame or null
     */
    public DataFrame get(String key) {
        if (!enabled) {
            return null;
        }
        DataFrame result = cache.getIfPresent(key);
        if (result != null) {
            log.debug("Cache hit for key: {}", key);
        }
        return result;
    }

    /**
     * Put data into cache
     *
     * @param key cache key
     * @param data DataFrame to cache
     */
    public void put(String key, DataFrame data) {
        if (!enabled || data == null) {
            return;
        }
        cache.put(key, data);
        log.debug("Cached data for key: {}", key);
    }

    /**
     * Invalidate cache entry
     *
     * @param key cache key
     */
    public void invalidate(String key) {
        cache.invalidate(key);
        log.debug("Invalidated cache for key: {}", key);
    }

    /**
     * Invalidate all cache entries
     */
    public void invalidateAll() {
        cache.invalidateAll();
        log.info("Invalidated all cache entries");
    }

    /**
     * Get cache statistics
     *
     * @return cache stats
     */
    public com.github.benmanes.caffeine.cache.stats.CacheStats getStats() {
        return cache.stats();
    }

    /**
     * Check if cache is enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get cache size
     *
     * @return estimated size
     */
    public long getSize() {
        return cache.estimatedSize();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf(input.hashCode());
        }
    }
}
