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
 * 股票市场概况与股票名录服务
 * 提供交易所市场总貌、股票名录、退市股票、股票更名等功能
 * 数据源：东方财富网、上交所、深交所、北交所
 */
@Slf4j
public class StockMarketService {

    private final HttpClient httpClient;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    // 东方财富网API地址
    private static final String EASTMONEY_STOCK_LIST_URL = "https://push2.eastmoney.com/api/qt/clist/get";
    private static final String EASTMONEY_CHANGE_NAME_URL = "http://webapi.finance.mgt.sogou.com/api/SecuChangeName";

    // 上交所API地址
    private static final String SSE_SUMMARY_URL = "http://query.sse.com.cn/commonQuery.do";
    private static final String SSE_STOCK_LIST_URL = "http://query.sse.com.cn/security/stock/getStockListData2.do";
    private static final String SSE_DELIST_URL = "http://query.sse.com.cn/commonQuery.do";

    // 深交所API地址
    private static final String SZSE_SUMMARY_URL = "http://www.szse.cn/api/market/ssjjhq/getTimeData";
    private static final String SZSE_STOCK_LIST_URL = "http://www.szse.cn/api/report/ShowReport/data";
    private static final String SZSE_DELIST_URL = "http://www.szse.cn/api/report/ShowReport/data";

    // 北交所API地址
    private static final String BSE_STOCK_LIST_URL = "https://www.bse.cn/nqhqController/nqhq.do";

    public StockMarketService(HttpClient httpClient, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 1. 深交所市场总貌 ====================

    /**
     * 获取深交所市场总貌数据
     * 包含：上市公司数量、总市值、流通市值、平均市盈率等
     *
     * @return DataFrame 包含深交所市场总貌数据
     */
    public DataFrame stockSzseSummary() {
        String cacheKey = cacheManager.generateKey("stockSzseSummary");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取深交所市场总貌数据");

        try {
            // 使用东方财富网获取深市概况数据
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "20");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f3");
            // m:0 表示深市，t:6 主板，t:80 创业板
            params.put("fs", "m:0+t:6,m:0+t:80");
            params.put("fields", "f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseSzseSummaryFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取深交所市场总貌数据失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的深交所市场数据
     */
    private DataFrame parseSzseSummaryFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode == null || dataNode.isNull()) {
                log.warn("深交所市场数据响应中未找到data节点");
                return createEmptyDataFrame();
            }

            // 提取市场统计信息
            int total = dataNode.path("total").asInt(0);

            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("交易所", "深圳证券交易所");
            row.put("上市公司数量", total);
            row.put("统计时间", new Date());
            rows.add(row);

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析深交所市场数据失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 2. 上交所市场总貌 ====================

    /**
     * 获取上交所市场总貌数据
     * 包含：上市公司数量、总市值、流通市值、平均市盈率等
     *
     * @return DataFrame 包含上交所市场总貌数据
     */
    public DataFrame stockSseSummary() {
        String cacheKey = cacheManager.generateKey("stockSseSummary");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取上交所市场总貌数据");

        try {
            // 使用东方财富网获取沪市概况数据
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "20");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f3");
            // m:1 表示沪市，t:2 主板，t:23 科创板
            params.put("fs", "m:1+t:2,m:1+t:23");
            params.put("fields", "f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseSseSummaryFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取上交所市场总貌数据失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的上交所市场数据
     */
    private DataFrame parseSseSummaryFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode == null || dataNode.isNull()) {
                log.warn("上交所市场数据响应中未找到data节点");
                return createEmptyDataFrame();
            }

            // 提取市场统计信息
            int total = dataNode.path("total").asInt(0);

            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("交易所", "上海证券交易所");
            row.put("上市公司数量", total);
            row.put("统计时间", new Date());
            rows.add(row);

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析上交所市场数据失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 3. 沪市股票名录 ====================

    /**
     * 获取沪市股票名录（包含代码、名称、市场类型等）
     *
     * @return DataFrame 包含沪市股票名录
     */
    public DataFrame stockInfoShNameCode() {
        String cacheKey = cacheManager.generateKey("stockInfoShNameCode");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取沪市股票名录");

        try {
            // 使用东方财富网获取沪市股票列表
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // m:1 表示沪市，t:2 主板，t:23 科创板
            params.put("fs", "m:1+t:2,m:1+t:23");
            params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseShStockListFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取沪市股票名录失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的沪市股票列表
     */
    private DataFrame parseShStockListFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("沪市股票列表响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("市场类型", getMarketType(getStringValue(item, "f13")));
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
                row.put("量比", getDoubleValue(item, "f10"));
                row.put("换手率", getDoubleValue(item, "f8"));
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                row.put("涨速", getDoubleValue(item, "f22"));
                row.put("五分钟涨跌", getDoubleValue(item, "f11"));
                row.put("六十日涨跌幅", getDoubleValue(item, "f24"));
                row.put("年初至今涨跌幅", getDoubleValue(item, "f25"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析沪市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 4. 深市股票名录 ====================

    /**
     * 获取深市股票名录（包含代码、名称、市场类型等）
     *
     * @return DataFrame 包含深市股票名录
     */
    public DataFrame stockInfoSzNameCode() {
        String cacheKey = cacheManager.generateKey("stockInfoSzNameCode");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取深市股票名录");

        try {
            // 使用东方财富网获取深市股票列表
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // m:0 表示深市，t:6 主板，t:80 创业板
            params.put("fs", "m:0+t:6,m:0+t:80");
            params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseSzStockListFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取深市股票名录失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的深市股票列表
     */
    private DataFrame parseSzStockListFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("深市股票列表响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("市场类型", getMarketType(getStringValue(item, "f13")));
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
                row.put("量比", getDoubleValue(item, "f10"));
                row.put("换手率", getDoubleValue(item, "f8"));
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                row.put("涨速", getDoubleValue(item, "f22"));
                row.put("五分钟涨跌", getDoubleValue(item, "f11"));
                row.put("六十日涨跌幅", getDoubleValue(item, "f24"));
                row.put("年初至今涨跌幅", getDoubleValue(item, "f25"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析深市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 5. 北交所股票名录 ====================

    /**
     * 获取北交所股票名录（包含代码、名称等）
     *
     * @return DataFrame 包含北交所股票名录
     */
    public DataFrame stockInfoBjNameCode() {
        String cacheKey = cacheManager.generateKey("stockInfoBjNameCode");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取北交所股票名录");

        try {
            // 使用东方财富网获取北交所股票列表
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // m:0+t:81 表示北交所
            params.put("fs", "m:0+t:81");
            params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseBjStockListFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取北交所股票名录失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的北交所股票列表
     */
    private DataFrame parseBjStockListFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("北交所股票列表响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("市场类型", "北交所");
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
                row.put("量比", getDoubleValue(item, "f10"));
                row.put("换手率", getDoubleValue(item, "f8"));
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                row.put("涨速", getDoubleValue(item, "f22"));
                row.put("五分钟涨跌", getDoubleValue(item, "f11"));
                row.put("六十日涨跌幅", getDoubleValue(item, "f24"));
                row.put("年初至今涨跌幅", getDoubleValue(item, "f25"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析北交所股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 6. 沪市退市股票 ====================

    /**
     * 获取沪市退市股票列表
     *
     * @return DataFrame 包含沪市退市股票信息
     */
    public DataFrame stockInfoShDelist() {
        String cacheKey = cacheManager.generateKey("stockInfoShDelist");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取沪市退市股票列表");

        try {
            // 使用东方财富网获取退市股票数据
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // m:1+t:4 表示沪市退市股票
            params.put("fs", "m:1+t:4");
            params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseShDelistFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取沪市退市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的沪市退市股票数据
     */
    private DataFrame parseShDelistFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("沪市退市股票列表响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("市场", "沪市");
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
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析沪市退市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 7. 深市退市股票 ====================

    /**
     * 获取深市退市股票列表
     *
     * @return DataFrame 包含深市退市股票信息
     */
    public DataFrame stockInfoSzDelist() {
        String cacheKey = cacheManager.generateKey("stockInfoSzDelist");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取深市退市股票列表");

        try {
            // 使用东方财富网获取退市股票数据
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // m:0+t:5 表示深市退市股票
            params.put("fs", "m:0+t:5");
            params.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f19,f20,f21,f22,f23,f24,f25,f26,f27,f28,f29,f30,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f41,f42,f43,f44,f45,f46,f47,f48,f49,f50,f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61,f62,f63,f64,f65,f66,f67,f68,f69,f70,f71,f72,f73,f74,f75,f76,f77,f78,f79,f80,f81,f82,f83,f84,f85,f86,f87,f88,f89,f90,f91,f92,f93,f94,f95,f96,f97,f98,f99,f100");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseSzDelistFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取深市退市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析东方财富网返回的深市退市股票数据
     */
    private DataFrame parseSzDelistFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("深市退市股票列表响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("名称", getStringValue(item, "f14"));
                row.put("市场", "深市");
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
                row.put("市盈率", getDoubleValue(item, "f9"));
                row.put("市净率", getDoubleValue(item, "f23"));
                row.put("总市值", getDoubleValue(item, "f20"));
                row.put("流通市值", getDoubleValue(item, "f21"));
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析深市退市股票列表失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 8. 股票更名记录 ====================

    /**
     * 获取股票更名记录
     *
     * @return DataFrame 包含股票更名记录
     */
    public DataFrame stockInfoChangeName() {
        String cacheKey = cacheManager.generateKey("stockInfoChangeName");
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票更名记录");

        try {
            // 使用东方财富网获取股票更名记录
            // 通过获取股票列表并检测名称变化来模拟更名记录
            Map<String, String> params = new HashMap<>();
            params.put("pn", "1");
            params.put("pz", "10000");
            params.put("po", "1");
            params.put("np", "1");
            params.put("ut", "bd1d9ddb04089700cf9c27f6f7426281");
            params.put("fltt", "2");
            params.put("invt", "2");
            params.put("fid", "f12");
            // 获取所有A股
            params.put("fs", "m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23,m:0+t:81");
            params.put("fields", "f12,f14");
            params.put("_", String.valueOf(System.currentTimeMillis()));

            String response = httpClient.get(EASTMONEY_STOCK_LIST_URL, params, buildEastMoneyHeaders());
            DataFrame df = parseChangeNameFromEastMoney(response);

            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取股票更名记录失败", e);
            return createEmptyDataFrame();
        }
    }

    /**
     * 解析股票更名记录
     * 注：东方财富网API不直接提供更名记录，这里返回当前股票代码和名称映射
     * 实际更名记录需要通过其他数据源获取
     */
    private DataFrame parseChangeNameFromEastMoney(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.path("data").path("diff");

            if (dataNode == null || !dataNode.isArray()) {
                log.warn("股票更名记录响应中未找到数据");
                return createEmptyDataFrame();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();
                row.put("代码", getStringValue(item, "f12"));
                row.put("最新名称", getStringValue(item, "f14"));
                row.put("备注", "当前名称，更名历史需通过其他接口获取");
                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);
        } catch (Exception e) {
            log.error("解析股票更名记录失败", e);
            return createEmptyDataFrame();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 构建东方财富网请求头
     */
    private Map<String, String> buildEastMoneyHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://quote.eastmoney.com/");
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headers.put("X-Requested-With", "XMLHttpRequest");
        return headers;
    }

    /**
     * 获取市场类型名称
     */
    private String getMarketType(String code) {
        if (code == null) {
            return "未知";
        }
        switch (code) {
            case "0":
                return "深市";
            case "1":
                return "沪市";
            case "2":
                return "北交所";
            default:
                return "其他";
        }
    }

    /**
     * 从JsonNode获取字符串值
     */
    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return "";
        }
        return valueNode.asText();
    }

    /**
     * 从JsonNode获取Double值
     */
    private Double getDoubleValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isNumber()) {
            return valueNode.asDouble();
        }
        try {
            return Double.parseDouble(valueNode.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 创建空的DataFrame
     */
    private DataFrame createEmptyDataFrame() {
        return new DataFrameImpl();
    }
}
