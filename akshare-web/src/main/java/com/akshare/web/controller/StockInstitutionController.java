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
 * 股票机构数据控制器
 * 提供机构相关的REST API接口，包括龙虎榜、机构买卖、营业部排行等数据
 * 数据源：东方财富网
 */
@Slf4j
@RestController
@RequestMapping("/api/stock/institution")
@CrossOrigin(origins = "*")
public class StockInstitutionController {

    @Autowired
    private StockClient stockClient;

    /**
     * 获取龙虎榜详情数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-龙虎榜详情
     *
     * @param startDate 开始日期，格式：yyyyMMdd，如：20220314
     * @param endDate 结束日期，格式：yyyyMMdd，如：20220315
     * @return Result 包含龙虎榜详情的统一响应格式
     *         数据字段：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    @GetMapping("/lhb-detail")
    public Result<List<Map<String, Object>>> getLhbDetail(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("[getLhbDetail] 开始获取龙虎榜详情数据，startDate={}，endDate={}", startDate, endDate);
        try {
            DataFrame df = stockClient.stockLhbDetailEm(startDate, endDate);
            log.info("[getLhbDetail] 成功获取龙虎榜详情数据，共{}条记录", df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getLhbDetail] 获取龙虎榜详情数据失败，startDate={}，endDate={}", startDate, endDate, e);
            return Result.error("获取龙虎榜详情数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取个股龙虎榜详情数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-个股龙虎榜详情
     *
     * @param symbol 股票代码，如：000001
     * @param date 交易日期，格式：yyyyMMdd，如：20220314
     * @param flag 买卖方向，可选值："买入"、"卖出"，不传则返回全部
     * @return Result 包含个股龙虎榜详情的统一响应格式
     *         数据字段：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    @GetMapping("/lhb-stock-detail")
    public Result<List<Map<String, Object>>> getLhbStockDetail(
            @RequestParam String symbol,
            @RequestParam String date,
            @RequestParam(required = false) String flag) {
        log.info("[getLhbStockDetail] 开始获取个股龙虎榜详情数据，symbol={}，date={}，flag={}", symbol, date, flag);
        try {
            DataFrame df = stockClient.stockLhbStockDetailEm(symbol, date, flag);
            log.info("[getLhbStockDetail] 成功获取个股龙虎榜详情数据，共{}条记录", df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getLhbStockDetail] 获取个股龙虎榜详情数据失败，symbol={}，date={}", symbol, date, e);
            return Result.error("获取个股龙虎榜详情数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取机构买卖统计数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-机构买卖统计
     *
     * @param startDate 开始日期，格式：yyyyMMdd，如：20220314
     * @param endDate 结束日期，格式：yyyyMMdd，如：20220315
     * @return Result 包含机构买卖统计的统一响应格式
     *         数据字段：序号、代码、名称、上榜日、解读、收盘价、涨跌幅、龙虎榜净买额、
     *         龙虎榜买入额、龙虎榜卖出额、龙虎榜成交额、市场总成交额、净买额占总成交比、
     *         成交额占总成交比、换手率、流通市值、上榜原因、上榜后1日、上榜后2日、上榜后5日、上榜后10日
     */
    @GetMapping("/lhb-jgmm")
    public Result<List<Map<String, Object>>> getLhbJgmm(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("[getLhbJgmm] 开始获取机构买卖统计数据，startDate={}，endDate={}", startDate, endDate);
        try {
            DataFrame df = stockClient.stockLhbJgmmEm(startDate, endDate);
            log.info("[getLhbJgmm] 成功获取机构买卖统计数据，共{}条记录", df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getLhbJgmm] 获取机构买卖统计数据失败，startDate={}，endDate={}", startDate, endDate, e);
            return Result.error("获取机构买卖统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取营业部排行数据
     * 数据来源：东方财富网-数据中心-龙虎榜单-营业部排行
     *
     * @param period 时间周期，可选值："近一月"、"近三月"、"近六月"、"近一年"，默认"近一月"
     * @return Result 包含营业部排行的统一响应格式
     *         数据字段：序号、营业部名称、上榜次数、买入个股数、卖出个股数、
     *         买入总金额、卖出总金额、总买卖净额、买入股票
     */
    @GetMapping("/lhb-yybph")
    public Result<List<Map<String, Object>>> getLhbYybph(
            @RequestParam(required = false, defaultValue = "近一月") String period) {
        log.info("[getLhbYybph] 开始获取营业部排行数据，period={}", period);
        try {
            DataFrame df = stockClient.stockLhbYybphEm(period);
            log.info("[getLhbYybph] 成功获取营业部排行数据，共{}条记录", df.rowCount());
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("[getLhbYybph] 获取营业部排行数据失败，period={}", period, e);
            return Result.error("获取营业部排行数据失败: " + e.getMessage());
        }
    }
}
