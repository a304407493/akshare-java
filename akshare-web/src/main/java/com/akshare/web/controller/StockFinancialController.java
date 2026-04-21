package com.akshare.web.controller;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.stock.client.StockClient;
import com.akshare.web.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 股票财务数据控制器
 * 提供财务数据相关的REST API接口
 * 包括：财务报表、财务分析指标、利润表、资产负债表、现金流量表等
 *
 * @author AKShare Java Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/stock/financial")
@CrossOrigin(origins = "*")
public class StockFinancialController {

    @Autowired
    private StockClient stockClient;

    // ==================== 财务报表接口 ====================

    /**
     * 获取财务报表数据
     * 支持获取资产负债表、利润表、现金流量表等财务报表数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度，默认为1
     * @param reportName 报表名称："balancesheet"-资产负债表，"incomesheet"-利润表，"cashflow"-现金流量表，默认为"balancesheet"
     * @return Result 包含财务报表数据的统一响应对象
     */
    @GetMapping("/report")
    public Result<List<Map<String, Object>>> getFinancialReport(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") String reportType,
            @RequestParam(defaultValue = "balancesheet") String reportName) {
        log.info("[getFinancialReport] 开始获取财务报表数据，股票代码：{}，报表类型：{}，报表名称：{}", symbol, reportType, reportName);
        try {
            DataFrame df = stockClient.stockFinancialReportEm(symbol, reportType, reportName);
            log.info("[getFinancialReport] 成功获取{}的财务报表数据，共{}条记录", symbol, df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getFinancialReport] 获取财务报表数据失败，股票代码：{}，错误信息：{}", symbol, e.getMessage(), e);
            return Result.error("获取财务报表数据失败: " + e.getMessage());
        }
    }

    // ==================== 财务分析指标接口 ====================

    /**
     * 获取财务分析指标数据
     * 包括：盈利能力指标（ROE、ROA、毛利率等）、偿债能力指标（资产负债率、流动比率等）、
     * 成长能力指标（营收增长率、净利润增长率等）、营运能力指标（总资产周转率、存货周转率等）
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度，默认为1
     * @return Result 包含财务分析指标数据的统一响应对象
     */
    @GetMapping("/analysis")
    public Result<List<Map<String, Object>>> getFinancialAnalysis(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") String reportType) {
        log.info("[getFinancialAnalysis] 开始获取财务分析指标数据，股票代码：{}，报表类型：{}", symbol, reportType);
        try {
            DataFrame df = stockClient.stockFinancialAnalysisEm(symbol, reportType);
            log.info("[getFinancialAnalysis] 成功获取{}的财务分析指标数据，共{}条记录", symbol, df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getFinancialAnalysis] 获取财务分析指标数据失败，股票代码：{}，错误信息：{}", symbol, e.getMessage(), e);
            return Result.error("获取财务分析指标数据失败: " + e.getMessage());
        }
    }

    // ==================== 利润表接口 ====================

    /**
     * 获取利润表数据
     * 包含：营业总收入、营业收入、营业成本、销售费用、管理费用、财务费用、
     * 营业利润、利润总额、净利润、归母净利润、每股收益等数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度，默认为1
     * @return Result 包含利润表数据的统一响应对象
     */
    @GetMapping("/profit-sheet")
    public Result<List<Map<String, Object>>> getProfitSheet(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") String reportType) {
        log.info("[getProfitSheet] 开始获取利润表数据，股票代码：{}，报表类型：{}", symbol, reportType);
        try {
            DataFrame df = stockClient.stockProfitSheetEm(symbol, reportType);
            log.info("[getProfitSheet] 成功获取{}的利润表数据，共{}条记录", symbol, df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getProfitSheet] 获取利润表数据失败，股票代码：{}，错误信息：{}", symbol, e.getMessage(), e);
            return Result.error("获取利润表数据失败: " + e.getMessage());
        }
    }

    // ==================== 资产负债表接口 ====================

    /**
     * 获取资产负债表数据
     * 包含：资产总计、负债合计、股东权益合计、货币资金、应收账款、存货、
     * 流动资产、非流动资产、短期借款、应付账款、流动负债、非流动负债等数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度，默认为1
     * @return Result 包含资产负债表数据的统一响应对象
     */
    @GetMapping("/balance-sheet")
    public Result<List<Map<String, Object>>> getBalanceSheet(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") String reportType) {
        log.info("[getBalanceSheet] 开始获取资产负债表数据，股票代码：{}，报表类型：{}", symbol, reportType);
        try {
            DataFrame df = stockClient.stockBalanceSheetEm(symbol, reportType);
            log.info("[getBalanceSheet] 成功获取{}的资产负债表数据，共{}条记录", symbol, df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getBalanceSheet] 获取资产负债表数据失败，股票代码：{}，错误信息：{}", symbol, e.getMessage(), e);
            return Result.error("获取资产负债表数据失败: " + e.getMessage());
        }
    }

    // ==================== 现金流量表接口 ====================

    /**
     * 获取现金流量表数据
     * 包含：经营活动现金流净额、投资活动现金流净额、筹资活动现金流净额、
     * 现金及等价物净增加额、销售商品收到的现金、支付给职工的现金、支付的税费等数据
     *
     * @param symbol 股票代码，例如："600519"
     * @param reportType 报表类型：1-按报告期，2-按年度，3-按单季度，默认为1
     * @return Result 包含现金流量表数据的统一响应对象
     */
    @GetMapping("/cash-flow-sheet")
    public Result<List<Map<String, Object>>> getCashFlowSheet(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") String reportType) {
        log.info("[getCashFlowSheet] 开始获取现金流量表数据，股票代码：{}，报表类型：{}", symbol, reportType);
        try {
            DataFrame df = stockClient.stockCashFlowSheetEm(symbol, reportType);
            log.info("[getCashFlowSheet] 成功获取{}的现金流量表数据，共{}条记录", symbol, df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getCashFlowSheet] 获取现金流量表数据失败，股票代码：{}，错误信息：{}", symbol, e.getMessage(), e);
            return Result.error("获取现金流量表数据失败: " + e.getMessage());
        }
    }
}
