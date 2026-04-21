package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.akshare.core.util.StockCodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StockAuctionService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String EASTMONEY_TRENDS_URL = "https://push2his.eastmoney.com/api/qt/stock/trends2/get";
    private static final String EASTMONEY_STOCK_URL = "https://push2.eastmoney.com/api/qt/stock/get";

    public StockAuctionService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    public DataFrame getStockAuctionData(String symbol) {
        String cacheKey = cacheManager.generateKey("stockAuctionData", symbol);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的集合竞价数据", symbol);

        String exchange = StockCodeUtils.getExchange(symbol);
        if (exchange == null) {
            log.error("Invalid stock code: {}", symbol);
            return new DataFrameImpl();
        }

        String secid = exchange.equals("sh") ? "1." + symbol : "0." + symbol;

        Map<String, String> params = new HashMap<>();
        params.put("secid", secid);
        params.put("fields1", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13");
        params.put("fields2", "f51,f52,f53,f54,f55,f56,f57,f58");
        params.put("iscr", "0");
        params.put("iscd", "0");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_TRENDS_URL, params, null);

        DataFrame df = parseAuctionData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    public DataFrame getStockBidAskData(String symbol) {
        String cacheKey = cacheManager.generateKey("stockBidAskData", symbol);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的买卖五档数据", symbol);

        String exchange = StockCodeUtils.getExchange(symbol);
        if (exchange == null) {
            log.error("Invalid stock code: {}", symbol);
            return new DataFrameImpl();
        }

        String secid = exchange.equals("sh") ? "1." + symbol : "0." + symbol;

        Map<String, String> params = new HashMap<>();
        params.put("secid", secid);
        params.put("fields", "f43,f44,f45,f46,f47,f48,f57,f58,f60,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100,f101,f102,f103,f104,f105,f106,f107,f108,f109,f110,f111,f112,f113,f114,f115,f116,f117,f118,f119,f120");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_STOCK_URL, params, null);

        DataFrame df = parseBidAskDataFromEastMoney(response, symbol);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseAuctionData(String response) {
        try {
            log.info("集合竞价API响应: {}", response.length() > 200 ? response.substring(0, 200) + "..." : response);
            
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode == null || dataNode.isNull()) {
                log.warn("分时走势响应中未找到数据");
                return new DataFrameImpl();
            }

            JsonNode trendsNode = dataNode.path("trends");

            if (trendsNode == null || !trendsNode.isArray()) {
                log.warn("未找到分时走势数据");
                return new DataFrameImpl();
            }

            log.info("找到{}条分时数据", trendsNode.size());

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode trend : trendsNode) {
                String trendStr = trend.asText();
                String[] parts = trendStr.split(",");

                if (parts.length >= 6) {
                    String timeStr = parts[0];
                    Map<String, Object> row = new HashMap<>();
                    row.put("时间", timeStr);
                    row.put("最新价", parseDouble(parts[1]));
                    row.put("均价", parseDouble(parts[2]));
                    row.put("最高", parseDouble(parts[3]));
                    row.put("最低", parseDouble(parts[4]));
                    row.put("成交量", parseDouble(parts[5]));
                    if (parts.length >= 7) {
                        row.put("成交额", parseDouble(parts[6]));
                    }
                    rows.add(row);
                }
            }

            log.info("解析出{}条集合竞价数据", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析集合竞价数据失败", e);
            return new DataFrameImpl();
        }
    }

    private DataFrame parseBidAskDataFromEastMoney(String response, String symbol) {
        try {
            log.info("买卖五档API响应: {}", response.length() > 200 ? response.substring(0, 200) + "..." : response);
            
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode == null || dataNode.isNull()) {
                log.warn("东方财富股票行情响应中未找到数据");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();

            row.put("代码", symbol);
            row.put("名称", getStringValue(dataNode, "f58"));
            row.put("最新价", getPriceValue(dataNode, "f43"));
            row.put("今开", getPriceValue(dataNode, "f46"));
            row.put("最高", getPriceValue(dataNode, "f44"));
            row.put("最低", getPriceValue(dataNode, "f45"));
            row.put("昨收", getPriceValue(dataNode, "f60"));
            row.put("成交量", getDoubleValue(dataNode, "f47"));
            row.put("成交额", getDoubleValue(dataNode, "f48"));

            row.put("买一价", getPriceValue(dataNode, "f71"));
            row.put("买一量", getDoubleValue(dataNode, "f72"));
            row.put("买二价", getPriceValue(dataNode, "f73"));
            row.put("买二量", getDoubleValue(dataNode, "f74"));
            row.put("买三价", getPriceValue(dataNode, "f75"));
            row.put("买三量", getDoubleValue(dataNode, "f76"));
            row.put("买四价", getPriceValue(dataNode, "f77"));
            row.put("买四量", getDoubleValue(dataNode, "f78"));
            row.put("买五价", getPriceValue(dataNode, "f79"));
            row.put("买五量", getDoubleValue(dataNode, "f80"));

            row.put("卖一价", getPriceValue(dataNode, "f81"));
            row.put("卖一量", getDoubleValue(dataNode, "f82"));
            row.put("卖二价", getPriceValue(dataNode, "f83"));
            row.put("卖二量", getDoubleValue(dataNode, "f84"));
            row.put("卖三价", getPriceValue(dataNode, "f85"));
            row.put("卖三量", getDoubleValue(dataNode, "f86"));
            row.put("卖四价", getPriceValue(dataNode, "f87"));
            row.put("卖四量", getDoubleValue(dataNode, "f88"));
            row.put("卖五价", getPriceValue(dataNode, "f89"));
            row.put("卖五量", getDoubleValue(dataNode, "f90"));

            rows.add(row);

            log.info("解析出{}条买卖五档数据，名称: {}, 最新价: {}, 买一价: {}, 卖一价: {}", 
                rows.size(), 
                row.get("名称"), 
                row.get("最新价"), 
                row.get("买一价"), 
                row.get("卖一价"));

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析东方财富买卖五档数据失败", e);
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

    private Double getPriceValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) return null;
        try {
            double price = valueNode.asDouble();
            return price / 1000.0;
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
