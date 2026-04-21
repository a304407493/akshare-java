package com.akshare.stock.service;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票机构数据服务类
 * 提供龙虎榜、机构买卖、营业部排行等机构相关数据
 * 数据源：东方财富网
 */
public class StockInstitutionService {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(StockInstitutionService.class);

    /** HTTP客户端，用于发送网络请求 */
    private final HttpClient httpClient;
    /** JSON解析器，用于解析响应数据 */
    private final JsonParser jsonParser;
    /** 缓存管理器，用于缓存数据 */
    private final CacheManager cacheManager;
    /** JSON对象映射器 */
    private final ObjectMapper objectMapper;

    /** 东方财富龙虎榜详情API地址 */
    private static final String LHB_DETAIL_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 东方财富个股龙虎榜API地址 */
    private static final String LHB_STOCK_DETAIL_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 东方财富机构买卖统计API地址 */
    private static final String LHB_JGMM_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";
    /** 东方财富营业部排行API地址 */
    private static final String LHB_YYBPH_URL = "https://datacenter-web.eastmoney.com/api/data/v1/get";

    /**
     * 构造函数
     * @param httpClient HTTP客户端
     * @param jsonParser JSON解析器
     * @param cacheManager 缓存管理器
     */
    public StockInstitutionService(HttpClient httpClient, JsonParser jsonParser, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取龙虎榜详情数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-龙虎榜详情
     *
     * @param startDate 开始日期，格式：yyyyMMdd，如：20220314
     * @param endDate 结束日期，格式：yyyyMMdd，如：20220315
     * @return DataFrame 包含龙虎榜详情的数据框
     *         字段包括：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    public DataFrame stockLhbDetailEm(String startDate, String endDate) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockLhbDetailEm", startDate, endDate);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取龙虎榜详情数据，startDate={}, endDate={}", startDate, endDate);
            return cached;
        }

        log.info("正在从东方财富获取龙虎榜详情数据，startDate={}, endDate={}", startDate, endDate);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "NET_BUY_AMT,TRADE_DATE,SECURITY_CODE"); // 排序字段：净买额、交易日期、证券代码
        params.put("sortTypes", "-1,-1,1"); // 排序方式：降序、降序、升序
        params.put("pageSize", "500"); // 每页条数
        params.put("pageNumber", "1"); // 页码
        params.put("reportName", "RPT_ORGANIZATION_TRADE_DETAILS"); // 报表名称
        params.put("columns", "ALL"); // 返回所有列
        params.put("source", "WEB"); // 数据来源
        params.put("client", "WEB"); // 客户端类型
        // 添加日期过滤条件
        params.put("filter", "(TRADE_DATE>='" + formatDate(startDate) + "')(TRADE_DATE<='" + formatDate(endDate) + "')");

        // 发送HTTP GET请求
        String response = httpClient.get(LHB_DETAIL_URL, params, null);

        // 解析响应数据
        DataFrame df = parseLhbDetailData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 获取个股龙虎榜详情数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-个股龙虎榜详情
     *
     * @param symbol 股票代码，如：000001
     * @param date 交易日期，格式：yyyyMMdd，如：20220314
     * @param flag 买卖方向，可选值："买入"、"卖出"
     * @return DataFrame 包含个股龙虎榜详情的数据框
     *         字段包括：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    public DataFrame stockLhbStockDetailEm(String symbol, String date, String flag) {
        // 参数校验
        if (symbol == null || symbol.isEmpty()) {
            log.error("股票代码不能为空");
            return new DataFrameImpl();
        }
        if (date == null || date.isEmpty()) {
            log.error("日期不能为空");
            return new DataFrameImpl();
        }

        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockLhbStockDetailEm", symbol, date, flag);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取个股龙虎榜详情数据，symbol={}, date={}, flag={}", symbol, date, flag);
            return cached;
        }

        log.info("正在从东方财富获取个股龙虎榜详情数据，symbol={}, date={}, flag={}", symbol, date, flag);

        // 格式化日期
        String formattedDate = formatDate(date);
        // 使用同一天作为开始和结束日期
        DataFrame allData = stockLhbDetailEm(formattedDate, formattedDate);
        
        // 过滤出特定股票的数据
        DataFrame filteredData = filterDataBySymbol(allData, symbol);
        
        // 将结果存入缓存
        cacheManager.put(cacheKey, filteredData);
        return filteredData;
    }
    
    /**
     * 根据股票代码过滤DataFrame数据
     * 
     * @param dataFrame 原始DataFrame
     * @param symbol 股票代码
     * @return 过滤后的DataFrame
     */
    private DataFrame filterDataBySymbol(DataFrame dataFrame, String symbol) {
        if (dataFrame == null || dataFrame.isEmpty()) {
            return new DataFrameImpl();
        }
        
        List<Map<String, Object>> rows = dataFrame.toList();
        List<Map<String, Object>> filteredRows = new ArrayList<>();
        
        for (Map<String, Object> row : rows) {
            Object codeObj = row.get("代码");
            if (codeObj != null && symbol.equals(codeObj.toString())) {
                filteredRows.add(row);
            }
        }
        
        return DataFrameImpl.fromList(filteredRows);
    }

    /**
     * 获取机构买卖统计数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-机构买卖统计
     *
     * @param startDate 开始日期，格式：yyyyMMdd，如：20220314
     * @param endDate 结束日期，格式：yyyyMMdd，如：20220315
     * @return DataFrame 包含机构买卖统计的数据框
     *         字段包括：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    public DataFrame stockLhbJgmmEm(String startDate, String endDate) {
        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockLhbJgmmEm", startDate, endDate);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取机构买卖统计数据，startDate={}, endDate={}", startDate, endDate);
            return cached;
        }

        log.info("正在从东方财富获取机构买卖统计数据，startDate={}, endDate={}", startDate, endDate);

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "NET_BUY_AMT,TRADE_DATE,SECURITY_CODE"); // 排序字段：净买额、交易日期、证券代码
        params.put("sortTypes", "-1,-1,1"); // 排序方式：降序、降序、升序
        params.put("pageSize", "500"); // 每页条数
        params.put("pageNumber", "1"); // 页码
        params.put("reportName", "RPT_ORGANIZATION_TRADE_DETAILS"); // 报表名称
        params.put("columns", "ALL"); // 返回所有列
        params.put("source", "WEB"); // 数据来源
        params.put("client", "WEB"); // 客户端类型
        // 添加日期过滤条件
        String filter = "(TRADE_DATE>='" + formatDate(startDate) + "')(TRADE_DATE<='" + formatDate(endDate) + "')";
        params.put("filter", filter);

        // 发送HTTP GET请求
        String response = httpClient.get(LHB_JGMM_URL, params, null);

        // 解析响应数据
        DataFrame df = parseLhbDetailData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 获取营业部排行数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-营业部排行
     *
     * @param symbol 时间周期，可选值："近一月"、"近三月"、"近六月"、"近一年"
     * @return DataFrame 包含营业部排行的数据框
     *         字段包括：序号、营业部名称、上榜次数、买入个股数、卖出个股数、
     *         买入总金额、卖出总金额、总买卖净额、买入股票
     */
    public DataFrame stockLhbYybphEm(String symbol) {
        // 参数校验
        if (symbol == null || symbol.isEmpty()) {
            symbol = "近一月"; // 默认查询近一月数据
        }

        // 生成缓存键
        String cacheKey = cacheManager.generateKey("stockLhbYybphEm", symbol);
        // 尝试从缓存获取数据
        DataFrame cached = cacheManager.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取营业部排行数据，symbol={}", symbol);
            return cached;
        }

        log.info("正在从东方财富获取营业部排行数据，symbol={}", symbol);

        // 将时间周期转换为日期范围
        String[] dateRange = convertPeriodToDateRange(symbol);
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("sortColumns", "TOTAL_NETAMT"); // 排序字段：总买卖净额
        params.put("sortTypes", "-1"); // 排序方式：降序
        params.put("pageSize", "500"); // 每页条数
        params.put("pageNumber", "1"); // 页码
        params.put("reportName", "RPT_LHB_YYBPH"); // 报表名称：营业部排行
        params.put("columns", "ALL"); // 返回所有列
        params.put("source", "WEB"); // 数据来源
        params.put("client", "WEB"); // 客户端类型
        // 添加日期过滤条件
        params.put("filter", "(TRADE_DATE>='" + startDate + "')(TRADE_DATE<='" + endDate + "')");

        // 发送HTTP GET请求
        String response = httpClient.get(LHB_YYBPH_URL, params, null);

        // 解析响应数据
        DataFrame df = parseYybphData(response);

        // 将结果存入缓存
        cacheManager.put(cacheKey, df);
        return df;
    }

    /**
     * 解析龙虎榜详情数据
     * @param response HTTP响应字符串
     * @return DataFrame 解析后的数据框
     */
    private DataFrame parseLhbDetailData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");
            // 获取数据列表节点
            JsonNode dataNode = resultNode.path("data");

            // 检查数据是否为空
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("东方财富龙虎榜响应中未找到数据");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 遍历数据节点
            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // 映射字段数据
                row.put("序号", getIntValue(item, "SERIAL_NUM")); // 序号
                row.put("代码", getStringValue(item, "SECURITY_CODE")); // 证券代码
                row.put("名称", getStringValue(item, "SECURITY_NAME_ABBR")); // 证券简称
                row.put("上榜日", getStringValue(item, "TRADE_DATE")); // 上榜日期
                row.put("解读", getStringValue(item, "EXPLANATION")); // 解读
                row.put("收盘价", getDoubleValue(item, "CLOSE_PRICE")); // 收盘价
                row.put("涨跌幅", getDoubleValue(item, "CHANGE_RATE")); // 涨跌幅
                row.put("龙虎榜净买额", getDoubleValue(item, "NET_AMT")); // 龙虎榜净买额
                row.put("龙虎榜买入额", getDoubleValue(item, "BUY_AMT")); // 龙虎榜买入额
                row.put("龙虎榜卖出额", getDoubleValue(item, "SELL_AMT")); // 龙虎榜卖出额
                row.put("龙虎榜成交额", getDoubleValue(item, "TOTAL_AMT")); // 龙虎榜成交额
                row.put("市场总成交额", getDoubleValue(item, "MARKET_AMT")); // 市场总成交额
                row.put("净买额占总成交比", getDoubleValue(item, "NET_RATE")); // 净买额占总成交比
                row.put("成交额占总成交比", getDoubleValue(item, "TURNOVER_RATE")); // 成交额占总成交比
                row.put("换手率", getDoubleValue(item, "TURNOVERRATE")); // 换手率
                row.put("流通市值", getDoubleValue(item, "FREE_MARKET_CAP")); // 流通市值
                row.put("上榜原因", getStringValue(item, "REASON")); // 上榜原因
                row.put("上榜后1日", getDoubleValue(item, "AFTER_1DAY")); // 上榜后1日涨跌幅
                row.put("上榜后2日", getDoubleValue(item, "AFTER_2DAY")); // 上榜后2日涨跌幅
                row.put("上榜后5日", getDoubleValue(item, "AFTER_5DAY")); // 上榜后5日涨跌幅
                row.put("上榜后10日", getDoubleValue(item, "AFTER_10DAY")); // 上榜后10日涨跌幅

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (IOException e) {
            log.error("解析东方财富龙虎榜详情数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 解析营业部排行数据
     * @param response HTTP响应字符串
     * @return DataFrame 解析后的数据框
     */
    private DataFrame parseYybphData(String response) {
        try {
            // 解析JSON响应
            JsonNode rootNode = objectMapper.readTree(response);
            // 获取结果数据节点
            JsonNode resultNode = rootNode.path("result");
            // 获取数据列表节点
            JsonNode dataNode = resultNode.path("data");

            // 检查数据是否为空
            if (dataNode == null || !dataNode.isArray()) {
                log.warn("东方财富营业部排行响应中未找到数据");
                return new DataFrameImpl();
            }

            // 构建数据行列表
            List<Map<String, Object>> rows = new ArrayList<>();

            // 遍历数据节点
            for (JsonNode item : dataNode) {
                Map<String, Object> row = new HashMap<>();

                // 映射字段数据
                row.put("序号", getIntValue(item, "SERIAL_NUM")); // 序号
                row.put("营业部名称", getStringValue(item, "YYB_NAME")); // 营业部名称
                row.put("上榜次数", getIntValue(item, "TOTAL_COUNT")); // 上榜次数
                row.put("买入个股数", getIntValue(item, "BUY_COUNT")); // 买入个股数
                row.put("卖出个股数", getIntValue(item, "SELL_COUNT")); // 卖出个股数
                row.put("买入总金额", getDoubleValue(item, "BUY_AMT")); // 买入总金额
                row.put("卖出总金额", getDoubleValue(item, "SELL_AMT")); // 卖出总金额
                row.put("总买卖净额", getDoubleValue(item, "TOTAL_NETAMT")); // 总买卖净额
                row.put("买入股票", getStringValue(item, "BUY_STOCK")); // 买入股票

                rows.add(row);
            }

            return DataFrameImpl.fromList(rows);

        } catch (IOException e) {
            log.error("解析东方财富营业部排行数据失败", e);
            return new DataFrameImpl();
        }
    }

    /**
     * 将时间周期转换为日期范围
     * @param period 时间周期，如："近一月"、"近三月"、"近六月"、"近一年"
     * @return 日期范围数组，[开始日期, 结束日期]，格式：yyyy-MM-dd
     */
    private String[] convertPeriodToDateRange(String period) {
        // 获取当前日期
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate;

        // 根据周期计算开始日期
        switch (period) {
            case "近三月":
                startDate = endDate.minusMonths(3); // 近三月
                break;
            case "近六月":
                startDate = endDate.minusMonths(6); // 近六月
                break;
            case "近一年":
                startDate = endDate.minusYears(1); // 近一年
                break;
            case "近一月":
            default:
                startDate = endDate.minusMonths(1); // 默认近一月
                break;
        }

        // 格式化日期
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new String[]{startDate.format(formatter), endDate.format(formatter)};
    }

    /**
     * 格式化日期字符串
     * 支持多种输入格式：yyyyMMdd、yyyy-MM-dd、yyyy-MM-dd HH:mm:ss
     * 统一输出格式：yyyy-MM-dd
     * @param date 日期字符串
     * @return 格式化后的日期字符串，格式：yyyy-MM-dd
     */
    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return date;
        }
        
        date = date.trim();
        
        // 如果已经是 yyyy-MM-dd 格式（10位），直接返回
        if (date.length() == 10 && date.charAt(4) == '-' && date.charAt(7) == '-') {
            return date;
        }
        
        // 如果是 yyyy-MM-dd HH:mm:ss 格式，提取日期部分
        if (date.length() > 10 && date.charAt(4) == '-' && date.charAt(7) == '-') {
            return date.substring(0, 10);
        }
        
        // 如果是 yyyyMMdd 格式（8位），转换为 yyyy-MM-dd
        if (date.length() == 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        
        // 其他格式，直接返回原值
        return date;
    }

    /**
     * 获取字符串类型的字段值
     * @param node JSON节点
     * @param field 字段名
     * @return 字符串值，如果字段不存在则返回空字符串
     */
    private String getStringValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        return valueNode != null && !valueNode.isNull() ? valueNode.asText() : "";
    }

    /**
     * 获取整数类型的字段值
     * @param node JSON节点
     * @param field 字段名
     * @return 整数值，如果字段不存在或解析失败则返回null
     */
    private Integer getIntValue(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) return null;
        try {
            return valueNode.asInt();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取浮点数类型的字段值
     * @param node JSON节点
     * @param field 字段名
     * @return 浮点数值，如果字段不存在或解析失败则返回null
     */
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
