package com.akshare.macro.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Macroeconomic Data Service
 * Provides China's macroeconomic indicators
 */
@Slf4j
public class MacroDataService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    // 新的宏观数据API接口
    private static final String EASTMONEY_GDP_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    private static final String EASTMONEY_CPI_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    private static final String EASTMONEY_PPI_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";

    public MacroDataService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get China GDP data
     *
     * @return DataFrame with GDP data
     */
    public DataFrame getMacroChinaGdp() {
        String cacheKey = cacheManager.generateKey("macroChinaGdp");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从东方财富获取中国GDP数据");

        Map<String, String> params = new HashMap<>();
        params.put("reportName", "RPT_ECONOMY_GDP");
        params.put("columns", "ALL");
        params.put("pageNumber", "1");
        params.put("pageSize", "100");

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://data.eastmoney.com");

        try {
            String response = httpClient.get(EASTMONEY_GDP_URL, params, headers);
            DataFrame df = parseNewMacroData(response, "GDP");
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取GDP数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * Get China CPI data
     *
     * @return DataFrame with CPI data
     */
    public DataFrame getMacroChinaCpi() {
        String cacheKey = cacheManager.generateKey("macroChinaCpi");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从东方财富获取中国CPI数据");

        Map<String, String> params = new HashMap<>();
        params.put("reportName", "RPT_ECONOMY_CPI");
        params.put("columns", "ALL");
        params.put("pageNumber", "1");
        params.put("pageSize", "100");

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://data.eastmoney.com");

        try {
            String response = httpClient.get(EASTMONEY_CPI_URL, params, headers);
            DataFrame df = parseNewMacroData(response, "CPI");
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取CPI数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * Get China PPI data
     *
     * @return DataFrame with PPI data
     */
    public DataFrame getMacroChinaPpi() {
        String cacheKey = cacheManager.generateKey("macroChinaPpi");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从东方财富获取中国PPI数据");

        Map<String, String> params = new HashMap<>();
        params.put("reportName", "RPT_ECONOMY_PPI");
        params.put("columns", "ALL");
        params.put("pageNumber", "1");
        params.put("pageSize", "100");

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://data.eastmoney.com");

        try {
            String response = httpClient.get(EASTMONEY_PPI_URL, params, headers);
            DataFrame df = parseNewMacroData(response, "PPI");
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取PPI数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * Get China PMI data
     *
     * @return DataFrame with PMI data
     */
    public DataFrame getMacroChinaPmi() {
        String cacheKey = cacheManager.generateKey("macroChinaPmi");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从东方财富获取中国PMI数据");

        Map<String, String> params = new HashMap<>();
        params.put("reportName", "RPT_ECONOMY_PMI");
        params.put("columns", "ALL");
        params.put("pageNumber", "1");
        params.put("pageSize", "100");

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://data.eastmoney.com");

        try {
            String response = httpClient.get(EASTMONEY_GDP_URL, params, headers);
            DataFrame df = parseNewMacroData(response, "PMI");
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取PMI数据失败", e);
            return new DataFrameImpl();
        }
    }

    private DataFrame parseNewMacroData(String response, String indicatorType) {
        try {
            if (response == null || response.isEmpty()) {
                log.warn("响应为空");
                return new DataFrameImpl();
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultNode = rootNode.path("result");
            JsonNode dataNode = resultNode.path("data");

            if (!dataNode.isArray()) {
                log.warn("未找到{}数据", indicatorType);
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                switch (indicatorType) {
                    case "GDP":
                        row.put("报告期", getStringValue(item, "TIME"));
                        row.put("国内生产总值", getDoubleValue(item, "DOMESTICL_PRODUCT_BASE"));
                        row.put("第一产业", getDoubleValue(item, "FIRST_PRODUCT_BASE"));
                        row.put("第二产业", getDoubleValue(item, "SECOND_PRODUCT_BASE"));
                        row.put("第三产业", getDoubleValue(item, "THIRD_PRODUCT_BASE"));
                        row.put("GDP同比增长", getDoubleValue(item, "SUM_SAME"));
                        row.put("第一产业同比增长", getDoubleValue(item, "FIRST_SAME"));
                        row.put("第二产业同比增长", getDoubleValue(item, "SECOND_SAME"));
                        row.put("第三产业同比增长", getDoubleValue(item, "THIRD_SAME"));
                        break;
                    case "CPI":
                        row.put("报告期", getStringValue(item, "TIME"));
                        row.put("全国CPI", getDoubleValue(item, "NATIONAL_BASE"));
                        row.put("全国同比", getDoubleValue(item, "NATIONAL_SAME"));
                        row.put("全国环比", getDoubleValue(item, "NATIONAL_SEQUENTIAL"));
                        row.put("城市CPI", getDoubleValue(item, "CITY_BASE"));
                        row.put("城市同比", getDoubleValue(item, "CITY_SAME"));
                        row.put("农村CPI", getDoubleValue(item, "COUNTY_BASE"));
                        row.put("农村同比", getDoubleValue(item, "COUNTY_SAME"));
                        break;
                    case "PPI":
                        row.put("报告期", getStringValue(item, "TIME"));
                        row.put("PPI", getDoubleValue(item, "BASE"));
                        row.put("PPI同比", getDoubleValue(item, "BASE_SAME"));
                        row.put("PPI累计", getDoubleValue(item, "BASE_ACCUMULATE"));
                        break;
                    case "PMI":
                        row.put("报告期", getStringValue(item, "TIME"));
                        row.put("制造业PMI", getDoubleValue(item, "MAKE_INDEX"));
                        row.put("制造业PMI同比", getDoubleValue(item, "MAKE_SAME"));
                        row.put("非制造业PMI", getDoubleValue(item, "NMAKE_INDEX"));
                        row.put("非制造业PMI同比", getDoubleValue(item, "NMAKE_SAME"));
                        break;
                }

                rows.add(row);
            }

            log.info("成功解析{}条{}数据", rows.size(), indicatorType);
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析{}数据失败", indicatorType, e);
            return new DataFrameImpl();
        }
    }

    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null && !valueNode.isNull() ? valueNode.asText() : "";
    }

    private Double getDoubleValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) return null;
        try {
            return valueNode.asDouble();
        } catch (Exception e) {
            return null;
        }
    }
}
