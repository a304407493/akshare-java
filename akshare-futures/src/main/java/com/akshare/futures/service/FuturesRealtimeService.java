package com.akshare.futures.service;

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
 * Futures Real-time Data Service
 * Provides futures quotes and historical data
 */
@Slf4j
public class FuturesRealtimeService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String SINA_FUTURES_URL = "https://hq.sinajs.cn/list=";
    private static final String EASTMONEY_FUTURES_URL = "https://push2.eastmoney.com/api/qt/clist/get";

    // Common futures symbols
    private static final String[] COMMON_FUTURES = {
        // Commodity futures
        "RB0",   // Rebar
        "HC0",   // Hot-rolled coil
        "I0",    // Iron ore
        "J0",    // Coke
        "JM0",   // Coking coal
        "CU0",   // Copper
        "AL0",   // Aluminum
        "ZN0",   // Zinc
        "NI0",   // Nickel
        "AU0",   // Gold
        "AG0",   // Silver
        "TA0",   // PTA
        "MA0",   // Methanol
        "PP0",   // Polypropylene
        "L0",    // LLDPE
        "V0",    // PVC
        "M0",    // Soybean meal
        "Y0",    // Soybean oil
        "P0",    // Palm oil
        "C0",    // Corn
        "CF0",   // Cotton
        "SR0",   // White sugar
        "RU0",   // Natural rubber
        "FU0",   // Fuel oil
        "SC0",   // Crude oil
    };

    public FuturesRealtimeService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get domestic futures real-time spot data
     *
     * @return DataFrame with futures real-time data
     */
    public DataFrame getFuturesZhSpot() {
        String cacheKey = cacheManager.generateKey("futuresZhSpot");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching futures real-time data from Sina");

        // Build URL with all futures symbols
        StringBuilder urlBuilder = new StringBuilder(SINA_FUTURES_URL);
        for (int i = 0; i < COMMON_FUTURES.length; i++) {
            if (i > 0) urlBuilder.append(",");
            urlBuilder.append("nf_").append(COMMON_FUTURES[i]);
        }

        // 添加 Referer 头以解决新浪 403 错误
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://finance.sina.com.cn");
        
        String response = httpClient.get(urlBuilder.toString(), null, headers);

        DataFrame df = parseSinaFuturesData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get domestic futures daily K-line data
     *
     * @param symbol futures code, e.g., "RB0" for rebar continuous
     * @return DataFrame with futures daily data
     */
    public DataFrame getFuturesZhDaily(String symbol) {
        String cacheKey = cacheManager.generateKey("futuresZhDaily", symbol);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching futures daily data for {} from East Money", symbol);

        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        params.put("pz", "500");
        params.put("po", "1");
        params.put("np", "1");
        params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
        params.put("fltt", "2");
        params.put("invt", "2");
        params.put("fid", "f12");
        params.put("fs", "m:113,m:114,m:115,m:8");
        params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_FUTURES_URL, params, null);

        DataFrame df = parseEastMoneyFuturesData(response, symbol);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseSinaFuturesData(String response) {
        List<Map<String, Object>> rows = new ArrayList<>();

        // Sina response format: var hq_str_nf_RB0="...";
        String[] lines = response.split(";");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            int start = line.indexOf('"');
            int end = line.lastIndexOf('"');
            if (start == -1 || end == -1 || start >= end) continue;

            String data = line.substring(start + 1, end);
            String[] fields = data.split(",");

            if (fields.length < 10) continue;

            // Extract symbol from variable name
            String varName = line.substring(0, start);
            String symbol = "";
            if (varName.contains("nf_")) {
                symbol = varName.substring(varName.indexOf("nf_") + 3);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("代码", symbol);
            row.put("名称", fields[0]);
            row.put("最新价", parseDouble(fields[8]));
            row.put("涨跌额", parseDouble(fields[9]));
            row.put("买价", parseDouble(fields[6]));
            row.put("卖价", parseDouble(fields[7]));
            row.put("最高价", parseDouble(fields[4]));
            row.put("最低价", parseDouble(fields[5]));
            row.put("开盘价", parseDouble(fields[2]));
            row.put("昨结算", parseDouble(fields[3]));
            row.put("持仓量", parseDouble(fields[10]));
            row.put("成交量", parseDouble(fields[14]));
            row.put("成交额", parseDouble(fields[15]));
            row.put("日期", fields[17]);

            rows.add(row);
        }

        return DataFrameImpl.fromList(rows);
    }

    private DataFrame parseEastMoneyFuturesData(String response, String filterSymbol) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("No futures data found");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                String symbol = getStringValue(item, "f12");

                // Filter by symbol if specified
                if (filterSymbol != null && !filterSymbol.isEmpty() && !symbol.equals(filterSymbol)) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();

                row.put("代码", symbol);
                row.put("名称", getStringValue(item, "f14"));
                row.put("最新价", getDoubleValue(item, "f2"));
                row.put("涨跌幅", getDoubleValue(item, "f3"));
                row.put("涨跌额", getDoubleValue(item, "f4"));
                row.put("成交量", getDoubleValue(item, "f5"));
                row.put("成交额", getDoubleValue(item, "f6"));
                row.put("振幅", getDoubleValue(item, "f7"));
                row.put("最高", getDoubleValue(item, "f15"));
                row.put("最低", getDoubleValue(item, "f16"));
                row.put("今开", getDoubleValue(item, "f17"));
                row.put("昨收", getDoubleValue(item, "f18"));
                row.put("持仓量", getDoubleValue(item, "f20"));
                row.put("日增仓", getDoubleValue(item, "f21"));

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("Failed to parse futures data", e);
            return new DataFrameImpl();
        }
    }

    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null ? valueNode.asText() : "";
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

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
