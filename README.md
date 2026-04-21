# AKShare Java

<p align="center">
  <img src="https://img.shields.io/badge/Java-8%2B-blue" alt="Java 8+">
  <img src="https://img.shields.io/badge/Maven-3.6%2B-blue" alt="Maven 3.6+">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License: MIT">
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome">
</p>

<p align="center">
  <b>AKShare Java</b> 是 Python 版本 <a href="https://www.akshare.xyz/">AKShare</a> 的 Java 实现，提供访问中国金融市场数据的功能。
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#API文档">API文档</a> •
  <a href="#Web可视化">Web可视化</a> •
  <a href="#贡献指南">贡献指南</a>
</p>

---

## 功能特性

- **股票数据**：实时行情、历史K线、分钟级数据、股票列表、市场概况、股东数据、财务数据、机构数据
- **基金数据**：开放式基金净值、ETF行情、基金名称列表
- **期货数据**：商品期货行情、历史数据
- **宏观数据**：GDP、CPI、PPI、PMI 等宏观经济指标
- **债券数据**：可转债行情和信息
- **Web可视化**：基于 Vue + Spring Boot 的完整可视化系统

## 快速开始

### 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本
- Node.js 12+（Web模块需要）

### Maven依赖

```xml
<dependency>
    <groupId>com.akshare</groupId>
    <artifactId>akshare-stock</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 源码构建

```bash
git clone https://github.com/a304407493/akshare-java.git
cd akshare-java
mvn clean install
```

### 快速示例

```java
import com.akshare.stock.client.StockClient;
import com.akshare.core.dataframe.DataFrame;

public class QuickStart {
    public static void main(String[] args) {
        StockClient client = new StockClient();
        
        // 获取A股实时数据
        DataFrame spotData = client.stockZhASpotEm();
        spotData.head(10).print();
        
        // 获取历史K线数据
        DataFrame histData = client.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
        histData.print();
    }
}
```

更多示例请查看 [examples](examples/src/main/java/QuickStart.java) 目录。

## 项目结构

```
akshare-java/
├── akshare-core/          # 核心模块 - HTTP客户端、数据解析、缓存管理
├── akshare-common/        # 公共模块 - 枚举、常量
├── akshare-stock/         # 股票模块 - 股票相关数据接口
├── akshare-fund/          # 基金模块 - 基金相关数据接口
├── akshare-futures/       # 期货模块 - 期货相关数据接口
├── akshare-macro/         # 宏观模块 - 宏观经济数据接口
├── akshare-bond/          # 债券模块 - 债券相关数据接口
├── akshare-web/           # Web模块 - Spring Boot + Vue 可视化系统
│   ├── src/main/java/     # 后端代码
│   └── src/main/frontend/ # 前端代码
├── examples/              # 示例代码
└── pom.xml                # Maven父POM
```

## API文档

### 股票数据 (StockClient)

| 方法 | 说明 | 参数 |
|------|------|------|
| `stockZhASpotEm()` | A股实时行情（东方财富） | - |
| `stockZhASpotSina()` | A股实时行情（新浪财经） | - |
| `stockZhAHist(symbol, period, startDate, endDate, adjust)` | 历史K线数据 | symbol: 股票代码, period: daily/weekly/monthly, adjust: qfq/hfq/none |
| `stockZhAHistMinEm(symbol, period, adjust)` | 分钟级K线数据 | period: 1/5/15/30/60 |
| `stockInfoACodeName()` | A股股票代码名称列表 | - |
| `stockSseSummary()` | 上交所市场概况 | - |
| `stockSzseSummary()` | 深交所市场概况 | - |

### 基金数据 (FundClient)

| 方法 | 说明 |
|------|------|
| `fundEmOpenFundDaily()` | 开放式基金每日净值 |
| `fundEmFundName()` | 基金名称列表 |
| `fundEtfSpotEm()` | ETF实时行情 |

### 期货数据 (FuturesClient)

| 方法 | 说明 |
|------|------|
| `futuresZhSpot()` | 期货实时行情 |
| `futuresZhDaily(symbol)` | 期货日线数据 |

### 宏观数据 (MacroClient)

| 方法 | 说明 |
|------|------|
| `macroChinaGdp()` | 中国GDP数据 |
| `macroChinaCpi()` | 中国CPI数据 |
| `macroChinaPpi()` | 中国PPI数据 |
| `macroChinaPmi()` | 中国PMI数据 |

### 债券数据 (BondClient)

| 方法 | 说明 |
|------|------|
| `bondZhCov()` | 可转债数据 |

### DataFrame操作

```java
// 选择列
DataFrame selected = df.select("代码", "名称", "最新价");

// 过滤行
DataFrame filtered = df.filter(row -> row.getDouble("涨跌幅") > 5.0);

// 排序
DataFrame sorted = df.sort("涨跌幅", false); // 降序

// 导出数据
String json = df.toJson();
String csv = df.toCsv();
List<Map<String, Object>> list = df.toList();
```

## Web可视化

AKShare Java 提供了完整的 Web 可视化系统，基于 Spring Boot + Vue 实现。

### 启动Web服务

**1. 启动后端服务（端口8765）**

```bash
cd akshare-web
mvn spring-boot:run
```

**2. 启动前端服务（端口9876）**

```bash
cd akshare-web/src/main/frontend
npm install
npm run dev
```

**3. 访问应用**

打开浏览器访问：http://localhost:9876

### Web功能

- **实时行情**：A股实时行情数据展示
- **历史K线**：股票历史K线图（基于ECharts）
- **股票列表**：完整的A股股票列表
- **市场概况**：上交所、深交所市场概况
- **股东数据**：股东持股数据、股东户数趋势
- **财务数据**：财务报表、财务指标分析
- **机构数据**：龙虎榜数据、机构排行

## 数据源

- **东方财富网**：股票、基金、宏观数据的主要来源
- **新浪财经**：实时行情的备用数据源
- **上海证券交易所**：市场概况、退市股票
- **深圳证券交易所**：市场概况、退市股票

## 技术栈

### 后端
- Java 8
- Maven 3.6+
- OkHttp 3.12.13（HTTP客户端）
- Jackson 2.16.0（JSON处理）
- Jsoup 1.17.2（HTML解析）
- Caffeine 2.9.3（缓存）
- Spring Boot 2.7.18（Web模块）

### 前端
- Vue 2.6.14
- Element UI 2.15.13
- ECharts 5.4.0
- Axios 0.27.2

## 注意事项

1. **数据使用声明**：数据仅供学习研究使用，不构成投资建议
2. **请求频率**：请遵守数据源网站的使用条款，建议每秒不超过1次请求
3. **缓存机制**：库内置缓存机制，可缓存频繁访问的数据
4. **异常处理**：提供了完善的异常类型，包括网络异常、解析异常、限流异常等

## 异常处理

```java
try {
    DataFrame df = client.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
} catch (AkShareValidationException e) {
    // 参数验证错误
} catch (AkShareNetworkException e) {
    // 网络错误
} catch (AkShareRateLimitException e) {
    // 请求频率限制
} catch (AkShareException e) {
    // 其他错误
}
```

## 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 提交Bug报告
- 提交功能建议
- 提交代码改进
- 完善文档

请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细信息。

## 更新日志

请查看 [CHANGELOG.md](CHANGELOG.md) 了解版本更新历史。

## 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 免责声明

本库仅供教育和研究目的使用。提供的数据仅供参考，不应作为投资决策的唯一依据。作者不对因使用本库而产生的任何损失负责。

## 致谢

本项目灵感来源于 Python 版本的 [AKShare](https://www.akshare.xyz/)，感谢 Albert King 开发的优秀开源项目。

## 相关链接

- [AKShare Python版](https://www.akshare.xyz/)
- [东方财富网](https://www.eastmoney.com/)
- [问题反馈](https://github.com/your-username/akshare-java/issues)

---

<p align="center">
  如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！
</p>
