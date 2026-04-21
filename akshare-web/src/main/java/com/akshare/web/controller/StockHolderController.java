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
 * 股东数据控制器
 * 提供股东相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/stock/holder")
@CrossOrigin(origins = "*")
public class StockHolderController {

    @Autowired
    private StockClient stockClient;

    /**
     * 获取十大流通股东数据
     * 从东方财富网获取指定股票的十大流通股东信息
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date   报告期日期，格式如"20240930"
     * @return 十大流通股东数据列表
     */
    @GetMapping("/top10-free")
    public Result<List<Map<String, Object>>> getTop10FreeHolders(
            @RequestParam String symbol,
            @RequestParam String date) {
        try {
            log.info("正在获取{}的十大流通股东数据，报告期={}", symbol, date);
            DataFrame df = stockClient.stockGdfxFreeTop10Em(symbol, date);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取十大流通股东数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取十大股东数据
     * 从东方财富网获取指定股票的十大股东信息
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date   报告期日期，格式如"20240930"
     * @return 十大股东数据列表
     */
    @GetMapping("/top10")
    public Result<List<Map<String, Object>>> getTop10Holders(
            @RequestParam String symbol,
            @RequestParam String date) {
        try {
            log.info("正在获取{}的十大股东数据，报告期={}", symbol, date);
            DataFrame df = stockClient.stockGdfxTop10Em(symbol, date);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取十大股东数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取股东持股详情数据
     * 从东方财富网获取指定股票的股东持股详情
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date   报告期日期，格式如"20240930"
     * @return 股东持股详情数据列表
     */
    @GetMapping("/holding-detail")
    public Result<List<Map<String, Object>>> getHoldingDetail(
            @RequestParam String symbol,
            @RequestParam String date) {
        try {
            log.info("正在获取{}的股东持股详情数据，报告期={}", symbol, date);
            DataFrame df = stockClient.stockGdfxHoldingDetailEm(symbol, date);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取股东持股详情数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取流通股东持股详情数据
     * 从东方财富网获取指定股票的流通股东持股详情
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @param date   报告期日期，格式如"20240930"
     * @return 流通股东持股详情数据列表
     */
    @GetMapping("/free-holding-detail")
    public Result<List<Map<String, Object>>> getFreeHoldingDetail(
            @RequestParam String symbol,
            @RequestParam String date) {
        try {
            log.info("正在获取{}的流通股东持股详情数据，报告期={}", symbol, date);
            DataFrame df = stockClient.stockGdfxFreeHoldingDetailEm(symbol, date);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取流通股东持股详情数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取股东户数数据
     * 从东方财富网获取指定股票的股东户数变化趋势数据
     *
     * @param symbol 股票代码，格式如"sh600000"或"sz000001"
     * @return 股东户数数据列表
     */
    @GetMapping("/holder-num")
    public Result<List<Map<String, Object>>> getHolderNum(
            @RequestParam String symbol) {
        try {
            log.info("正在获取{}的股东户数数据", symbol);
            DataFrame df = stockClient.stockHolderNumEm(symbol);
            return Result.success(df.toList());
        } catch (Exception e) {
            log.error("获取股东户数数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }
}
