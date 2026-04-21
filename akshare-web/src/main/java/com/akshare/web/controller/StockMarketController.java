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
 * 股票市场概况控制器
 * 提供市场总貌、股票名录、退市股票、更名记录等相关REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/stock/market")
@CrossOrigin(origins = "*")
public class StockMarketController {

    @Autowired
    private StockClient stockClient;

    /**
     * 获取深交所市场总貌
     * 包含：上市公司数量、总市值、流通市值、平均市盈率等市场概况数据
     *
     * @return Result 包含深交所市场总貌数据
     */
    @GetMapping("/szse-summary")
    public Result<List<Map<String, Object>>> getSzseSummary() {
        log.info("[StockMarketController] 开始获取深交所市场总貌数据");
        try {
            DataFrame df = stockClient.stockSzseSummary();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取深交所市场总貌数据成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取深交所市场总貌数据失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取深交所市场总貌失败: " + e.getMessage());
        }
    }

    /**
     * 获取上交所市场总貌
     * 包含：上市公司数量、总市值、流通市值、平均市盈率等市场概况数据
     *
     * @return Result 包含上交所市场总貌数据
     */
    @GetMapping("/sse-summary")
    public Result<List<Map<String, Object>>> getSseSummary() {
        log.info("[StockMarketController] 开始获取上交所市场总貌数据");
        try {
            DataFrame df = stockClient.stockSseSummary();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取上交所市场总貌数据成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取上交所市场总貌数据失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取上交所市场总貌失败: " + e.getMessage());
        }
    }

    /**
     * 获取沪市股票名录
     * 包含：股票代码、股票名称、市场类型等基础信息
     *
     * @return Result 包含沪市股票名录数据
     */
    @GetMapping("/sh-name-code")
    public Result<List<Map<String, Object>>> getShNameCode() {
        log.info("[StockMarketController] 开始获取沪市股票名录");
        try {
            DataFrame df = stockClient.stockInfoShNameCode();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取沪市股票名录成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取沪市股票名录失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取沪市股票名录失败: " + e.getMessage());
        }
    }

    /**
     * 获取深市股票名录
     * 包含：股票代码、股票名称、市场类型等基础信息
     *
     * @return Result 包含深市股票名录数据
     */
    @GetMapping("/sz-name-code")
    public Result<List<Map<String, Object>>> getSzNameCode() {
        log.info("[StockMarketController] 开始获取深市股票名录");
        try {
            DataFrame df = stockClient.stockInfoSzNameCode();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取深市股票名录成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取深市股票名录失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取深市股票名录失败: " + e.getMessage());
        }
    }

    /**
     * 获取北交所股票名录
     * 包含：股票代码、股票名称等基础信息
     *
     * @return Result 包含北交所股票名录数据
     */
    @GetMapping("/bj-name-code")
    public Result<List<Map<String, Object>>> getBjNameCode() {
        log.info("[StockMarketController] 开始获取北交所股票名录");
        try {
            DataFrame df = stockClient.stockInfoBjNameCode();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取北交所股票名录成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取北交所股票名录失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取北交所股票名录失败: " + e.getMessage());
        }
    }

    /**
     * 获取沪市退市股票列表
     * 包含：已退市股票的代码、名称、退市日期等信息
     *
     * @return Result 包含沪市退市股票数据
     */
    @GetMapping("/sh-delist")
    public Result<List<Map<String, Object>>> getShDelist() {
        log.info("[StockMarketController] 开始获取沪市退市股票列表");
        try {
            DataFrame df = stockClient.stockInfoShDelist();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取沪市退市股票列表成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取沪市退市股票列表失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取沪市退市股票失败: " + e.getMessage());
        }
    }

    /**
     * 获取深市退市股票列表
     * 包含：已退市股票的代码、名称、退市日期等信息
     *
     * @return Result 包含深市退市股票数据
     */
    @GetMapping("/sz-delist")
    public Result<List<Map<String, Object>>> getSzDelist() {
        log.info("[StockMarketController] 开始获取深市退市股票列表");
        try {
            DataFrame df = stockClient.stockInfoSzDelist();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取深市退市股票列表成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取深市退市股票列表失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取深市退市股票失败: " + e.getMessage());
        }
    }

    /**
     * 获取股票更名记录
     * 包含：股票代码、原名称、新名称、更名日期等历史更名信息
     *
     * @return Result 包含股票更名记录数据
     */
    @GetMapping("/change-name")
    public Result<List<Map<String, Object>>> getChangeName() {
        log.info("[StockMarketController] 开始获取股票更名记录");
        try {
            DataFrame df = stockClient.stockInfoChangeName();
            List<Map<String, Object>> data = df.toList();
            log.info("[StockMarketController] 获取股票更名记录成功，共{}条记录", data.size());
            return Result.success(data);
        } catch (Exception e) {
            log.error("[StockMarketController] 获取股票更名记录失败，错误信息：{}", e.getMessage(), e);
            return Result.error("获取股票更名记录失败: " + e.getMessage());
        }
    }
}
