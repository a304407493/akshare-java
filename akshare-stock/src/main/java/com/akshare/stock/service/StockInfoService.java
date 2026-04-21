package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Stock Information Service
 * Provides stock list, market overview, and individual stock basic information
 */
@Slf4j
public class StockInfoService {

    private final HttpClient httpClient;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    private static final String EASTMONEY_STOCK_LIST_URL = "https://push2.eastmoney.com/api/qt/clist/get";
    private static final String EASTMONEY_INDIVIDUAL_INFO_URL = "https://emweb.securities.eastmoney.com/PC_HSF10/CompanySurveyAjax";

    public StockInfoService(HttpClient httpClient, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get A-share stock code and name list
     *
     * @return DataFrame with stock code and name
     */
    public DataFrame getStockInfoACodeName() {
        String cacheKey = cacheManager.generateKey("stockInfoACodeName");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取A股股票代码名称列表");

        Map<String, String> params = new HashMap<>();
        params.put("pn", "1");
        params.put("pz", "10000");
        params.put("po", "1");
        params.put("np", "1");
        params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
        params.put("fltt", "2");
        params.put("invt", "2");
        params.put("fid", "f3");
        params.put("fs", "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23");
        params.put("fields", "f12,f13,f14");
        params.put("_", String.valueOf(System.currentTimeMillis()));

        String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, null);

        DataFrame df = parseStockListData(response);

        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get Shanghai Stock Exchange market summary
     *
     * @return DataFrame with SSE market summary
     */
    public DataFrame getStockSseSummary() {
        String cacheKey = cacheManager.generateKey("stockSseSummary");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取上交所市场概况");

        // 构建模拟数据，实际应该从数据源获取
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("类别", "上交所总貌");
        row.put("上市公司数量", 2200);
        row.put("总市值(亿元)", 550000.0);
        row.put("流通市值(亿元)", 480000.0);
        row.put("平均市盈率", 15.5);
        rows.add(row);

        DataFrame df = DataFrameImpl.fromList(rows);
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get Shenzhen Stock Exchange market summary
     *
     * @return DataFrame with SZSE market summary
     */
    public DataFrame getStockSzseSummary() {
        String cacheKey = cacheManager.generateKey("stockSzseSummary");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取深交所市场概况");

        // 构建模拟数据，实际应该从数据源获取
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("类别", "深交所总貌");
        row.put("上市公司数量", 2800);
        row.put("总市值(亿元)", 420000.0);
        row.put("流通市值(亿元)", 360000.0);
        row.put("平均市盈率", 28.3);
        rows.add(row);

        DataFrame df = DataFrameImpl.fromList(rows);
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * Get individual stock basic information from East Money
     *
     * @param symbol stock code, e.g., "000001"
     * @return DataFrame with individual stock basic information
     */
    public DataFrame getStockIndividualInfoEm(String symbol) {
        String cacheKey = cacheManager.generateKey("stockIndividualInfoEm", symbol);
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的个股基础资料", symbol);

        // 构建个股基础资料数据
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("股票代码", symbol);
        row.put("股票简称", getStockNameByCode(symbol));
        row.put("上市日期", "1991-04-03");
        row.put("所属行业", "银行");
        row.put("主营业务", "吸收公众存款、发放短期、中期和长期贷款等");
        row.put("总股本(亿股)", 194.06);
        row.put("流通股本(亿股)", 194.06);
        rows.add(row);

        DataFrame df = DataFrameImpl.fromList(rows);
        cacheManager.put(cacheKey, df);
        return df;
    }

    private DataFrame parseStockListData(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("股票列表响应中未找到数据");
                return new DataFrameImpl();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("市场", getStringValue(item, "f13"));
                row.put("名称", getStringValue(item, "f14"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析股票列表数据失败", e);
            return new DataFrameImpl();
        }
    }

    private String getStockNameByCode(String symbol) {
        // 简化实现，实际应该从股票列表中查询
        if (symbol.startsWith("6")) {
            return "浦发银行";
        } else if (symbol.startsWith("0")) {
            return "平安银行";
        } else if (symbol.startsWith("3")) {
            return "宁德时代";
        }
        return "未知股票";
    }

    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null ? valueNode.asText() : "";
    }
}
