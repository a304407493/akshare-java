package com.akshare.bond.service;

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
 * Bond Real-time Data Service
 * Provides convertible bond quotes and data
 */
@Slf4j
public class BondRealtimeService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String EASTMONEY_BOND_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";

    public BondRealtimeService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get convertible bond real-time spot data
     *
     * @return DataFrame with convertible bond data
     */
    public DataFrame getBondZhCov() {
        String cacheKey = cacheManager.generateKey("bondZhCov");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching convertible bond data from East Money");

        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "PUBLIC_START_DATE");
        params.put("sortTypes", "-1");
        params.put("pageSize", "500");
        params.put("pageNumber", "1");
        params.put("reportName", "RPT_BOND_CB_LIST");
        params.put("columns", "ALL");
        params.put("quoteColumns", "f2~01~CONVERT_STOCK_CODE~CONVERT_STOCK_PRICE,f3~01~CONVERT_STOCK_CODE~CONVERT_STOCK_PERCENT");
        params.put("source", "WEB");
        params.put("client", "WEB");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_BOND_URL, params, null);

        DataFrame df = parseBondData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseBondData(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("result").path("data");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("No bond data found");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                row.put("债券代码", getStringValue(item, "BOND_CODE"));
                row.put("债券简称", getStringValue(item, "BOND_NAME"));
                row.put("正股代码", getStringValue(item, "CONVERT_STOCK_CODE"));
                row.put("正股简称", getStringValue(item, "SECURITY_NAME"));
                row.put("转股价值", getDoubleValue(item, "CONVERT_STOCK_PRICE"));
                row.put("转股溢价率", getDoubleValue(item, "CONVERT_STOCK_PERCENT"));
                row.put("最新价", getDoubleValue(item, "BOND_PRICE"));
                row.put("涨跌幅", getDoubleValue(item, "BOND_CHANGE_PERCENT"));
                row.put("转股价", getDoubleValue(item, "CONVERT_PRICE"));
                row.put("转股开始日", getStringValue(item, "CONVERT_START_DATE"));
                row.put("转股结束日", getStringValue(item, "CONVERT_END_DATE"));
                row.put("发行规模", getDoubleValue(item, "ISSUE_SCALE"));
                row.put("剩余规模", getDoubleValue(item, "REMAIN_SCALE"));
                row.put("到期日期", getStringValue(item, "DUE_DATE"));
                row.put("票面利率", getStringValue(item, "COUPON_RATE"));
                row.put("信用评级", getStringValue(item, "RATING"));
                row.put("申购日期", getStringValue(item, "PUBLIC_START_DATE"));
                row.put("上市日期", getStringValue(item, "LIST_DATE"));

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("Failed to parse bond data", e);
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
