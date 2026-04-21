package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.akshare.core.util.StockCodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 股票财务数据服务类
 * 提供从东方财富网获取财务报表数据的功能
 * 包括：资产负债表、利润表、现金流量表、财务分析指标等
 *
 * @author AKShare Java Team
 * @since 1.0.0
 */
@Slf4j
public class StockFinancialService {

    /** HTTP客户端，用于发送网络请求 */
    private final HttpClient httpClient;
    /** 缓存管理器，用于缓存请求结果 */
    private final CacheManager cacheManager;
    /** JSON对象映射器，用于解析JSON数据 */
    private final ObjectMapper objectMapper;

    // ==================== 东方财富网API接口地址 ====================
    /** 资产负债表API地址 */
    private static final String EASTMONEY_BALANCE_SHEET_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 利润表API地址 */
    private static final String EASTMONEY_PROFIT_SHEET_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 现金流量表API地址 */
    private static final String EASTMONEY_CASH_FLOW_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 财务分析指标API地址 */
    private static final String EASTMONEY_FINANCIAL_ANALYSIS_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";

    // ==================== 报表类型常量 ====================
    /** 报表类型：按报告期 */
    public static final String REPORT_TYPE_REPORT = "1";
    /** 报表类型：按年度 */
    public static final String REPORT_TYPE_YEAR = "2";
    /** 报表类型：按单季度 */
    public static final String REPORT_TYPE_QUARTER = "3";

    /**
     * 构造函数
     *
     * @param httpClient HTTP客户端实例
     * @param cacheManager 缓存管理器实例
     */
    public StockFinancialService(HttpClient httpClient, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 财务报表综合接口 ====================

    /**
     * 获取财务报表数据（资产负债表、利润表、现金流量表）
     * 从东方财富网获取指定股票的财务报表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @param reportName 报表名称："balancesheet"-资产负债表，"incomesheet"-利润表，"cashflow"-现金流量表
     * @return DataFrame 包含财务报表数据
     */
    public DataFrame stockFinancialReportEm(String symbol, String reportType, String reportName) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockFinancialReportEm", symbol, reportType, reportName);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票[{}]的财务报表数据，类型：{}，报表：{}", symbol, reportType, reportName);

        // 验证股票代码
        if (!StockCodeUtils.isValidStockCode(symbol)) {
            log.error("无效的股票代码：{}", symbol);
            return new DataFrameImpl();
        }

        // 获取交易所代码并构建secid
        String exchange = StockCodeUtils.getExchange(symbol);
        String secid = ("sh".equals(exchange) ? "1" : "0") + "." + symbol;

        // 根据报表名称确定报表类型参数
        String reportTypeParam = getReportTypeParam(reportName);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "REPORT_DATE");
        params.put("sortTypes", "-1");
        params.put("pageSize", "500");
        params.put("pageNumber", "1");
        params.put("reportName", reportTypeParam);
        params.put("columns", "ALL");
        params.put("filter", "(SECURITY_CODE=\"" + symbol + "\")");

        // 发送HTTP请求获取数据
        String response = httpClient.get(EASTMONEY_BALANCE_SHEET_URL, params, null);

        // 解析响应数据
        DataFrame df = parseFinancialReportData(response, reportName);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 获取财务分析指标数据
     * 包括：盈利能力、偿债能力、成长能力、营运能力等指标
     * 从资产负债表和利润表中提取关键指标
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含财务分析指标数据
     */
    public DataFrame stockFinancialAnalysisEm(String symbol, String reportType) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockFinancialAnalysisEm", symbol, reportType);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票[{}]的财务分析指标数据，类型：{}", symbol, reportType);

        // 验证股票代码
        if (!StockCodeUtils.isValidStockCode(symbol)) {
            log.error("无效的股票代码：{}", symbol);
            return new DataFrameImpl();
        }

        // 同时获取资产负债表和利润表数据
        DataFrame balanceSheet = stockBalanceSheetEm(symbol, reportType);
        DataFrame profitSheet = stockProfitSheetEm(symbol, reportType);
        
        if ((balanceSheet == null || balanceSheet.isEmpty()) && (profitSheet == null || profitSheet.isEmpty())) {
            log.warn("无法获取财务数据，无法计算财务分析指标");
            return new DataFrameImpl();
        }

        // 从财务报表中提取关键财务指标
        DataFrame df = extractFinancialIndicators(balanceSheet, profitSheet);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 从财务报表数据中提取财务分析指标
     *
     * @param balanceSheet 资产负债表数据
     * @param profitSheet 利润表数据
     * @return DataFrame 包含财务分析指标
     */
    private DataFrame extractFinancialIndicators(DataFrame balanceSheet, DataFrame profitSheet) {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        // 确定数据行数
        int rowCount = 0;
        if (balanceSheet != null && !balanceSheet.isEmpty()) {
            rowCount = balanceSheet.rowCount();
        } else if (profitSheet != null && !profitSheet.isEmpty()) {
            rowCount = profitSheet.rowCount();
        }

        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> row = new HashMap<>();

            // 基本信息 - 优先从balanceSheet获取
            if (balanceSheet != null && !balanceSheet.isEmpty() && i < balanceSheet.rowCount()) {
                row.put("股票代码", balanceSheet.get(i, "SECURITY_CODE"));
                row.put("股票简称", balanceSheet.get(i, "SECURITY_NAME_ABBR"));
                row.put("报告期", balanceSheet.get(i, "REPORT_DATE"));
            } else if (profitSheet != null && !profitSheet.isEmpty() && i < profitSheet.rowCount()) {
                row.put("股票代码", profitSheet.get(i, "SECURITY_CODE"));
                row.put("股票简称", profitSheet.get(i, "SECURITY_NAME_ABBR"));
                row.put("报告期", profitSheet.get(i, "REPORT_DATE"));
            }

            // 偿债能力指标 - 从资产负债表获取
            if (balanceSheet != null && !balanceSheet.isEmpty() && i < balanceSheet.rowCount()) {
                row.put("资产负债率", balanceSheet.get(i, "DEBT_ASSET_RATIO"));
                row.put("股东权益比率", balanceSheet.get(i, "TOTAL_EQUITY_RATIO"));
                row.put("流动比率", balanceSheet.get(i, "CURRENT_RATIO"));
                row.put("速动比率", balanceSheet.get(i, "QUICK_RATIO"));
                row.put("权益乘数", balanceSheet.get(i, "EQUITY_RATIO"));
                
                // 资产规模指标
                row.put("总资产", balanceSheet.get(i, "TOTAL_ASSETS"));
                row.put("总负债", balanceSheet.get(i, "TOTAL_LIABILITIES"));
                row.put("股东权益", balanceSheet.get(i, "TOTAL_EQUITY"));
            }

            // 盈利能力指标 - 从利润表获取
            if (profitSheet != null && !profitSheet.isEmpty() && i < profitSheet.rowCount()) {
                // 盈利能力指标
                row.put("净资产收益率", profitSheet.get(i, "ROE"));
                row.put("摊薄净资产收益率", profitSheet.get(i, "ROE_DILUTED"));
                row.put("总资产报酬率", profitSheet.get(i, "ROA"));
                row.put("销售净利率", profitSheet.get(i, "NETPROFIT_MARGIN"));
                row.put("销售毛利率", profitSheet.get(i, "GROSSPROFIT_MARGIN"));
                
                // 营收和利润数据
                row.put("营业总收入", profitSheet.get(i, "TOTAL_OPERATE_INCOME"));
                row.put("营业收入", profitSheet.get(i, "OPERATE_INCOME"));
                row.put("净利润", profitSheet.get(i, "NETPROFIT"));
                row.put("归母净利润", profitSheet.get(i, "DEDUCT_PARENT_NETPROFIT"));
                row.put("基本每股收益", profitSheet.get(i, "BASIC_EPS"));
            }

            // 计算同比增长率（如果有上期数据）
            if (balanceSheet != null && !balanceSheet.isEmpty() && i < balanceSheet.rowCount() - 1) {
                Object currentAssets = balanceSheet.get(i, "TOTAL_ASSETS");
                Object prevAssets = balanceSheet.get(i + 1, "TOTAL_ASSETS");
                if (currentAssets != null && prevAssets != null) {
                    try {
                        double current = Double.parseDouble(currentAssets.toString());
                        double prev = Double.parseDouble(prevAssets.toString());
                        if (prev != 0) {
                            double growthRate = (current - prev) / prev * 100;
                            row.put("总资产增长率", growthRate);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略计算错误
                    }
                }
            }
            
            // 计算营收增长率和净利润增长率（从利润表）
            if (profitSheet != null && !profitSheet.isEmpty() && i < profitSheet.rowCount() - 1) {
                // 营收增长率
                Object currentRevenue = profitSheet.get(i, "TOTAL_OPERATE_INCOME");
                Object prevRevenue = profitSheet.get(i + 1, "TOTAL_OPERATE_INCOME");
                if (currentRevenue != null && prevRevenue != null) {
                    try {
                        double current = Double.parseDouble(currentRevenue.toString());
                        double prev = Double.parseDouble(prevRevenue.toString());
                        if (prev != 0) {
                            double growthRate = (current - prev) / prev * 100;
                            row.put("营业收入增长率", growthRate);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略计算错误
                    }
                }
                
                // 净利润增长率
                Object currentProfit = profitSheet.get(i, "NETPROFIT");
                Object prevProfit = profitSheet.get(i + 1, "NETPROFIT");
                if (currentProfit != null && prevProfit != null) {
                    try {
                        double current = Double.parseDouble(currentProfit.toString());
                        double prev = Double.parseDouble(prevProfit.toString());
                        if (prev != 0) {
                            double growthRate = (current - prev) / prev * 100;
                            row.put("净利润增长率", growthRate);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略计算错误
                    }
                }
            }
            
            // 计算营运能力指标（周转率）
            if (balanceSheet != null && !balanceSheet.isEmpty() && profitSheet != null && !profitSheet.isEmpty() 
                && i < balanceSheet.rowCount() && i < profitSheet.rowCount()) {
                try {
                    // 总资产周转率 = 营业收入 / 平均总资产
                    Object revenue = profitSheet.get(i, "TOTAL_OPERATE_INCOME");
                    Object totalAssets = balanceSheet.get(i, "TOTAL_ASSETS");
                    if (revenue != null && totalAssets != null) {
                        double rev = Double.parseDouble(revenue.toString());
                        double assets = Double.parseDouble(totalAssets.toString());
                        if (assets != 0) {
                            row.put("总资产周转率", rev / assets);
                        }
                    }
                    
                    // 存货周转率 = 营业成本 / 平均存货
                    Object operateCost = profitSheet.get(i, "OPERATE_COST");
                    Object inventory = balanceSheet.get(i, "INVENTORY");
                    if (operateCost != null && inventory != null) {
                        double cost = Double.parseDouble(operateCost.toString());
                        double inv = Double.parseDouble(inventory.toString());
                        if (inv != 0) {
                            row.put("存货周转率", cost / inv);
                        }
                    }
                    
                    // 应收账款周转率 = 营业收入 / 平均应收账款
                    Object accountsRece = balanceSheet.get(i, "ACCOUNTS_RECE");
                    if (revenue != null && accountsRece != null) {
                        double rev = Double.parseDouble(revenue.toString());
                        double rece = Double.parseDouble(accountsRece.toString());
                        if (rece != 0) {
                            row.put("应收账款周转率", rev / rece);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略计算错误
                }
            }

            rows.add(row);
        }

        log.info("成功提取财务分析指标数据，共{}条记录", rows.size());
        return DataFrameImpl.fromList(rows);
    }

    // ==================== 具体报表接口 ====================

    /**
     * 获取利润表数据
     * 从东方财富网获取指定股票的利润表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含利润表数据
     */
    public DataFrame stockProfitSheetEm(String symbol, String reportType) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockProfitSheetEm", symbol, reportType);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票[{}]的利润表数据，类型：{}", symbol, reportType);

        // 验证股票代码
        if (!StockCodeUtils.isValidStockCode(symbol)) {
            log.error("无效的股票代码：{}", symbol);
            return new DataFrameImpl();
        }

        // 根据报表类型确定报表名称
        String reportName = getProfitSheetReportName(reportType);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "REPORT_DATE");
        params.put("sortTypes", "-1");
        params.put("pageSize", "500");
        params.put("pageNumber", "1");
        params.put("reportName", reportName);
        params.put("columns", "ALL");
        params.put("filter", "(SECURITY_CODE=\"" + symbol + "\")");

        // 发送HTTP请求获取数据
        String response = httpClient.get(EASTMONEY_PROFIT_SHEET_URL, params, null);

        // 解析响应数据
        DataFrame df = parseProfitSheetData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 获取资产负债表数据
     * 从东方财富网获取指定股票的资产负债表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含资产负债表数据
     */
    public DataFrame stockBalanceSheetEm(String symbol, String reportType) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockBalanceSheetEm", symbol, reportType);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票[{}]的资产负债表数据，类型：{}", symbol, reportType);

        // 验证股票代码
        if (!StockCodeUtils.isValidStockCode(symbol)) {
            log.error("无效的股票代码：{}", symbol);
            return new DataFrameImpl();
        }

        // 根据报表类型确定报表名称
        String reportName = getBalanceSheetReportName(reportType);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "REPORT_DATE");
        params.put("sortTypes", "-1");
        params.put("pageSize", "500");
        params.put("pageNumber", "1");
        params.put("reportName", reportName);
        params.put("columns", "ALL");
        params.put("filter", "(SECURITY_CODE=\"" + symbol + "\")");

        // 发送HTTP请求获取数据
        String response = httpClient.get(EASTMONEY_BALANCE_SHEET_URL, params, null);

        // 解析响应数据
        DataFrame df = parseBalanceSheetData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 获取现金流量表数据
     * 从东方财富网获取指定股票的现金流量表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含现金流量表数据
     */
    public DataFrame stockCashFlowSheetEm(String symbol, String reportType) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockCashFlowSheetEm", symbol, reportType);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        log.info("正在获取股票[{}]的现金流量表数据，类型：{}", symbol, reportType);

        // 验证股票代码
        if (!StockCodeUtils.isValidStockCode(symbol)) {
            log.error("无效的股票代码：{}", symbol);
            return new DataFrameImpl();
        }

        // 根据报表类型确定报表名称
        String reportName = getCashFlowReportName(reportType);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "REPORT_DATE");
        params.put("sortTypes", "-1");
        params.put("pageSize", "500");
        params.put("pageNumber", "1");
        params.put("reportName", reportName);
        params.put("columns", "ALL");
        params.put("filter", "(SECURITY_CODE=\"" + symbol + "\")");

        // 发送HTTP请求获取数据
        String response = httpClient.get(EASTMONEY_CASH_FLOW_URL, params, null);

        // 解析响应数据
        DataFrame df = parseCashFlowSheetData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    // ==================== 辅助方法：获取报表名称 ====================

    /**
     * 根据报表名称获取对应的东方财富报表类型参数
     *
     * @param reportName 报表名称
     * @return 东方财富报表类型参数
     */
    private String getReportTypeParam(String reportName) {
        if (reportName == null) {
            return "RPT_DMSK_FN_BALANCE";
        }
        switch (reportName.toLowerCase()) {
            case "balancesheet":
            case "balance":
            case "资产负债表":
                return "RPT_DMSK_FN_BALANCE";
            case "incomesheet":
            case "income":
            case "利润表":
                return "RPT_DMSK_FN_INCOME";
            case "cashflow":
            case "cash":
            case "现金流量表":
                return "RPT_DMSK_FN_CASHFLOW";
            default:
                return "RPT_DMSK_FN_BALANCE";
        }
    }

    /**
     * 根据报表类型获取利润表报表名称
     *
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return 利润表报表名称
     */
    private String getProfitSheetReportName(String reportType) {
        if (reportType == null) {
            return "RPT_DMSK_FN_INCOME";
        }
        switch (reportType) {
            case REPORT_TYPE_YEAR:
                return "RPT_DMSK_FN_INCOME_YEAR";
            case REPORT_TYPE_QUARTER:
                return "RPT_DMSK_FN_INCOME_QUARTER";
            case REPORT_TYPE_REPORT:
            default:
                return "RPT_DMSK_FN_INCOME";
        }
    }

    /**
     * 根据报表类型获取资产负债表报表名称
     *
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return 资产负债表报表名称
     */
    private String getBalanceSheetReportName(String reportType) {
        if (reportType == null) {
            return "RPT_DMSK_FN_BALANCE";
        }
        switch (reportType) {
            case REPORT_TYPE_YEAR:
                return "RPT_DMSK_FN_BALANCE_YEAR";
            case REPORT_TYPE_QUARTER:
                return "RPT_DMSK_FN_BALANCE_QUARTER";
            case REPORT_TYPE_REPORT:
            default:
                return "RPT_DMSK_FN_BALANCE";
        }
    }

    /**
     * 根据报表类型获取现金流量表报表名称
     *
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return 现金流量表报表名称
     */
    private String getCashFlowReportName(String reportType) {
        if (reportType == null) {
            return "RPT_DMSK_FN_CASHFLOW";
        }
        switch (reportType) {
            case REPORT_TYPE_YEAR:
                return "RPT_DMSK_FN_CASHFLOW_YEAR";
            case REPORT_TYPE_QUARTER:
                return "RPT_DMSK_FN_CASHFLOW_QUARTER";
            case REPORT_TYPE_REPORT:
            default:
                return "RPT_DMSK_FN_CASHFLOW";
        }
    }

    // ==================== 数据解析方法 ====================

    /**
     * 解析财务报表数据
     *
     * @param response HTTP响应字符串
     * @param reportName 报表名称
     * @return DataFrame 包含解析后的数据
     */
    private DataFrame parseFinancialReportData(String response, String reportName) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            // 检查结果是否存在
            if (resultNode == null || resultNode.isNull()) {
                log.warn("财务报表响应中未找到result数据");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("财务报表响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 解析数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode item : dataNode) {
                Map<String, Object> row = parseJsonNodeToMap(item);
                rows.add(row);
            }

            log.info("成功解析财务报表数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析财务报表数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 解析财务分析指标数据
     *
     * @param response HTTP响应字符串
     * @return DataFrame 包含解析后的财务分析指标数据
     */
    private DataFrame parseFinancialAnalysisData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            // 检查结果是否存在
            if (resultNode == null || resultNode.isNull()) {
                log.warn("财务分析指标响应中未找到result数据");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("财务分析指标响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 解析数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode item : dataNode) {
                Map<String, Object> row = parseJsonNodeToMap(item);
                // 添加中文列名映射
                row = mapFinancialAnalysisColumns(row);
                rows.add(row);
            }

            log.info("成功解析财务分析指标数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析财务分析指标数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 解析利润表数据
     *
     * @param response HTTP响应字符串
     * @return DataFrame 包含解析后的利润表数据
     */
    private DataFrame parseProfitSheetData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            // 检查结果是否存在
            if (resultNode == null || resultNode.isNull()) {
                log.warn("利润表响应中未找到result数据");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("利润表响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 解析数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode item : dataNode) {
                Map<String, Object> row = parseJsonNodeToMap(item);
                // 添加中文列名映射
                row = mapProfitSheetColumns(row);
                rows.add(row);
            }

            log.info("成功解析利润表数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析利润表数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 解析资产负债表数据
     *
     * @param response HTTP响应字符串
     * @return DataFrame 包含解析后的资产负债表数据
     */
    private DataFrame parseBalanceSheetData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            // 检查结果是否存在
            if (resultNode == null || resultNode.isNull()) {
                log.warn("资产负债表响应中未找到result数据");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("资产负债表响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 解析数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode item : dataNode) {
                Map<String, Object> row = parseJsonNodeToMap(item);
                // 添加中文列名映射
                row = mapBalanceSheetColumns(row);
                rows.add(row);
            }

            log.info("成功解析资产负债表数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析资产负债表数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 解析现金流量表数据
     *
     * @param response HTTP响应字符串
     * @return DataFrame 包含解析后的现金流量表数据
     */
    private DataFrame parseCashFlowSheetData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");

            // 检查结果是否存在
            if (resultNode == null || resultNode.isNull()) {
                log.warn("现金流量表响应中未找到result数据");
                return new DataFrameImpl();
            }

            // 获取数据列表
            JsonNode dataNode = resultNode.path("data");
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("现金流量表响应中未找到data数组");
                return new DataFrameImpl();
            }

            // 解析数据行
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode item : dataNode) {
                Map<String, Object> row = parseJsonNodeToMap(item);
                // 添加中文列名映射
                row = mapCashFlowSheetColumns(row);
                rows.add(row);
            }

            log.info("成功解析现金流量表数据，共{}条记录", rows.size());
            return DataFrameImpl.fromList(rows);

        } catch (Exception e) {
            log.error("解析现金流量表数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 将JsonNode解析为Map对象
     *
     * @param node JSON节点
     * @return 包含字段名和值的Map
     */
    private Map<String, Object> parseJsonNodeToMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        // 遍历JSON节点的所有字段
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode valueNode = node.get(fieldName);
            // 根据节点类型转换为对应的Java对象
            if (valueNode.isNull()) {
                map.put(fieldName, null);
            } else if (valueNode.isNumber()) {
                map.put(fieldName, valueNode.numberValue());
            } else if (valueNode.isBoolean()) {
                map.put(fieldName, valueNode.booleanValue());
            } else {
                map.put(fieldName, valueNode.asText());
            }
        }
        return map;
    }

    // ==================== 列名映射方法 ====================

    /**
     * 映射利润表列为中文名称
     *
     * @param row 原始数据行
     * @return 添加中文列名后的数据行
     */
    private Map<String, Object> mapProfitSheetColumns(Map<String, Object> row) {
        Map<String, Object> mappedRow = new HashMap<>(row);
        // 添加常用的中文列名映射
        putIfExists(row, mappedRow, "SECURITY_CODE", "股票代码");
        putIfExists(row, mappedRow, "SECURITY_NAME_ABBR", "股票名称");
        putIfExists(row, mappedRow, "REPORT_DATE", "报告期");
        putIfExists(row, mappedRow, "TOTAL_OPERATE_INCOME", "营业总收入");
        putIfExists(row, mappedRow, "TOTAL_OPERATE_INCOME_SQ", "营业总收入同比");
        putIfExists(row, mappedRow, "OPERATE_INCOME", "营业收入");
        putIfExists(row, mappedRow, "OPERATE_INCOME_SQ", "营业收入同比");
        putIfExists(row, mappedRow, "TOTAL_OPERATE_COST", "营业总成本");
        putIfExists(row, mappedRow, "OPERATE_COST", "营业成本");
        putIfExists(row, mappedRow, "SALE_EXPENSE", "销售费用");
        putIfExists(row, mappedRow, "MANAGE_EXPENSE", "管理费用");
        putIfExists(row, mappedRow, "FINANCE_EXPENSE", "财务费用");
        putIfExists(row, mappedRow, "OPERATE_PROFIT", "营业利润");
        putIfExists(row, mappedRow, "OPERATE_PROFIT_SQ", "营业利润同比");
        putIfExists(row, mappedRow, "TOTAL_PROFIT", "利润总额");
        putIfExists(row, mappedRow, "INCOME_TAX", "所得税费用");
        putIfExists(row, mappedRow, "NETPROFIT", "净利润");
        putIfExists(row, mappedRow, "NETPROFIT_SQ", "净利润同比");
        putIfExists(row, mappedRow, "DEDUCT_PARENT_NETPROFIT", "归母净利润");
        putIfExists(row, mappedRow, "DEDUCT_PARENT_NETPROFIT_SQ", "归母净利润同比");
        putIfExists(row, mappedRow, "BASIC_EPS", "基本每股收益");
        putIfExists(row, mappedRow, "DILUTED_EPS", "稀释每股收益");
        return mappedRow;
    }

    /**
     * 映射资产负债表列为中文名称
     *
     * @param row 原始数据行
     * @return 添加中文列名后的数据行
     */
    private Map<String, Object> mapBalanceSheetColumns(Map<String, Object> row) {
        Map<String, Object> mappedRow = new HashMap<>(row);
        // 添加常用的中文列名映射
        putIfExists(row, mappedRow, "SECURITY_CODE", "股票代码");
        putIfExists(row, mappedRow, "SECURITY_NAME_ABBR", "股票名称");
        putIfExists(row, mappedRow, "REPORT_DATE", "报告期");
        putIfExists(row, mappedRow, "TOTAL_ASSETS", "资产总计");
        putIfExists(row, mappedRow, "TOTAL_ASSETS_SQ", "资产总计同比");
        putIfExists(row, mappedRow, "TOTAL_LIABILITIES", "负债合计");
        putIfExists(row, mappedRow, "TOTAL_LIABILITIES_SQ", "负债合计同比");
        putIfExists(row, mappedRow, "TOTAL_EQUITY", "股东权益合计");
        putIfExists(row, mappedRow, "TOTAL_EQUITY_SQ", "股东权益同比");
        putIfExists(row, mappedRow, "MONETARYFUNDS", "货币资金");
        putIfExists(row, mappedRow, "ACCOUNTS_RECE", "应收账款");
        putIfExists(row, mappedRow, "INVENTORY", "存货");
        putIfExists(row, mappedRow, "TOTAL_CURRENT_ASSETS", "流动资产合计");
        putIfExists(row, mappedRow, "TOTAL_NONCURRENT_ASSETS", "非流动资产合计");
        putIfExists(row, mappedRow, "SHORTTERM_LOAN", "短期借款");
        putIfExists(row, mappedRow, "ACCOUNTS_PAYABLE", "应付账款");
        putIfExists(row, mappedRow, "TOTAL_CURRENT_LIABILITIES", "流动负债合计");
        putIfExists(row, mappedRow, "TOTAL_NONCURRENT_LIABILITIES", "非流动负债合计");
        putIfExists(row, mappedRow, "SURPLUS_RESERVE", "盈余公积");
        putIfExists(row, mappedRow, "UNASSIGN_RPOFIT", "未分配利润");
        return mappedRow;
    }

    /**
     * 映射现金流量表列为中文名称
     *
     * @param row 原始数据行
     * @return 添加中文列名后的数据行
     */
    private Map<String, Object> mapCashFlowSheetColumns(Map<String, Object> row) {
        Map<String, Object> mappedRow = new HashMap<>(row);
        // 添加常用的中文列名映射
        putIfExists(row, mappedRow, "SECURITY_CODE", "股票代码");
        putIfExists(row, mappedRow, "SECURITY_NAME_ABBR", "股票名称");
        putIfExists(row, mappedRow, "REPORT_DATE", "报告期");
        putIfExists(row, mappedRow, "NETCASH_OPERATE", "经营活动现金流净额");
        putIfExists(row, mappedRow, "NETCASH_OPERATE_SQ", "经营现金流同比");
        putIfExists(row, mappedRow, "CASH_INVEST", "投资活动现金流净额");
        putIfExists(row, mappedRow, "CASH_FINANCE", "筹资活动现金流净额");
        putIfExists(row, mappedRow, "CASH_CHANGE", "现金及等价物净增加额");
        putIfExists(row, mappedRow, "SALES_SERVICES", "销售商品提供劳务收到的现金");
        putIfExists(row, mappedRow, "PAY_STAFF_CASH", "支付给职工以及为职工支付的现金");
        putIfExists(row, mappedRow, "PAY_ALL_TAX", "支付的各项税费");
        putIfExists(row, mappedRow, "NETCASH_OPERATENOTE", "经营活动现金流净额备注");
        putIfExists(row, mappedRow, "FIXED_ASSET", "购建固定资产支付的现金");
        putIfExists(row, mappedRow, "INVEST_INCOME", "取得投资收益收到的现金");
        putIfExists(row, mappedRow, "END_CASH", "期末现金及现金等价物余额");
        putIfExists(row, mappedRow, "BEGIN_CASH", "期初现金及现金等价物余额");
        return mappedRow;
    }

    /**
     * 映射财务分析指标列为中文名称
     *
     * @param row 原始数据行
     * @return 添加中文列名后的数据行
     */
    private Map<String, Object> mapFinancialAnalysisColumns(Map<String, Object> row) {
        Map<String, Object> mappedRow = new HashMap<>(row);
        // 添加常用的中文列名映射
        putIfExists(row, mappedRow, "SECURITY_CODE", "股票代码");
        putIfExists(row, mappedRow, "SECURITY_NAME_ABBR", "股票名称");
        putIfExists(row, mappedRow, "REPORT_DATE", "报告期");
        // 盈利能力指标
        putIfExists(row, mappedRow, "ROE", "净资产收益率ROE");
        putIfExists(row, mappedRow, "ROE_DILUTED", "摊薄净资产收益率");
        putIfExists(row, mappedRow, "ROA", "总资产报酬率");
        putIfExists(row, mappedRow, "NETPROFIT_MARGIN", "销售净利率");
        putIfExists(row, mappedRow, "GROSSPROFIT_MARGIN", "销售毛利率");
        // 偿债能力指标
        putIfExists(row, mappedRow, "DEBT_ASSET_RATIO", "资产负债率");
        putIfExists(row, mappedRow, "CURRENT_RATIO", "流动比率");
        putIfExists(row, mappedRow, "QUICK_RATIO", "速动比率");
        putIfExists(row, mappedRow, "EQUITY_RATIO", "权益乘数");
        // 成长能力指标
        putIfExists(row, mappedRow, "REVENUE_GROWTH", "营业收入增长率");
        putIfExists(row, mappedRow, "NETPROFIT_GROWTH", "净利润增长率");
        putIfExists(row, mappedRow, "TOTAL_ASSETS_GROWTH", "总资产增长率");
        putIfExists(row, mappedRow, "EQUITY_GROWTH", "净资产增长率");
        // 营运能力指标
        putIfExists(row, mappedRow, "TOTAL_ASSETS_TURNOVER", "总资产周转率");
        putIfExists(row, mappedRow, "RECEIVABLES_TURNOVER", "应收账款周转率");
        putIfExists(row, mappedRow, "INVENTORY_TURNOVER", "存货周转率");
        putIfExists(row, mappedRow, "CURRENT_ASSETS_TURNOVER", "流动资产周转率");
        return mappedRow;
    }

    /**
     * 如果源Map中存在指定键，则将其值复制到目标Map
     *
     * @param source 源Map
     * @param target 目标Map
     * @param sourceKey 源键名
     * @param targetKey 目标键名
     */
    private void putIfExists(Map<String, Object> source, Map<String, Object> target, String sourceKey, String targetKey) {
        if (source.containsKey(sourceKey)) {
            target.put(targetKey, source.get(sourceKey));
        }
    }
}
