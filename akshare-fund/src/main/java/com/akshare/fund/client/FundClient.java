package com.akshare.fund.client;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.http.AkShareHttpClient;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.akshare.fund.service.FundRealtimeService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Fund Client - Main entry point for fund data API
 * Provides unified access to all fund-related data
 */
@Slf4j
public class FundClient {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;

    private final FundRealtimeService realtimeService;

    public FundClient() {
        this.httpClient = new AkShareHttpClient();
        this.jsonParser = new JsonParser();
        this.cacheManager = new CacheManager(true, Duration.ofMinutes(10), 500);

        this.realtimeService = new FundRealtimeService(httpClient, jsonParser, cacheManager);

        log.info("FundClient initialized");
    }

    /**
     * Get open fund daily net value from East Money
     *
     * @return DataFrame with fund daily data
     */
    public DataFrame fundEmOpenFundDaily() {
        return realtimeService.getFundEmOpenFundDaily();
    }

    /**
     * Get fund name list
     *
     * @return DataFrame with fund names
     */
    public DataFrame fundEmFundName() {
        return realtimeService.getFundEmFundName();
    }

    /**
     * Get ETF real-time spot data from East Money
     *
     * @return DataFrame with ETF data
     */
    public DataFrame fundEtfSpotEm() {
        return realtimeService.getFundEtfSpotEm();
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
