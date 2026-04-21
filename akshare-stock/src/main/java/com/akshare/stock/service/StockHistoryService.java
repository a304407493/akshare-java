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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Stock Historical Data Service
 * Provides historical K-line data for stocks
 */
@Slf4j
public class StockHistoryService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String EASTMONEY_KLINE_URL = "http://push2his.eastmoney.com/api/qt/stock/kline";
    private static final String EASTMONEY_KLINE_GET_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";

    public StockHistoryService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get A-share historical K-line data
     *
     * @param symbol stock code, e.g., "000001"
     * @param period period type: daily/weekly/monthly
     * @param startDate start date, format: yyyyMMdd
     * @param endDate end date, format: yyyyMMdd
     * @param adjust adjust type: qfq/hfq/none
     * @return DataFrame with historical data
     */
    public DataFrame getStockZhAHist(String symbol, String period, String startDate, String endDate, String adjust) {
        String cacheKey = cacheManager.generateKey("stockZhAHist", symbol, period, startDate, endDate, adjust);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的历史数据(周期={}, 复权={})", symbol, period, adjust);

        // Get exchange code
        String exchange = StockCodeUtils.getExchange(symbol);
        if (exchange == null) {
            log.error("Invalid stock code: {}", symbol);
            return new DataFrameImpl();
        }

        // Set default dates if not provided
        if (startDate == null || startDate.isEmpty()) {
            startDate = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // Map period to East Money code
        String periodCode = mapPeriodToEastMoney(period);

        // Map adjust to East Money code
        String adjustCode = mapAdjustToEastMoney(adjust);

        // Build secid
        String secid = exchange.equals("sh") ? "1." + symbol : "0." + symbol;

        Map<String, String> params = new HashMap<>();
        params.put("secid", secid);
        params.put("ut", "fa5fd1943c7b386f172d6893dbfba10b");
        params.put("fields1", "f1,f2,f3,f4,f5,f6");
        params.put("fields2", "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61");
        params.put("klt", periodCode);
        params.put("fqt", adjustCode);
        params.put("beg", startDate);
        params.put("end", endDate);
        params.put("smplmt", "460");
        params.put("lmt", "1000000");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_KLINE_GET_URL, params, null);

        DataFrame df = parseKlineData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get A-share minute K-line data from East Money
     *
     * @param symbol stock code, e.g., "000001"
     * @param period minute period: 1/5/15/30/60
     * @param adjust adjust type: qfq/hfq/none
     * @return DataFrame with minute K-line data
     */
    public DataFrame getStockZhAHistMinEm(String symbol, int period, String adjust) {
        String cacheKey = cacheManager.generateKey("stockZhAHistMinEm", symbol, period, adjust);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的分钟K线数据(周期={}分钟, 复权={})", symbol, period, adjust);

        // Get exchange code
        String exchange = StockCodeUtils.getExchange(symbol);
        if (exchange == null) {
            log.error("Invalid stock code: {}", symbol);
            return new DataFrameImpl();
        }

        // Validate period
        if (!Arrays.asList(1, 5, 15, 30, 60).contains(period)) {
            log.error("无效的周期: {}。必须是: 1, 5, 15, 30, 60", period);
            return new DataFrameImpl();
        }

        // Map adjust to East Money code
        String adjustCode = mapAdjustToEastMoney(adjust);

        // Build secid
        String secid = exchange.equals("sh") ? "1." + symbol : "0." + symbol;

        // 设置默认日期范围（最近一个交易日）
        String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String startDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Map<String, String> params = new HashMap<>();
        params.put("secid", secid);
        params.put("ut", "fa5fd1943c7b386f172d6893dbfba10b");
        params.put("fields1", "f1,f2,f3,f4,f5,f6");
        params.put("fields2", "f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61");
        params.put("klt", String.valueOf(period));
        params.put("fqt", adjustCode);
        params.put("beg", startDate);
        params.put("end", endDate);
        params.put("lmt", "1000000");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_KLINE_GET_URL, params, null);

        DataFrame df = parseKlineData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseKlineData(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode == null || dataNode.isNull()) {
                log.warn("K线响应中未找到数据");
                return new DataFrameImpl();
            }

            JsonNode klinesNode = dataNode.path("klines");

            if (klinesNode == null || !klinesNode.isArray()) {
                log.warn("未找到K线数据");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode kline : klinesNode) {
                String klineStr = kline.asText();
                String[] parts = klineStr.split(",");

                if (parts.length >= 6) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("日期", parts[0]);
                    row.put("开盘", parseDouble(parts[1]));
                    row.put("收盘", parseDouble(parts[2]));
                    row.put("最高", parseDouble(parts[3]));
                    row.put("最低", parseDouble(parts[4]));
                    row.put("成交量", parseDouble(parts[5]));
                    if (parts.length >= 7) {
                        row.put("成交额", parseDouble(parts[6]));
                    }
                    if (parts.length >= 8) {
                        row.put("振幅", parseDouble(parts[7]));
                    }
                    if (parts.length >= 9) {
                        row.put("涨跌幅", parseDouble(parts[8]));
                    }
                    if (parts.length >= 10) {
                        row.put("涨跌额", parseDouble(parts[9]));
                    }
                    if (parts.length >= 11) {
                        row.put("换手率", parseDouble(parts[10]));
                    }

                    rows.add(row);
                }
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析K线数据失败", e);
            return new DataFrameImpl();
        }
    }

    private String mapPeriodToEastMoney(String period) {
        if (period == null) return "101";

        switch (period.toLowerCase()) {
            case "daily":
            case "day":
            case "d":
                return "101";
            case "weekly":
            case "week":
            case "w":
                return "102";
            case "monthly":
            case "month":
            case "m":
                return "103";
            case "quarterly":
            case "quarter":
            case "q":
                return "104";
            case "yearly":
            case "year":
            case "y":
                return "105";
            default:
                return "101";
        }
    }

    private String mapAdjustToEastMoney(String adjust) {
        if (adjust == null) return "0";

        switch (adjust.toLowerCase()) {
            case "none":
            case "":
                return "0";
            case "qfq":
            case "front":
                return "1";
            case "hfq":
            case "back":
                return "2";
            default:
                return "0";
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
