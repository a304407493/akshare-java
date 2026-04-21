package com.akshare.stock.client;

import com.akshare.core.cache.CacheManager;
import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.http.AkShareHttpClient;
import com.akshare.core.http.HttpClient;
import com.akshare.core.parser.HtmlParser;
import com.akshare.core.parser.JsonParser;
import com.akshare.core.util.StockCodeUtils;
import com.akshare.stock.service.StockFinancialService;
import com.akshare.stock.service.StockHistoryService;
import com.akshare.stock.service.StockHolderService;
import com.akshare.stock.service.StockInfoService;
import com.akshare.stock.service.StockAuctionService;
import com.akshare.stock.service.StockInstitutionService;
import com.akshare.stock.service.StockMarketService;
import com.akshare.stock.service.StockRealtimeService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Stock Client - Main entry point for stock data API
 * Provides unified access to all stock-related data
 */
@Slf4j
public class StockClient {

    private final HttpClient httpClient;
    private final JsonParser jsonParser;
    private final HtmlParser htmlParser;
    private final CacheManager cacheManager;

    private final StockRealtimeService realtimeService;
    private final StockHistoryService historyService;
    private final StockInfoService infoService;
    private final StockMarketService marketService;
    private final StockFinancialService financialService;
    private final StockHolderService holderService;
    private final StockInstitutionService institutionService;
    private final StockAuctionService auctionService;

    public StockClient() {
        this.httpClient = new AkShareHttpClient();
        this.jsonParser = new JsonParser();
        this.htmlParser = new HtmlParser();
        this.cacheManager = new CacheManager(true, Duration.ofMinutes(5), 1000);

        this.realtimeService = new StockRealtimeService(httpClient, jsonParser, htmlParser, cacheManager);
        this.historyService = new StockHistoryService(httpClient, jsonParser, cacheManager);
        this.infoService = new StockInfoService(httpClient, cacheManager);
        this.marketService = new StockMarketService(httpClient, cacheManager);
        this.financialService = new StockFinancialService(httpClient, cacheManager);
        this.holderService = new StockHolderService(httpClient, cacheManager);
        this.institutionService = new StockInstitutionService(httpClient, jsonParser, cacheManager);
        this.auctionService = new StockAuctionService(httpClient, jsonParser, cacheManager);

        log.info("StockClient initialized");
    }

    public StockClient(HttpClient httpClient, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.jsonParser = new JsonParser();
        this.htmlParser = new HtmlParser();
        this.cacheManager = cacheManager;

        this.realtimeService = new StockRealtimeService(httpClient, jsonParser, htmlParser, cacheManager);
        this.historyService = new StockHistoryService(httpClient, jsonParser, cacheManager);
        this.infoService = new StockInfoService(httpClient, cacheManager);
        this.marketService = new StockMarketService(httpClient, cacheManager);
        this.financialService = new StockFinancialService(httpClient, cacheManager);
        this.holderService = new StockHolderService(httpClient, cacheManager);
        this.institutionService = new StockInstitutionService(httpClient, jsonParser, cacheManager);
        this.auctionService = new StockAuctionService(httpClient, jsonParser, cacheManager);

        log.info("StockClient initialized with custom configuration");
    }

    // ==================== Real-time Data ====================

    /**
     * Get A-share real-time spot data from East Money
     *
     * @return DataFrame with real-time stock data
     */
    public DataFrame stockZhASpotEm() {
        return realtimeService.getStockZhASpotEm();
    }

    /**
     * Get A-share real-time spot data from Sina
     *
     * @return DataFrame with real-time stock data
     */
    public DataFrame stockZhASpotSina() {
        return realtimeService.getStockZhASpotSina();
    }

    // ==================== Historical Data ====================

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
    public DataFrame stockZhAHist(String symbol, String period, String startDate, String endDate, String adjust) {
        StockCodeUtils.validateStockCode(symbol);
        return historyService.getStockZhAHist(symbol, period, startDate, endDate, adjust);
    }

    /**
     * Get A-share historical K-line data (simplified)
     *
     * @param symbol stock code, e.g., "000001"
     * @param period period type: daily/weekly/monthly
     * @return DataFrame with historical data
     */
    public DataFrame stockZhAHist(String symbol, String period) {
        return stockZhAHist(symbol, period, null, null, "qfq");
    }

    /**
     * Get A-share historical K-line data (simplified)
     *
     * @param symbol stock code, e.g., "000001"
     * @return DataFrame with historical data (daily)
     */
    public DataFrame stockZhAHist(String symbol) {
        return stockZhAHist(symbol, "daily", null, null, "qfq");
    }

    /**
     * Get A-share minute K-line data from East Money
     *
     * @param symbol stock code, e.g., "000001"
     * @param period minute period: 1/5/15/30/60
     * @param adjust adjust type: qfq/hfq/none
     * @return DataFrame with minute K-line data
     */
    public DataFrame stockZhAHistMinEm(String symbol, int period, String adjust) {
        StockCodeUtils.validateStockCode(symbol);
        return historyService.getStockZhAHistMinEm(symbol, period, adjust);
    }

    /**
     * Get A-share minute K-line data (default 1-minute)
     *
     * @param symbol stock code, e.g., "000001"
     * @return DataFrame with 1-minute K-line data
     */
    public DataFrame stockZhAHistMinEm(String symbol) {
        return stockZhAHistMinEm(symbol, 1, "qfq");
    }

    // ==================== Stock Information ====================

    /**
     * Get A-share stock code and name list
     *
     * @return DataFrame with stock code and name
     */
    public DataFrame stockInfoACodeName() {
        return infoService.getStockInfoACodeName();
    }

    /**
     * Get Shanghai Stock Exchange market summary
     *
     * @return DataFrame with SSE market summary
     */
    public DataFrame stockSseSummary() {
        return infoService.getStockSseSummary();
    }

    /**
     * Get Shenzhen Stock Exchange market summary
     *
     * @return DataFrame with SZSE market summary
     */
    public DataFrame stockSzseSummary() {
        return infoService.getStockSzseSummary();
    }

    /**
     * Get individual stock basic information from East Money
     *
     * @param symbol stock code, e.g., "000001"
     * @return DataFrame with individual stock basic information
     */
    public DataFrame stockIndividualInfoEm(String symbol) {
        StockCodeUtils.validateStockCode(symbol);
        return infoService.getStockIndividualInfoEm(symbol);
    }

    // ==================== Stock List ====================

    /**
     * 获取沪市股票名录（包含代码、名称、市场类型等）
     *
     * @return DataFrame 包含沪市股票名录
     */
    public DataFrame stockInfoShNameCode() {
        return marketService.stockInfoShNameCode();
    }

    /**
     * 获取深市股票名录（包含代码、名称、市场类型等）
     *
     * @return DataFrame 包含深市股票名录
     */
    public DataFrame stockInfoSzNameCode() {
        return marketService.stockInfoSzNameCode();
    }

    /**
     * 获取北交所股票名录（包含代码、名称等）
     *
     * @return DataFrame 包含北交所股票名录
     */
    public DataFrame stockInfoBjNameCode() {
        return marketService.stockInfoBjNameCode();
    }

    // ==================== Delist Stocks ====================

    /**
     * 获取沪市退市股票列表
     *
     * @return DataFrame 包含沪市退市股票信息
     */
    public DataFrame stockInfoShDelist() {
        return marketService.stockInfoShDelist();
    }

    /**
     * 获取深市退市股票列表
     *
     * @return DataFrame 包含深市退市股票信息
     */
    public DataFrame stockInfoSzDelist() {
        return marketService.stockInfoSzDelist();
    }

    // ==================== Stock Change Name ====================

    /**
     * 获取股票更名记录
     *
     * @return DataFrame 包含股票更名记录
     */
    public DataFrame stockInfoChangeName() {
        return marketService.stockInfoChangeName();
    }

    // ==================== Financial Data ====================

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
        StockCodeUtils.validateStockCode(symbol);
        return financialService.stockFinancialReportEm(symbol, reportType, reportName);
    }

    /**
     * 获取财务分析指标数据
     * 包括：盈利能力、偿债能力、成长能力、营运能力等指标
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含财务分析指标数据
     */
    public DataFrame stockFinancialAnalysisEm(String symbol, String reportType) {
        StockCodeUtils.validateStockCode(symbol);
        return financialService.stockFinancialAnalysisEm(symbol, reportType);
    }

    /**
     * 获取利润表数据
     * 从东方财富网获取指定股票的利润表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度
     * @return DataFrame 包含利润表数据
     */
    public DataFrame stockProfitSheetEm(String symbol, String reportType) {
        StockCodeUtils.validateStockCode(symbol);
        return financialService.stockProfitSheetEm(symbol, reportType);
    }

    /**
     * 获取利润表数据（默认按报告期）
     *
     * @param symbol 股票代码，例如："600519"
     * @return DataFrame 包含利润表数据
     */
    public DataFrame stockProfitSheetEm(String symbol) {
        return stockProfitSheetEm(symbol, StockFinancialService.REPORT_TYPE_REPORT);
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
        StockCodeUtils.validateStockCode(symbol);
        return financialService.stockBalanceSheetEm(symbol, reportType);
    }

    /**
     * 获取资产负债表数据（默认按报告期）
     *
     * @param symbol 股票代码，例如："600519"
     * @return DataFrame 包含资产负债表数据
     */
    public DataFrame stockBalanceSheetEm(String symbol) {
        return stockBalanceSheetEm(symbol, StockFinancialService.REPORT_TYPE_REPORT);
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
        StockCodeUtils.validateStockCode(symbol);
        return financialService.stockCashFlowSheetEm(symbol, reportType);
    }

    /**
     * 获取现金流量表数据（默认按报告期）
     *
     * @param symbol 股票代码，例如："600519"
     * @return DataFrame 包含现金流量表数据
     */
    public DataFrame stockCashFlowSheetEm(String symbol) {
        return stockCashFlowSheetEm(symbol, StockFinancialService.REPORT_TYPE_REPORT);
    }

    // ==================== Configuration ====================

    /**
     * Set request timeout
     *
     * @param timeoutMs timeout in milliseconds
     */
    public void setTimeout(int timeoutMs) {
        httpClient.setTimeout(timeoutMs);
    }

    /**
     * Set proxy configuration
     *
     * @param proxyHost proxy host
     * @param proxyPort proxy port
     */
    public void setProxy(String proxyHost, int proxyPort) {
        httpClient.setProxy(proxyHost, proxyPort);
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        cacheManager.invalidateAll();
    }

    /**
     * Get cache statistics
     *
     * @return cache stats
     */
    public String getCacheStats() {
        return cacheManager.getStats().toString();
    }

    // ==================== Holder Data ====================

    /**
     * 获取十大流通股东数据（东方财富）
     * 对应Python AKShare的stock_gdfx_free_top_10_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含十大流通股东数据
     */
    public DataFrame stockGdfxFreeTop10Em(String symbol, String date) {
        return holderService.getStockGdfxFreeTop10Em(symbol, date);
    }

    /**
     * 获取十大股东数据（东方财富）
     * 对应Python AKShare的stock_gdfx_top_10_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含十大股东数据
     */
    public DataFrame stockGdfxTop10Em(String symbol, String date) {
        return holderService.getStockGdfxTop10Em(symbol, date);
    }

    /**
     * 获取股东持股详情数据（东方财富）
     * 对应Python AKShare的stock_gdfx_holding_detail_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含股东持股详情数据
     */
    public DataFrame stockGdfxHoldingDetailEm(String symbol, String date) {
        return holderService.getStockGdfxHoldingDetailEm(symbol, date);
    }

    /**
     * 获取流通股东持股详情数据（东方财富）
     * 对应Python AKShare的stock_gdfx_free_holding_detail_em接口
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date 报告期日期，格式如"20240930"
     * @return DataFrame 包含流通股东持股详情数据
     */
    public DataFrame stockGdfxFreeHoldingDetailEm(String symbol, String date) {
        return holderService.getStockGdfxFreeHoldingDetailEm(symbol, date);
    }

    /**
     * 获取股东户数数据（东方财富）
     * 对应Python AKShare的stock_gdfx_holder_num_em接口
     * 数据来源：RPT_F10_EH_HOLDERNUM
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @return DataFrame 包含股东户数数据
     *         字段包括：报告期、股东户数、户均持股、较上期变化、筹码集中度等
     */
    public DataFrame stockHolderNumEm(String symbol) {
        return holderService.getStockHolderNumEm(symbol);
    }

    // ==================== Institution Data ====================

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
        return institutionService.stockLhbDetailEm(startDate, endDate);
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
        return institutionService.stockLhbStockDetailEm(symbol, date, flag);
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
        return institutionService.stockLhbJgmmEm(startDate, endDate);
    }

    /**
     * 获取营业部排行数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-营业部排行
     *
     * @param period 时间周期，可选值："近一月"、"近三月"、"近六月"、"近一年"
     * @return DataFrame 包含营业部排行的数据框
     *         字段包括：序号、营业部名称、上榜次数、买入个股数、卖出个股数、
     *         买入总金额、卖出总金额、总买卖净额、买入股票
     */
    public DataFrame stockLhbYybphEm(String period) {
        return institutionService.stockLhbYybphEm(period);
    }

    // ==================== Auction Data ====================

    /**
     * 获取集合竞价数据
     *
     * @param symbol 股票代码，例如："000001"
     * @return DataFrame 包含集合竞价数据
     */
    public DataFrame stockAuctionData(String symbol) {
        StockCodeUtils.validateStockCode(symbol);
        return auctionService.getStockAuctionData(symbol);
    }

    /**
     * 获取买卖五档数据
     *
     * @param symbol 股票代码，例如："000001"
     * @return DataFrame 包含买卖五档数据
     */
    public DataFrame stockBidAskData(String symbol) {
        StockCodeUtils.validateStockCode(symbol);
        return auctionService.getStockBidAskData(symbol);
    }
}
