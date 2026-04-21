package com.akshare.macro.client;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.http.AkShareHttpClient;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.akshare.macro.service.MacroDataService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Macro Client - Main entry point for macroeconomic data API
 * Provides unified access to all macro-related data
 */
@Slf4j
public class MacroClient {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;

    private final MacroDataService macroDataService;

    public MacroClient() {
        this.httpClient = new AkShareHttpClient();
        this.jsonParser = new JsonParser();
        this.cacheManager = new CacheManager(true, Duration.ofHours(1), 200);

        this.macroDataService = new MacroDataService(httpClient, jsonParser, cacheManager);

        log.info("MacroClient initialized");
    }

    /**
     * Get China GDP data
     *
     * @return DataFrame with GDP data
     */
    public DataFrame macroChinaGdp() {
        return macroDataService.getMacroChinaGdp();
    }

    /**
     * Get China CPI data
     *
     * @return DataFrame with CPI data
     */
    public DataFrame macroChinaCpi() {
        return macroDataService.getMacroChinaCpi();
    }

    /**
     * Get China PPI data
     *
     * @return DataFrame with PPI data
     */
    public DataFrame macroChinaPpi() {
        return macroDataService.getMacroChinaPpi();
    }

    /**
     * Get China PMI data
     *
     * @return DataFrame with PMI data
     */
    public DataFrame macroChinaPmi() {
        return macroDataService.getMacroChinaPmi();
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
