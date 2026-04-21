package com.akshare.futures.client;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.http.AkShareHttpClient;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.akshare.futures.service.FuturesRealtimeService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Futures Client - Main entry point for futures data API
 * Provides unified access to all futures-related data
 */
@Slf4j
public class FuturesClient {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;

    private final FuturesRealtimeService realtimeService;

    public FuturesClient() {
        this.httpClient = new AkShareHttpClient();
        this.jsonParser = new JsonParser();
        this.cacheManager = new CacheManager(true, Duration.ofMinutes(2), 500);

        this.realtimeService = new FuturesRealtimeService(httpClient, jsonParser, cacheManager);

        log.info("FuturesClient initialized");
    }

    /**
     * Get domestic futures real-time spot data
     *
     * @return DataFrame with futures real-time data
     */
    public DataFrame futuresZhSpot() {
        return realtimeService.getFuturesZhSpot();
    }

    /**
     * Get domestic futures daily K-line data
     *
     * @param symbol futures code, e.g., "RB0" for rebar continuous
     * @return DataFrame with futures daily data
     */
    public DataFrame futuresZhDaily(String symbol) {
        return realtimeService.getFuturesZhDaily(symbol);
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
