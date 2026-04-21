package com.akshare.fund.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Fund Real-time Data Service
 * Provides fund net value and ETF data
 */
@Slf4j
public class FundRealtimeService {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String FUND_DAILY_URL = "https://fund.eastmoney.com/data/rankhandler.aspx";
    private static final String FUND_LIST_URL = "https://fund.eastmoney.com/js/fundcode_search.js";
    private static final String ETF_LIST_URL = "https://push2.eastmoney.com/api/qt/clist/get";

    public FundRealtimeService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get open fund daily net value from East Money
     *
     * @return DataFrame with fund daily data
     */
    public DataFrame getFundEmOpenFundDaily() {
        String cacheKey = cacheManager.generateKey("fundEmOpenFundDaily");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching open fund daily data from East Money");

        try {
            // Build the URL with parameters
            StringBuilder urlBuilder = new StringBuilder(FUND_DAILY_URL);
            urlBuilder.append("?op=ph");
            urlBuilder.append("&dt=kf");
            urlBuilder.append("&ft=all");
            urlBuilder.append("&rs=");
            urlBuilder.append("&gs=0");
            urlBuilder.append("&sc=zzf");
            urlBuilder.append("&st=desc");
            urlBuilder.append("&sd=2020-01-01");
            urlBuilder.append("&ed=").append(getCurrentDate());
            urlBuilder.append("&qdii=");
            urlBuilder.append("&tabSubtype=,,,,,");
            urlBuilder.append("&pi=1");
            urlBuilder.append("&pn=10000");
            urlBuilder.append("&dx=1");
            urlBuilder.append("&v=").append(System.currentTimeMillis());

            // 添加 Referer 头
            Map<String, String> headers = new HashMap<>();
            headers.put("Referer", "https://fund.eastmoney.com");
            
            String response = httpClient.get(urlBuilder.toString(), null, headers);
            
            // 检查响应是否有效
            if (response == null || response.length() < 100) {
                log.warn("基金数据响应无效，可能是API限制或接口变更");
                return new DataFrameImpl();
            }

            DataFrame df = parseFundDailyData(response);

            cacheManager.put(cacheKey, df);
            return df;

        } catch (Exception e) {
            log.error("Failed to fetch fund daily data", e);
            return new DataFrameImpl();
        }
    }

    /**
     * Get fund name list
     *
     * @return DataFrame with fund names
     */
    public DataFrame getFundEmFundName() {
        String cacheKey = cacheManager.generateKey("fundEmFundName");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching fund name list from East Money");

        String response = httpClient.get(FUND_LIST_URL, null, null);

        DataFrame df = parseFundNameData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get ETF real-time spot data from East Money
     *
     * @return DataFrame with ETF data
     */
    public DataFrame getFundEtfSpotEm() {
        String cacheKey = cacheManager.generateKey("fundEtfSpotEm");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("Fetching ETF spot data from East Money");

        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        params.put("pz", "500");
        params.put("po", "1");
        params.put("np", "1");
        params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
        params.put("fltt", "2");
        params.put("invt", "2");
        params.put("fid", "f12");
        params.put("fs", "b:MK0021,b:MK0022,b:MK0023,b:MK0024");
        params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(ETF_LIST_URL, params, null);

        DataFrame df = parseEtfData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseFundDailyData(String response) {
        try {
            // Response format: var rankData = { datas:[...], ... };
            // 提取 datas 数组部分
            int datasStart = response.indexOf("datas:");
            if (datasStart == -1) {
                log.warn("Invalid response format: datas not found");
                return new DataFrameImpl();
            }
            
            // 找到 datas 数组的开始和结束
            int arrayStart = response.indexOf('[', datasStart);
            int arrayEnd = response.indexOf(']', arrayStart);
            if (arrayStart == -1 || arrayEnd == -1) {
                log.warn("Invalid response format: datas array not found");
                return new DataFrameImpl();
            }
            
            String jsonArrayStr = response.substring(arrayStart, arrayEnd + 1);
            JsonNode dataNode = objectMapper.readTree(jsonArrayStr);

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("No fund data found");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                String fundStr = item.asText();
                String[] parts = fundStr.split(",");

                if (parts.length >= 20) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("基金代码", parts[0]);
                    row.put("基金简称", parts[1]);
                    row.put("日期", parts[2]);
                    row.put("单位净值", parseDouble(parts[3]));
                    row.put("累计净值", parseDouble(parts[4]));
                    row.put("日增长率", parseDouble(parts[5]));
                    row.put("近1周", parseDouble(parts[6]));
                    row.put("近1月", parseDouble(parts[7]));
                    row.put("近3月", parseDouble(parts[8]));
                    row.put("近6月", parseDouble(parts[9]));
                    row.put("近1年", parseDouble(parts[10]));
                    row.put("近2年", parseDouble(parts[11]));
                    row.put("近3年", parseDouble(parts[12]));
                    row.put("今年来", parseDouble(parts[13]));
                    row.put("成立来", parseDouble(parts[14]));
                    row.put("成立日期", parts[15]);
                    row.put("购买手续费", parts[17]);
                    row.put("赎回手续费", parts[18]);

                    rows.add(row);
                }
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("Failed to parse fund daily data", e);
            return new DataFrameImpl();
        }
    }

    private DataFrame parseFundNameData(String response) {
        try {
            // Response format: var r = [[...], [...], ...];
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start == -1 || end == -1) {
                log.warn("Invalid response format");
                return new DataFrameImpl();
            }

            String jsonStr = response.substring(start, end + 1);
            JsonNode rootNode = objectMapper.readTree(jsonStr);

            if (!rootNode.isArray()) {
                log.warn("No fund name data found");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : rootNode) {
                if (item.isArray() && item.size() >= 4) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("基金代码", item.get(0).asText());
                    row.put("基金简称", item.get(2).asText());
                    row.put("基金类型", item.get(3).asText());
                    row.put("拼音缩写", item.get(1).asText());

                    rows.add(row);
                }
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("Failed to parse fund name data", e);
            return new DataFrameImpl();
        }
    }

    private DataFrame parseEtfData(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("No ETF data found");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                row.put("代码", getStringValue(item, "f12"));
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
                row.put("换手率", getDoubleValue(item, "f8"));
                row.put("量比", getDoubleValue(item, "f10"));

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("Failed to parse ETF data", e);
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
        if (value == null || value.isEmpty() || value.equals("-")) return null;
        try {
            return Double.parseDouble(value.replace("%", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCurrentDate() {
        return java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
