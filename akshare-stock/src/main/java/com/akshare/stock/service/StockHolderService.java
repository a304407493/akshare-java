package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.dataframe.Row;
import com.akshare.core.http.HttpClient;
import com.akshare.core.util.StockCodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股东数据服务类
 * 提供股票股东相关数据的查询功能，包括十大股东、十大流通股东、股东持股详情等
 * 数据源：东方财富网
 */
@Slf4j
public class StockHolderService {

    // HTTP客户端，用于发送网络请求
    private final HttpClient httpClient;
    // 缓存管理器，用于缓存查询结果
    private final CacheManager cacheManager;
    // JSON对象映射器，用于解析JSON数据
    private final ObjectMapper objectMapper;

    // 东方财富数据中心API基础URL
    private static final String EASTMONEY_DATA_CENTER_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    // 东方财富F10股东研究页面API
    private static final String EASTMONEY_F10_HOLDER_URL = "https://emweb.securities.eastmoney.com/PC_HSF10/ShareholderResearch/PageAjax";

    /**
     * 构造函数
     * @param httpClient HTTP客户端实例
     * @param cacheManager 缓存管理器实例
     */
    public StockHolderService(HttpClient httpClient, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 十大流通股东 ====================

    /**
     * 获取十大流通股东数据（东方财富）
     * 对应Python AKShare的stock_gdfx_free_top_10_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含十大流通股东数据
     */
    public DataFrame getStockGdfxFreeTop10Em(String symbol, String date) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockGdfxFreeTop10Em", symbol, date);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的十大流通股东数据，报告期：{}", symbol, date);

        // 解析股票代码，提取纯数字代码和市场标识
        String stockCode = extractStockCode(symbol);
        String market = extractMarket(symbol);

        // 验证股票代码
        if (stockCode == null || !StockCodeUtils.isValidStockCode(stockCode)) {
            log.error("无效的股票代码: {}", symbol);
            return new DataFrameImpl();
        }

        // 构建请求参数
        Map<String, String> params = buildFreeTop10Params(stockCode, market, date);

        try {
            // 发送HTTP GET请求
            String response = httpClient.get(EASTMONEY_DATA_CENTER_URL, params, null);
            // 解析响应数据
            DataFrame df = parseFreeTop10Data(response);
            // 将结果存入缓存
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取十大流通股东数据失败: {}", e.getMessage());
            return new DataFrameImpl();
        }
    }

    /**
     * 构建十大流通股东查询参数
     * @param stockCode 纯数字股票代码
     * @param market 市场标识（sh/sz/bj）
     * @param date 报告期日期
     * @return 参数映射表
     */
    private Map<String, String> buildFreeTop10Params(String stockCode, String market, String date) {
        Map<String, String> params = new HashMap<>();
        // 报表名称：十大流通股东
        params.put("reportName", "RPT_F10_EH_FREEHOLDERS");
        // 返回所有列
        params.put("columns", "ALL");
        // 排序列：持股数量
        params.put("sortColumns", "HOLD_NUM");
        // 降序排列
        params.put("sortTypes", "-1");
        // 过滤条件：股票代码
        String filter = String.format("(SECURITY_CODE=\"%s\")", stockCode);
        params.put("filter", filter);
        // 每页数量
        params.put("pageSize", "10");
        // 页码
        params.put("pageNumber", "1");
        // 时间戳
        params.put("_", String.valueOf(System.currentTimeMillis()));
        return params;
    }

    /**
     * 解析十大流通股东数据
     * @param response HTTP响应JSON字符串
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseFreeTop10Data(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            if (resultNode == null || resultNode.isNull()) {
                log.warn("响应中未找到result节点");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 遍历数据节点
            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // 提取字段数据
                row.put("序号", getIntValue(item, "HOLDER_RANK")); // 排名序号
                row.put("股东名称", getStringValue(item, "HOLDER_NAME")); // 股东名称
                row.put("股东类型", getStringValue(item, "HOLDER_TYPE")); // 股东类型
                row.put("股份类型", getStringValue(item, "SHARES_TYPE")); // 股份类型
                row.put("持股数", getLongValue(item, "HOLD_NUM")); // 持股数量（股）
                row.put("占流通股比", getDoubleValue(item, "FREE_HOLDNUM_RATIO")); // 占流通股比例
                row.put("增减", getStringValue(item, "HOLD_CHANGE")); // 持股变动情况
                row.put("变动比率", getDoubleValue(item, "CHANGE_RATIO")); // 变动比率

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析十大流通股东数据失败", e);
            return new DataFrameImpl();
        }
    }

    // ==================== 十大股东 ====================

    /**
     * 获取十大股东数据（东方财富）
     * 对应Python AKShare的stock_gdfx_top_10_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含十大股东数据
     */
    public DataFrame getStockGdfxTop10Em(String symbol, String date) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockGdfxTop10Em", symbol, date);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的十大股东数据，报告期：{}", symbol, date);

        // 解析股票代码
        String stockCode = extractStockCode(symbol);
        String market = extractMarket(symbol);

        // 验证股票代码
        if (stockCode == null || !StockCodeUtils.isValidStockCode(stockCode)) {
            log.error("无效的股票代码: {}", symbol);
            return new DataFrameImpl();
        }

        // 构建请求参数
        Map<String, String> params = buildTop10Params(stockCode, market, date);

        try {
            // 发送HTTP GET请求
            String response = httpClient.get(EASTMONEY_DATA_CENTER_URL, params, null);
            // 解析响应数据
            DataFrame df = parseTop10Data(response);
            // 将结果存入缓存
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取十大股东数据失败: {}", e.getMessage());
            return new DataFrameImpl();
        }
    }

    /**
     * 构建十大股东查询参数
     * @param stockCode 纯数字股票代码
     * @param market 市场标识（sh/sz/bj）
     * @param date 报告期日期
     * @return 参数映射表
     */
    private Map<String, String> buildTop10Params(String stockCode, String market, String date) {
        Map<String, String> params = new HashMap<>();
        // 报表名称：十大股东
        params.put("reportName", "RPT_DMSK_HOLDERS");
        // 返回所有列
        params.put("columns", "ALL");
        // 排序列：持股数量
        params.put("sortColumns", "TOTAL_SHARES");
        // 降序排列
        params.put("sortTypes", "-1");
        // 过滤条件：股票代码和报告期
        String filter = String.format("(SECURITY_CODE=\"%s\")(END_DATE=\"%s\")", stockCode, date);
        params.put("filter", filter);
        // 每页数量
        params.put("pageSize", "10");
        // 页码
        params.put("pageNumber", "1");
        // 时间戳
        params.put("_", String.valueOf(System.currentTimeMillis()));
        return params;
    }

    /**
     * 解析十大股东数据
     * @param response HTTP响应JSON字符串
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseTop10Data(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            if (resultNode == null || resultNode.isNull()) {
                log.warn("响应中未找到result节点");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 遍历数据节点
            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // 提取字段数据
                row.put("序号", getIntValue(item, "RN")); // 排名序号
                row.put("股东名称", getStringValue(item, "HOLDER_NAME")); // 股东名称
                row.put("股份类型", getStringValue(item, "SHARES_TYPE")); // 股份类型
                row.put("持股数", getLongValue(item, "TOTAL_SHARES")); // 持股数量（股）
                row.put("占总股本比例", getDoubleValue(item, "HOLD_RATIO")); // 占总股本比例
                row.put("增减", getStringValue(item, "CHANGE")); // 持股变动情况
                row.put("变动比率", getDoubleValue(item, "CHANGE_RATIO")); // 变动比率

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析十大股东数据失败", e);
            return new DataFrameImpl();
        }
    }

    // ==================== 股东持股详情 ====================

    /**
     * 获取股东持股详情数据（东方财富）
     * 对应Python AKShare的stock_gdfx_holding_detail_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含股东持股详情数据
     */
    public DataFrame getStockGdfxHoldingDetailEm(String symbol, String date) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockGdfxHoldingDetailEm", symbol, date);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的股东持股详情数据，报告期：{}", symbol, date);

        // 解析股票代码
        String stockCode = extractStockCode(symbol);
        String market = extractMarket(symbol);

        // 验证股票代码
        if (stockCode == null || !StockCodeUtils.isValidStockCode(stockCode)) {
            log.error("无效的股票代码: {}", symbol);
            return new DataFrameImpl();
        }

        // 构建请求参数
        Map<String, String> params = buildHoldingDetailParams(stockCode, market, date);

        try {
            // 发送HTTP GET请求
            String response = httpClient.get(EASTMONEY_DATA_CENTER_URL, params, null);
            // 解析响应数据，传入日期进行过滤
            DataFrame df = parseHoldingDetailData(response, date);
            // 将结果存入缓存
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取股东持股详情数据失败: {}", e.getMessage());
            return new DataFrameImpl();
        }
    }

    /**
     * 构建股东持股详情查询参数
     * @param stockCode 纯数字股票代码
     * @param market 市场标识（sh/sz/bj）
     * @param date 报告期日期
     * @return 参数映射表
     */
    private Map<String, String> buildHoldingDetailParams(String stockCode, String market, String date) {
        Map<String, String> params = new HashMap<>();
        // 报表名称：股东持股详情
        params.put("reportName", "RPT_DMSK_HOLDERS");
        // 返回所有列
        params.put("columns", "ALL");
        // 排序列：报告日期降序
        params.put("sortColumns", "END_DATE");
        // 降序排列
        params.put("sortTypes", "-1");
        // 过滤条件：仅股票代码，日期在解析后过滤
        String filter = String.format("(SECURITY_CODE=\"%s\")", stockCode);
        params.put("filter", filter);
        // 每页数量（获取所有数据）
        params.put("pageSize", "50");
        // 页码
        params.put("pageNumber", "1");
        // 时间戳
        params.put("_", String.valueOf(System.currentTimeMillis()));
        return params;
    }

    /**
     * 解析股东持股详情数据
     * @param response HTTP响应JSON字符串
     * @param date 报告期日期，用于过滤数据
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseHoldingDetailData(String response, String date) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            if (resultNode == null || resultNode.isNull()) {
                log.warn("响应中未找到result节点");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 获取第一条数据的日期作为最新报告期
            String latestDate = null;
            if (dataNode.size() > 0) {
                latestDate = getStringValue(dataNode.get(0), "END_DATE");
                log.info("最新报告期日期: {}", latestDate);
            }

            // 遍历数据节点，只返回最新报告期的数据
            for (JsonNode item : dataNode) {
                // 获取报告日期
                String endDate = getStringValue(item, "END_DATE");
                // 只返回最新报告期的数据
                if (latestDate != null && endDate != null && !endDate.equals(latestDate)) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();

                // 提取字段数据
                row.put("序号", getIntValue(item, "RANK")); // 排名序号
                row.put("股东名称", getStringValue(item, "HOLDER_NAME")); // 股东名称
                row.put("股东类型", getStringValue(item, "HOLDER_TYPE")); // 股东类型
                row.put("股份类型", getStringValue(item, "SHARES_TYPE")); // 股份类型
                row.put("持股数", getLongValue(item, "HOLD_NUM")); // 持股数量（股）
                row.put("占总股本比例", getDoubleValue(item, "HOLD_RATIO")); // 占总股本比例
                row.put("增减", getStringValue(item, "HOLD_CHANGE")); // 持股变动情况
                row.put("变动比率", getDoubleValue(item, "HOLDNUM_CHANGE_RATIO")); // 变动比率
                row.put("持股变动数", getLongValue(item, "XZCHANGE")); // 持股变动数量
                row.put("报告期", endDate); // 添加报告期字段

                rows.add(row);
            }

            log.info("解析完成，返回最新报告期数据，行数={}", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析股东持股详情数据失败", e);
            return new DataFrameImpl();
        }
    }

    // ==================== 流通股东持股详情 ====================

    /**
     * 获取流通股东持股详情数据（东方财富）
     * 对应Python AKShare的stock_gdfx_free_holding_detail_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含流通股东持股详情数据
     */
    public DataFrame getStockGdfxFreeHoldingDetailEm(String symbol, String date) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockGdfxFreeHoldingDetailEm", symbol, date);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的流通股东持股详情数据，报告期：{}", symbol, date);

        // 解析股票代码
        String stockCode = extractStockCode(symbol);
        String market = extractMarket(symbol);

        // 验证股票代码
        if (stockCode == null || !StockCodeUtils.isValidStockCode(stockCode)) {
            log.error("无效的股票代码: {}", symbol);
            return new DataFrameImpl();
        }

        // 构建请求参数
        Map<String, String> params = buildFreeHoldingDetailParams(stockCode, market, date);

        try {
            // 发送HTTP GET请求
            String response = httpClient.get(EASTMONEY_DATA_CENTER_URL, params, null);
            // 解析响应数据，传入日期进行过滤
            DataFrame df = parseFreeHoldingDetailData(response, date);
            // 将结果存入缓存
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取流通股东持股详情数据失败: {}", e.getMessage());
            return new DataFrameImpl();
        }
    }

    /**
     * 构建流通股东持股详情查询参数
     * @param stockCode 纯数字股票代码
     * @param market 市场标识（sh/sz/bj）
     * @param date 报告期日期
     * @return 参数映射表
     */
    private Map<String, String> buildFreeHoldingDetailParams(String stockCode, String market, String date) {
        Map<String, String> params = new HashMap<>();
        // 报表名称：流通股东持股详情
        params.put("reportName", "RPT_DMSK_HOLDERS");
        // 返回所有列
        params.put("columns", "ALL");
        // 排序列：报告日期降序
        params.put("sortColumns", "END_DATE");
        // 降序排列
        params.put("sortTypes", "-1");
        // 过滤条件：仅股票代码，日期在解析后过滤
        String filter = String.format("(SECURITY_CODE=\"%s\")", stockCode);
        params.put("filter", filter);
        // 每页数量（获取所有数据）
        params.put("pageSize", "50");
        // 页码
        params.put("pageNumber", "1");
        // 时间戳
        params.put("_", String.valueOf(System.currentTimeMillis()));
        return params;
    }

    /**
     * 解析流通股东持股详情数据
     * @param response HTTP响应JSON字符串
     * @param date 报告期日期，用于过滤数据
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseFreeHoldingDetailData(String response, String date) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            if (resultNode == null || resultNode.isNull()) {
                log.warn("响应中未找到result节点");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 获取第一条数据的日期作为最新报告期
            String latestDate = null;
            if (dataNode.size() > 0) {
                latestDate = getStringValue(dataNode.get(0), "END_DATE");
                log.info("最新报告期日期: {}", latestDate);
            }

            // 遍历数据节点，只返回最新报告期的数据
            for (JsonNode item : dataNode) {
                // 获取报告日期
                String endDate = getStringValue(item, "END_DATE");
                // 只返回最新报告期的数据
                if (latestDate != null && endDate != null && !endDate.equals(latestDate)) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();

                // 提取字段数据
                row.put("序号", getIntValue(item, "RANK")); // 排名序号
                row.put("股东名称", getStringValue(item, "HOLDER_NAME")); // 股东名称
                row.put("股东类型", getStringValue(item, "HOLDER_TYPE")); // 股东类型
                row.put("股份类型", getStringValue(item, "SHARES_TYPE")); // 股份类型
                row.put("持股数", getLongValue(item, "HOLD_NUM")); // 流通持股数量（股）
                row.put("占流通股比", getDoubleValue(item, "HOLD_RATIO")); // 占流通股比例
                row.put("增减", getStringValue(item, "HOLD_CHANGE")); // 持股变动情况
                row.put("变动比率", getDoubleValue(item, "HOLDNUM_CHANGE_RATIO")); // 变动比率
                row.put("持股变动数", getLongValue(item, "XZCHANGE")); // 持股变动数量
                row.put("报告期", endDate); // 添加报告期字段

                rows.add(row);
            }

            log.info("解析完成，返回最新报告期数据，行数={}", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析流通股东持股详情数据失败", e);
            return new DataFrameImpl();
        }
    }

    // ==================== 股东户数数据 ====================

    /**
     * 获取股东户数数据（东方财富）
     * 对应Python AKShare的stock_gdfx_holder_num_em接口
     * 数据来源：RPT_F10_EH_HOLDERNUM
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @return DataFrame 包含股东户数数据
     *         字段包括：报告期、股东户数、户均持股、较上期变化、筹码集中度等
     */
    public DataFrame getStockHolderNumEm(String symbol) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockHolderNumEm", symbol);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取{}的股东户数数据", symbol);

        // 解析股票代码
        String stockCode = extractStockCode(symbol);

        // 验证股票代码
        if (stockCode == null || !StockCodeUtils.isValidStockCode(stockCode)) {
            log.error("无效的股票代码: {}", symbol);
            return new DataFrameImpl();
        }

        // 构建请求参数
        Map<String, String> params = buildHolderNumParams(stockCode);

        try {
            // 发送HTTP GET请求
            String response = httpClient.get(EASTMONEY_DATA_CENTER_URL, params, null);
            // 解析响应数据
            DataFrame df = parseHolderNumData(response);
            // 将结果存入缓存
            cacheManager.put(cacheKey, df);
            return df;
        } catch (Exception e) {
            log.error("获取股东户数数据失败: {}", e.getMessage());
            return new DataFrameImpl();
        }
    }

    /**
     * 构建股东户数查询参数
     * @param stockCode 纯数字股票代码
     * @return 参数映射表
     */
    private Map<String, String> buildHolderNumParams(String stockCode) {
        Map<String, String> params = new HashMap<>();
        // 报表名称：股东户数
        params.put("reportName", "RPT_F10_EH_HOLDERNUM");
        // 返回所有列
        params.put("columns", "ALL");
        // 排序列：报告日期降序
        params.put("sortColumns", "END_DATE");
        // 降序排列
        params.put("sortTypes", "-1");
        // 过滤条件：股票代码
        String filter = String.format("(SECURITY_CODE=\"%s\")", stockCode);
        params.put("filter", filter);
        // 每页数量（获取所有历史数据）
        params.put("pageSize", "500");
        // 页码
        params.put("pageNumber", "1");
        // 时间戳
        params.put("_", String.valueOf(System.currentTimeMillis()));
        return params;
    }

    /**
     * 解析股东户数数据
     * @param response HTTP响应JSON字符串
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseHolderNumData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            if (resultNode == null || resultNode.isNull()) {
                log.warn("响应中未找到result节点");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 遍历数据节点
            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // 提取字段数据 - 映射到前端期望的字段名
                row.put("股票代码", getStringValue(item, "SECURITY_CODE"));
                row.put("股票名称", getStringValue(item, "SECURITY_NAME_ABBR"));
                row.put("报告期", getStringValue(item, "END_DATE"));
                row.put("股东户数", getLongValue(item, "HOLDER_TOTAL_NUM")); // 股东总户数
                row.put("户均持股", getLongValue(item, "AVG_FREE_SHARES")); // 户均流通股数
                row.put("人均持股", getLongValue(item, "AVG_TOTAL_SHARES")); // 人均持股数
                row.put("较上期变化", getDoubleValue(item, "TOTAL_NUM_RATIO")); // 股东户数变化率
                row.put("户均持股市值", getDoubleValue(item, "AVG_HOLD_AMT")); // 户均持股市值
                row.put("筹码集中度", getDoubleValue(item, "HOLD_RATIO_TOTAL")); // 持股集中度
                row.put("股价", getDoubleValue(item, "PRICE")); // 股价
                row.put("公告日期", getStringValue(item, "NOTICE_DATE"));

                rows.add(row);
            }

            log.info("成功解析股东户数数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析股东户数数据失败", e);
            return new DataFrameImpl();
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 从带市场标识的股票代码中提取纯数字代码
     * @param symbol 股票代码，如"sh600000"或"sz000001"
     * @return 纯数字股票代码，如"600000"或"000001"
     */
    private String extractStockCode(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return null;
        }
        String trimmed = symbol.trim().toLowerCase();
        // 去除市场标识前缀
        if (trimmed.startsWith("sh") || trimmed.startsWith("sz") || trimmed.startsWith("bj")) {
            return trimmed.substring(2);
        }
        return trimmed;
    }

    /**
     * 从带市场标识的股票代码中提取市场标识
     * @param symbol 股票代码，如"sh600000"或"sz000001"
     * @return 市场标识（sh/sz/bj）
     */
    private String extractMarket(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return null;
        }
        String trimmed = symbol.trim().toLowerCase();
        // 提取市场标识前缀
        if (trimmed.startsWith("sh")) {
            return "sh";
        } else if (trimmed.startsWith("sz")) {
            return "sz";
        } else if (trimmed.startsWith("bj")) {
            return "bj";
        }
        // 如果没有前缀，根据代码规则判断
        String code = extractStockCode(symbol);
        return StockCodeUtils.getExchange(code);
    }

    /**
     * 从JSON节点获取字符串值
     * @param node JSON节点
     * @param field 字段名
     * @return 字符串值，如果为空则返回空字符串
     */
    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null && !valueNode.isNull() ? valueNode.asText() : "";
    }

    /**
     * 从JSON节点获取整数值
     * @param node JSON节点
     * @param field 字段名
     * @return 整数值，如果为空则返回null
     */
    private Integer getIntValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        try {
            return valueNode.asInt();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JSON节点获取长整数值
     * @param node JSON节点
     * @param field 字段名
     * @return 长整数值，如果为空则返回null
     */
    private Long getLongValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        try {
            return valueNode.asLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JSON节点获取双精度浮点数值
     * @param node JSON节点
     * @param field 字段名
     * @return 双精度浮点数值，如果为空则返回null
     */
    private Double getDoubleValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        try {
            return valueNode.asDouble();
        } catch (Exception e) {
            return null;
        }
    }
}
