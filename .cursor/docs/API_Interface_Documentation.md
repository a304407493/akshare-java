# AKShare Java 后端接口文档

## 导航菜单与后端接口对照表

| 导航菜单 | 前端路由 | 后端接口 | 调用链路 | 外部接口 |
|---------|---------|---------|---------|---------|
| **实时行情** | `/stock/realtime` | `/api/stock/realtime/em`<br>`/api/stock/realtime/sina` | `StockController.getRealtimeEm()` → `StockClient.stockZhASpotEm()`<br>`StockController.getRealtimeSina()` → `StockClient.stockZhASpotSina()` | 东方财富API<br>新浪财经API |
| **历史K线** | `/stock/history` | `/api/stock/history` | `StockController.getHistory()` → `StockClient.stockZhAHist()` | 东方财富API |
| **股票列表** | `/stock/list` | `/api/stock/list` | `StockController.getStockList()` → `StockClient.stockInfoACodeName()` | 东方财富API |
| **市场概况** | `/stock/market` | `/api/stock/market/sse-summary`<br>`/api/stock/market/szse-summary` | `StockMarketController.getSseSummary()` → `StockClient.stockSseSummary()`<br>`StockMarketController.getSzseSummary()` → `StockClient.stockSzseSummary()` | 上交所API<br>深交所API |
| **增强市场概况** | `/stock/market-enhanced` | 复用市场概况接口 | 同市场概况 | 上交所API<br>深交所API |
| **增强股票列表** | `/stock/list-enhanced` | 复用股票列表接口 | 同股票列表 | 东方财富API |
| **退市股票** | `/stock/delist` | `/api/stock/market/sh-delist`<br>`/api/stock/market/sz-delist` | `StockMarketController.getShDelist()` → `StockClient.stockInfoShDelist()`<br>`StockMarketController.getSzDelist()` → `StockClient.stockInfoSzDelist()` | 上交所API<br>深交所API |
| **股东数据** | `/stock/holder` | `/api/stock/holder/top10`<br>`/api/stock/holder/top10-free`<br>`/api/stock/holder/holding-detail`<br>`/api/stock/holder/free-holding-detail` | `StockHolderController.getTop10Holders()` → `StockClient.stockGdfxTop10Em()`<br>`StockHolderController.getTop10FreeHolders()` → `StockClient.stockGdfxFreeTop10Em()`<br>`StockHolderController.getHoldingDetail()` → `StockClient.stockGdfxHoldingDetailEm()`<br>`StockHolderController.getFreeHoldingDetail()` → `StockClient.stockGdfxFreeHoldingDetailEm()` | 东方财富API |
| **股东户数趋势** | `/stock/holder-trend` | `/api/stock/holder/holder-num` | `StockHolderController.getHolderNum()` → `StockClient.stockHolderNumEm()` | 东方财富API |
| **财务数据** | `/stock/financial` | `/api/stock/financial/report`<br>`/api/stock/financial/profit-sheet`<br>`/api/stock/financial/balance-sheet`<br>`/api/stock/financial/cash-flow-sheet` | `StockFinancialController.getFinancialReport()` → `StockClient.stockFinancialReportEm()`<br>`StockFinancialController.getProfitSheet()` → `StockClient.stockProfitSheetEm()`<br>`StockFinancialController.getBalanceSheet()` → `StockClient.stockBalanceSheetEm()`<br>`StockFinancialController.getCashFlowSheet()` → `StockClient.stockCashFlowSheetEm()` | 东方财富API |
| **财务指标** | `/stock/financial-indicators` | `/api/stock/financial/analysis` | `StockFinancialController.getFinancialAnalysis()` → `StockClient.stockFinancialAnalysisEm()` | 东方财富API |
| **机构数据** | `/stock/institution` | `/api/stock/institution/lhb-detail` | `StockInstitutionController.getLhbDetail()` → `StockClient.stockLhbDetailEm()` | 东方财富API |
| **机构详情** | `/stock/institution/detail` | `/api/stock/institution/lhb-stock-detail` | `StockInstitutionController.getLhbStockDetail()` → `StockClient.stockLhbStockDetailEm()` | 东方财富API |
| **机构排行** | `/stock/institution/rank` | `/api/stock/institution/lhb-yybph` | `StockInstitutionController.getLhbYybph()` → `StockClient.stockLhbYybphEm()` | 东方财富API |

## 调用链路说明

### 1. 前端调用
- 前端通过 `api.js` 中的方法发起 HTTP 请求
- 基础 URL: `http://localhost:8765/api`

### 2. 后端控制器
- 接收前端请求
- 调用 `StockClient` 方法获取数据
- 返回统一格式的 `Result` 对象

### 3. StockClient
- 封装具体的服务调用
- 协调各服务模块

### 4. 服务实现
- 调用外部 API 获取原始数据
- 数据转换和处理
- 返回 DataFrame 对象

## 外部接口来源

| 数据源 | 用途 |
|-------|------|
| **东方财富网** | 实时行情、历史K线、股票列表、股东数据、财务数据、机构数据 |
| **新浪财经** | 实时行情（备用数据源） |
| **上海证券交易所** | 市场概况、退市股票 |
| **深圳证券交易所** | 市场概况、退市股票 |

## 修复记录

### 2026-03-23 个股龙虎榜详情接口修复

**问题描述**：
- 接口 `/api/stock/institution/lhb-stock-detail` 返回空数据
- 请求示例：`http://localhost:9876/stock/institution/detail?symbol=000815&name=美利云&date=2026-03-20`

**问题原因**：
- 原实现直接调用东方财富 API，使用 `SECURITY_CODE` 过滤条件无法正确返回数据
- 东方财富 API 对个股龙虎榜详情的过滤条件不支持

**修复方案**：
1. 修改 `StockInstitutionService.stockLhbStockDetailEm()` 方法
2. 改为调用 `stockLhbDetailEm()` 获取当天所有龙虎榜数据
3. 新增 `filterDataBySymbol()` 方法，在本地过滤出指定股票代码的数据

**关键代码变更**：
```java
public DataFrame stockLhbStockDetailEm(String symbol, String date, String flag) {
    // ... 参数校验和缓存逻辑 ...
    
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
```

**验证结果**：
- `symbol=000815&date=2026-03-20` 返回美利云龙虎榜数据（换手率20%）
- `symbol=002335&date=2025-03-20` 返回科华数据龙虎榜数据（跌幅偏离值7%）

## 文件位置

| 文件类型 | 路径 |
|---------|------|
| 前端路由配置 | `akshare-web/src/main/frontend/src/router/index.js` |
| 前端 API 配置 | `akshare-web/src/main/frontend/src/utils/api.js` |
| 股票数据控制器 | `akshare-web/src/main/java/com/akshare/web/controller/StockController.java` |
| 市场数据控制器 | `akshare-web/src/main/java/com/akshare/web/controller/StockMarketController.java` |
| 股东数据控制器 | `akshare-web/src/main/java/com/akshare/web/controller/StockHolderController.java` |
| 财务数据控制器 | `akshare-web/src/main/java/com/akshare/web/controller/StockFinancialController.java` |
| 机构数据控制器 | `akshare-web/src/main/java/com/akshare/web/controller/StockInstitutionController.java` |
| 机构数据服务 | `akshare-stock/src/main/java/com/akshare/stock/service/StockInstitutionService.java` |

---

**文档生成时间**: 2026-03-23
**版本**: 1.0.0
