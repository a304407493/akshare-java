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
 * 股票数据控制器
 * 提供股票相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    private StockClient stockClient;

    /**
     * 获取A股实时行情数据（东方财富）
     */
    @GetMapping("/realtime/em")
    public Result<List<Map<String, Object>>> getRealtimeEm() {
        try {
            log.info("正在获取A股实时行情数据（东方财富）");
            DataFrame df = stockClient.stockZhASpotEm();
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取A股实时行情数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取A股实时行情数据（新浪）
     */
    @GetMapping("/realtime/sina")
    public Result<List<Map<String, Object>>> getRealtimeSina() {
        try {
            log.info("正在获取A股实时行情数据（新浪）");
            DataFrame df = stockClient.stockZhASpotSina();
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取A股实时行情数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取股票历史K线数据
     *
     * @param symbol    股票代码
     * @param period    周期：daily/weekly/monthly
     * @param startDate 开始日期，格式yyyyMMdd
     * @param endDate   结束日期，格式yyyyMMdd
     * @param adjust    复权类型：qfq/hfq/none
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "qfq") String adjust) {
        try {
            log.info("正在获取{}的历史K线数据，周期={}，复权={}", symbol, period, adjust);
            DataFrame df = stockClient.stockZhAHist(symbol, period, startDate, endDate, adjust);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取历史K线数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取股票分钟K线数据
     *
     * @param symbol 股票代码
     * @param period 分钟周期：1/5/15/30/60
     * @param adjust 复权类型：qfq/hfq/none
     */
    @GetMapping("/history/minute")
    public Result<List<Map<String, Object>>> getMinuteHistory(
            @RequestParam String symbol,
            @RequestParam(defaultValue = "1") int period,
            @RequestParam(defaultValue = "qfq") String adjust) {
        try {
            log.info("正在获取{}的分钟K线数据，周期={}分钟", symbol, period);
            DataFrame df = stockClient.stockZhAHistMinEm(symbol, period, adjust);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取分钟K线数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取A股股票代码名称列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getStockList() {
        try {
            log.info("正在获取A股股票代码名称列表");
            DataFrame df = stockClient.stockInfoACodeName();
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取股票列表失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取个股基础资料
     *
     * @param symbol 股票代码
     */
    @GetMapping("/info/{symbol}")
    public Result<List<Map<String, Object>>> getStockInfo(@PathVariable String symbol) {
        try {
            log.info("正在获取{}的个股基础资料", symbol);
            DataFrame df = stockClient.stockIndividualInfoEm(symbol);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取个股基础资料失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取集合竞价数据
     *
     * @param symbol 股票代码
     */
    @GetMapping("/auction")
    public Result<List<Map<String, Object>>> getAuctionData(@RequestParam String symbol) {
        try {
            log.info("正在获取{}的集合竞价数据", symbol);
            DataFrame df = stockClient.stockAuctionData(symbol);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取集合竞价数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取买卖五档数据
     *
     * @param symbol 股票代码
     */
    @GetMapping("/bid-ask")
    public Result<List<Map<String, Object>>> getBidAskData(@RequestParam String symbol) {
        try {
            log.info("正在获取{}的买卖五档数据", symbol);
            DataFrame df = stockClient.stockBidAskData(symbol);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取买卖五档数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }
}
