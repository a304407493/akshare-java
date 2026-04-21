# 股票数据 API 文档

本文档详细介绍 AKShare Java 股票模块提供的所有数据接口。

## StockClient

`StockClient` 是股票模块的主入口类，提供了所有股票相关数据的获取方法。

### 创建客户端

```java
StockClient client = new StockClient();

// 设置超时时间（毫秒）
client.setTimeout(60000);

// 设置代理
client.setProxy("proxy.example.com", 8080);
```

---

## 实时行情

### stockZhASpotEm - A股实时行情（东方财富）

获取所有A股的实时行情数据。

**方法签名**：
```java
public DataFrame stockZhASpotEm()
```

**返回值**：DataFrame，包含以下列：
- `序号` - 序号
- `代码` - 股票代码
- `名称` - 股票名称
- `最新价` - 最新价格
- `涨跌幅` - 涨跌幅(%)
- `涨跌额` - 涨跌额
- `成交量` - 成交量(手)
- `成交额` - 成交额(元)
- `振幅` - 振幅(%)
- `最高` - 最高价
- `最低` - 最低价
- `今开` - 今开价
- `昨收` - 昨收价
- `量比` - 量比
- `换手率` - 换手率(%)
- `市盈率-动态` - 动态市盈率
- `市净率` - 市净率

**示例**：
```java
StockClient client = new StockClient();
DataFrame spotData = client.stockZhASpotEm();
spotData.head(10).print();
```

### stockZhASpotSina - A股实时行情（新浪财经）

获取所有A股的实时行情数据（新浪财经数据源）。

**方法签名**：
```java
public DataFrame stockZhASpotSina()
```

**返回值**：DataFrame，列与东方财富版本类似。

---

## 历史数据

### stockZhAHist - 历史K线数据

获取指定股票的历史K线数据。

**方法签名**：
```java
public DataFrame stockZhAHist(String symbol, String period, 
                               String startDate, String endDate, String adjust)
```

**参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | String | 是 | 股票代码，如 "000001" |
| period | String | 是 | 周期，可选值：daily(日线)、weekly(周线)、monthly(月线) |
| startDate | String | 是 | 开始日期，格式：yyyyMMdd |
| endDate | String | 是 | 结束日期，格式：yyyyMMdd |
| adjust | String | 是 | 复权类型，可选值：qfq(前复权)、hfq(后复权)、none(不复权) |

**返回值**：DataFrame，包含以下列：
- `日期` - 交易日期
- `开盘` - 开盘价
- `收盘` - 收盘价
- `最高` - 最高价
- `最低` - 最低价
- `成交量` - 成交量(手)
- `成交额` - 成交额(元)
- `振幅` - 振幅(%)
- `涨跌幅` - 涨跌幅(%)
- `涨跌额` - 涨跌额
- `换手率` - 换手率(%)

**示例**：
```java
// 获取平安银行2024年1月到3月的日线数据（前复权）
DataFrame histData = client.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
histData.print();
```

### stockZhAHistMinEm - 分钟级K线数据

获取指定股票的分钟级K线数据。

**方法签名**：
```java
public DataFrame stockZhAHistMinEm(String symbol, int period, String adjust)
```

**参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | String | 是 | 股票代码 |
| period | int | 是 | 分钟周期，可选值：1、5、15、30、60 |
| adjust | String | 是 | 复权类型：qfq、hfq、none |

**返回值**：DataFrame，列与日线数据类似。

**示例**：
```java
// 获取平安银行的5分钟K线数据
DataFrame minData = client.stockZhAHistMinEm("000001", 5, "qfq");
minData.head(20).print();
```

---

## 股票信息

### stockInfoACodeName - A股代码名称列表

获取所有A股的代码和名称列表。

**方法签名**：
```java
public DataFrame stockInfoACodeName()
```

**返回值**：DataFrame，包含：
- `代码` - 股票代码
- `名称` - 股票名称

**示例**：
```java
DataFrame stockList = client.stockInfoACodeName();
System.out.println("A股总数：" + stockList.rowCount());
stockList.head(10).print();
```

### stockIndividualInfoEm - 个股基础信息

获取指定个股的基础信息。

**方法签名**：
```java
public DataFrame stockIndividualInfoEm(String symbol)
```

**参数**：
- symbol：股票代码

**返回值**：DataFrame，包含个股的基础资料信息。

---

## 市场概况

### stockSseSummary - 上交所市场概况

获取上海证券交易所的市场概况数据。

**方法签名**：
```java
public DataFrame stockSseSummary()
```

**返回值**：DataFrame，包含市场概况信息。

### stockSzseSummary - 深交所市场概况

获取深圳证券交易所的市场概况数据。

**方法签名**：
```java
public DataFrame stockSzseSummary()
```

**返回值**：DataFrame，包含市场概况信息。

---

## 股东数据

### stockGdfxTop10Em - 前十大股东

获取指定股票的前十大股东数据。

**方法签名**：
```java
public DataFrame stockGdfxTop10Em(String symbol)
```

**参数**：
- symbol：股票代码

**返回值**：DataFrame，包含前十大股东信息。

### stockGdfxFreeTop10Em - 前十大流通股东

获取指定股票的前十大流通股东数据。

**方法签名**：
```java
public DataFrame stockGdfxFreeTop10Em(String symbol)
```

### stockGdfxHoldingDetailEm - 股东持股明细

获取股东持股明细数据。

**方法签名**：
```java
public DataFrame stockGdfxHoldingDetailEm(String symbol)
```

### stockHolderNumEm - 股东户数

获取指定股票的股东户数数据。

**方法签名**：
```java
public DataFrame stockHolderNumEm(String symbol)
```

**返回值**：DataFrame，包含股东户数变化趋势。

---

## 财务数据

### stockFinancialReportEm - 财务报表

获取指定股票的财务报表数据。

**方法签名**：
```java
public DataFrame stockFinancialReportEm(String symbol)
```

### stockProfitSheetEm - 利润表

获取指定股票的利润表数据。

**方法签名**：
```java
public DataFrame stockProfitSheetEm(String symbol)
```

### stockBalanceSheetEm - 资产负债表

获取指定股票的资产负债表数据。

**方法签名**：
```java
public DataFrame stockBalanceSheetEm(String symbol)
```

### stockCashFlowSheetEm - 现金流量表

获取指定股票的现金流量表数据。

**方法签名**：
```java
public DataFrame stockCashFlowSheetEm(String symbol)
```

### stockFinancialAnalysisEm - 财务指标

获取指定股票的财务指标分析数据。

**方法签名**：
```java
public DataFrame stockFinancialAnalysisEm(String symbol)
```

---

## 机构数据

### stockLhbDetailEm - 龙虎榜详情

获取龙虎榜详情数据。

**方法签名**：
```java
public DataFrame stockLhbDetailEm(String startDate, String endDate)
```

**参数**：
- startDate：开始日期，格式 yyyyMMdd
- endDate：结束日期，格式 yyyyMMdd

### stockLhbStockDetailEm - 个股龙虎榜详情

获取指定个股在某日期的龙虎榜详情。

**方法签名**：
```java
public DataFrame stockLhbStockDetailEm(String symbol, String date, String flag)
```

### stockLhbYybphEm - 营业部排行

获取营业部排行数据。

**方法签名**：
```java
public DataFrame stockLhbYybphEm()
```

---

## 其他数据

### stockInfoShDelist - 上交所退市股票

获取上海证券交易所的退市股票列表。

**方法签名**：
```java
public DataFrame stockInfoShDelist()
```

### stockInfoSzDelist - 深交所退市股票

获取深圳证券交易所的退市股票列表。

**方法签名**：
```java
public DataFrame stockInfoSzDelist()
```

### stockAuctionEm - 竞价数据

获取股票的竞价数据。

**方法签名**：
```java
public DataFrame stockAuctionEm(String symbol)
```

---

## 使用示例

### 综合示例

```java
import com.akshare.stock.client.StockClient;
import com.akshare.core.dataframe.DataFrame;

public class StockApiExample {
    public static void main(String[] args) {
        StockClient client = new StockClient();
        
        // 1. 获取实时行情
        System.out.println("=== 实时行情 ===");
        DataFrame spotData = client.stockZhASpotEm();
        spotData.head(5).print();
        
        // 2. 获取历史数据
        System.out.println("\n=== 历史数据 ===");
        DataFrame histData = client.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
        histData.head(5).print();
        
        // 3. 获取股东数据
        System.out.println("\n=== 股东数据 ===");
        DataFrame holderData = client.stockGdfxTop10Em("000001");
        holderData.print();
        
        // 4. 获取财务数据
        System.out.println("\n=== 财务数据 ===");
        DataFrame financialData = client.stockFinancialReportEm("000001");
        financialData.head(5).print();
    }
}
```

### 数据筛选示例

```java
// 获取涨幅超过5%的股票
DataFrame spotData = client.stockZhASpotEm();
DataFrame risingStocks = spotData.filter(row -> {
    Double change = row.getDouble("涨跌幅");
    return change != null && change > 5.0;
});
risingStocks.print();

// 按涨跌幅排序
DataFrame sorted = spotData.sort("涨跌幅", false);
sorted.head(10).print();
```

---

## 注意事项

1. **请求频率**：建议每秒不超过1次请求
2. **数据缓存**：频繁访问的数据会被缓存，可使用 `clearCache()` 清除
3. **错误处理**：所有方法可能抛出 `AkShareException` 及其子类异常
4. **数据更新**：实时行情数据通常延迟15秒左右
