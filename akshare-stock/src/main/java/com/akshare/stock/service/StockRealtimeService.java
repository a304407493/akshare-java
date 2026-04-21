package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.dataframe.Row;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.HtmlParser;
import com.akshare.core.parser.JsonParser;
import com.akshare.core.util.StockCodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Stock Real-time Data Service
 * Provides real-time stock quotes and market data
 */
@Slf4j
public class StockRealtimeService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final HtmlParser htmlParser;
    private final CacheManager cacheManager;

    private static final String EASTMONEY_LIST_URL = "https://push2.eastmoney.com/api/qt/clist/get";
    private static final String SINA_HQ_URL = "https://hq.sinajs.cn/list=";

    public StockRealtimeService(HttpClient httpClient, JsonParser jsonParser,
                                 HtmlParser htmlParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.htmlParser = htmlParser;
        this.cacheManager = cacheManager;
    }

    /**
     * Get A-share real-time spot data from East Money
     *
     * @return DataFrame with real-time stock data
     */
    public DataFrame getStockZhASpotEm() {
        String cacheKey = cacheManager.generateKey("stockZhASpotEm");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从东方财富获取A股实时数据");

        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        params.put("pz", "5000");
        params.put("po", "1");
        params.put("np", "1");
        params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
        params.put("fltt", "2");
        params.put("invt", "2");
        params.put("fid", "f12");
        params.put("fs", "m:0+t:6,m:0+t:13,m:0+t:80,m:1+t:2,m:1+t:23,m:0+t:81+s:204");
        params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_LIST_URL, params, null);

        DataFrame df = parseEastMoneySpotData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get A-share real-time spot data from Sina
     *
     * @return DataFrame with real-time stock data
     */
    public DataFrame getStockZhASpotSina() {
        String cacheKey = cacheManager.generateKey("stockZhASpotSina");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在从新浪获取A股实时数据");

        // Get a sample of stocks (top 100 by volume)
        // In practice, you would need to fetch all stocks in batches
        String[] sampleStocks = {
            "sh600000", "sh600519", "sz000001", "sz000858",
            "sh600036", "sh601318", "sz002415", "sz300750"
        };

        String url = SINA_HQ_URL + String.join(",", sampleStocks);
        String response = httpClient.get(url, null, null);

        DataFrame df = parseSinaSpotData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseEastMoneySpotData(String response) {
        try {
            JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("东方财富响应中未找到数据");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // Field mapping based on East Money API
                row.put("序号", getIntValue(item, "f1"));
                row.put("最新价", getDoubleValue(item, "f2"));
                row.put("涨跌幅", getDoubleValue(item, "f3"));
                row.put("涨跌额", getDoubleValue(item, "f4"));
                row.put("成交量", getDoubleValue(item, "f5"));
                row.put("成交额", getDoubleValue(item, "f6"));
                row.put("振幅", getDoubleValue(item, "f7"));
                row.put("换手率", getDoubleValue(item, "f8"));
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("量比", getDoubleValue(item, "f10"));
                row.put("代码", getStringValue(item, "f12"));
                row.put("市场", getStringValue(item, "f13"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("最高", getDoubleValue(item, "f15"));
                row.put("最低", getDoubleValue(item, "f16"));
                row.put("今开", getDoubleValue(item, "f17"));
                row.put("昨收", getDoubleValue(item, "f18"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                row.put("涨速", getDoubleValue(item, "f22"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("60日涨跌幅", getDoubleValue(item, "f24"));
                row.put("年初至今涨跌幅", getDoubleValue(item, "f25"));
                row.put("主力净流入", getDoubleValue(item, "f62"));

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析东方财富实时数据失败", e);
            return new DataFrameImpl();
        }
    }

    private DataFrame parseSinaSpotData(String response) {
        List<Map<String, Object>> rows = new ArrayList<>();

        // Sina response format: var hq_str_sh600000="...";
        String[] lines = response.split(";");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            int start = line.indexOf('"');
            int end = line.lastIndexOf('"');
            if (start == -1 || end == -1 || start >= end) continue;

            String data = line.substring(start + 1, end);
            String[] fields = data.split(",");

            if (fields.length < 33) continue;

            // Extract stock code from variable name
            String varName = line.substring(0, start);
            String stockCode = "";
            if (varName.contains("sh")) {
                stockCode = varName.substring(varName.indexOf("sh") + 2);
            } else if (varName.contains("sz")) {
                stockCode = varName.substring(varName.indexOf("sz") + 2);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("代码", stockCode);
            row.put("名称", fields[0]);
            row.put("今开", parseDouble(fields[1]));
            row.put("昨收", parseDouble(fields[2]));
            row.put("最新价", parseDouble(fields[3]));
            row.put("最高", parseDouble(fields[4]));
            row.put("最低", parseDouble(fields[5]));
            row.put("竞买价", parseDouble(fields[6]));
            row.put("竞卖价", parseDouble(fields[7]));
            row.put("成交量", parseDouble(fields[8]));
            row.put("成交额", parseDouble(fields[9]));
            row.put("买一量", parseDouble(fields[10]));
            row.put("买一价", parseDouble(fields[11]));
            row.put("买二量", parseDouble(fields[12]));
            row.put("买二价", parseDouble(fields[13]));
            row.put("买三量", parseDouble(fields[14]));
            row.put("买三价", parseDouble(fields[15]));
            row.put("买四量", parseDouble(fields[16]));
            row.put("买四价", parseDouble(fields[17]));
            row.put("买五量", parseDouble(fields[18]));
            row.put("买五价", parseDouble(fields[19]));
            row.put("卖一量", parseDouble(fields[20]));
            row.put("卖一价", parseDouble(fields[21]));
            row.put("卖二量", parseDouble(fields[22]));
            row.put("卖二价", parseDouble(fields[23]));
            row.put("卖三量", parseDouble(fields[24]));
            row.put("卖三价", parseDouble(fields[25]));
            row.put("卖四量", parseDouble(fields[26]));
            row.put("卖四价", parseDouble(fields[27]));
            row.put("卖五量", parseDouble(fields[28]));
            row.put("卖五价", parseDouble(fields[29]));
            row.put("日期", fields[30]);
            row.put("时间", fields[31]);

            rows.add(row);
        }

        return DataFrameImpl.fromList(rows);
    }

    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null ? valueNode.asText() : "";
    }

    private Integer getIntValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) return null;
        try {
            return valueNode.asInt();
        } catch (Exception e) {
            return null;
        }
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
