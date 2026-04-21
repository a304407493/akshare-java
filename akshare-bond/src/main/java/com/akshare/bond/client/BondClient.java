package com.akshare.bond.client;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.http.AkShareHttpClient;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.akshare.bond.service.BondRealtimeService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Bond Client - Main entry point for bond data API
 * Provides unified access to all bond-related data
 */
@Slf4j
public class BondClient {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;

    private final BondRealtimeService realtimeService;

    public BondClient() {
        this.httpClient = new AkShareHttpClient();
        this.jsonParser = new JsonParser();
        this.cacheManager = new CacheManager(true, Duration.ofMinutes(5), 300);

        this.realtimeService = new BondRealtimeService(httpClient, jsonParser, cacheManager);

        log.info("BondClient initialized");
    }

    /**
     * Get convertible bond real-time spot data
     *
     * @return DataFrame with convertible bond data
     */
    public DataFrame bondZhCov() {
        return realtimeService.getBondZhCov();
    }

    /**
     * Set request timeout
     *
     * @param timeoutMs timeout in milliseconds
     */
    public void setTimeout(int timeoutMs) {
        httpClient.setTimeout(timeoutMs);
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        cacheManager.invalidateAll();
    }
}
