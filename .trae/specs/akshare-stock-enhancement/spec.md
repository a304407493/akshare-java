# AKShare Java Stock模块功能增强与可视化规格文档

## 背景与目标

当前Java版本的akshare-stock模块功能相对简单，仅实现了基础的股票实时行情、历史K线和基本信息查询。而Python版本的AKShare提供了超过122个股票数据接口，涵盖市场概况、股票名录、行情数据、财务数据、股东数据、机构数据等全方位功能。

本规格旨在：
1. 对比Python版本AKShare，补充Java版本缺失的股票功能
2. 将所有功能通过Web可视化界面展示
3. 提供完整的REST API接口供外部调用

## 现状分析

### 当前已实现功能
- 实时行情数据（东方财富、新浪财经）
- 历史K线数据（日周月、分钟线）
- 股票基本信息（代码名称、市场概况、个股资料）

### Python版本AKShare完整功能（122+接口）

#### 1. 市场概况与基本信息（12个接口）
- 交易所市场总貌（上交所、深交所、北交所）
- 地区与行业统计
- 股票名录管理（A股、沪市、深市、北交所）
- 股票状态变更（退市、更名、两网及退市）
- 个股基础资料

#### 2. 实时行情数据（8个接口）
- A股实时行情（东方财富、新浪）
- 港股实时行情
- 美股实时行情
- 次新股行情
- 科创板行情
- 创业板行情

#### 3. 历史行情数据（15个接口）
- A股历史K线（日周月）
- 分钟K线（1/5/15/30/60分钟）
- 分时数据
- 复权数据（前复权、后复权）

#### 4. 财务数据（25个接口）
- 财务报表（资产负债表、利润表、现金流量表）
- 主要财务指标
- 盈利能力指标
- 偿债能力指标
- 成长能力指标
- 营运能力指标

#### 5. 股东与股本数据（12个接口）
- 十大流通股东
- 十大股东
- 股东户数
- 股东增减持
- 股本变动
- 解禁股

#### 6. 机构数据（18个接口）
- 龙虎榜数据
- 机构席位追踪
- 营业部排行
- 基金持仓
- 北向资金
- 融资融券

#### 7. 分红融资数据（10个接口）
- 分红配送
- 增发配股
- IPO数据
- 可转债

#### 8. 新闻公告数据（8个接口）
- 公司公告
- 新闻资讯
- 研究报告
- 重大事项

#### 9. 特色数据（14个接口）
- 技术指标
- 估值指标
- 资金流向
- 板块资金
- 个股资金

## 功能增强范围

### 第一阶段：核心功能补充（优先级：高）

#### 1.1 市场概况与股票名录
- **stock_szse_summary**: 深交所市场总貌
- **stock_sse_summary**: 上交所市场总貌
- **stock_info_sh_name_code**: 沪市股票名录
- **stock_info_sz_name_code**: 深市股票名录
- **stock_info_bj_name_code**: 北交所股票名录
- **stock_info_sh_delist**: 沪市退市股票
- **stock_info_sz_delist**: 深市退市股票
- **stock_info_change_name**: 股票更名记录

#### 1.2 财务数据
- **stock_financial_report_em**: 财务报表
- **stock_financial_analysis_em**: 财务分析指标
- **stock_profit_sheet_em**: 利润表
- **stock_balance_sheet_em**: 资产负债表
- **stock_cash_flow_sheet_em**: 现金流量表

#### 1.3 股东数据
- **stock_gdfx_top_10_em**: 十大流通股东
- **stock_gdfx_free_top_10_em**: 十大股东
- **stock_gdfx_holding_detail_em**: 股东持股详情
- **stock_gdfx_free_holding_detail_em**: 流通股东持股详情

#### 1.4 机构数据
- **stock_lhb_detail_em**: 龙虎榜详情
- **stock_lhb_stock_detail_em**: 个股龙虎榜
- **stock_lhb_jgmm_em**: 机构买卖数据
- **stock_lhb_yybph_em**: 营业部排行

### 第二阶段：扩展功能（优先级：中）

#### 2.1 分红融资
- **stock_fhps_em**: 分红配送
- **stock_fhps_detail_em**: 分红详情
- **stock_ipo_info**: IPO信息

#### 2.2 资金流向
- **stock_fund_flow_concept**: 概念资金流向
- **stock_fund_flow_industry**: 行业资金流向
- **stock_fund_flow_individual**: 个股资金流向

#### 2.3 新闻公告
- **stock_news_em**: 个股新闻
- **stock_notice_report**: 公司公告

### 第三阶段：高级功能（优先级：低）

#### 3.1 技术指标
- **stock_zh_index_daily**: 指数行情
- **stock_zh_index_spot**: 指数实时行情

#### 3.2 特色数据
- **stock_comment_em**: 个股点评
- **stock_cg_lgjg**: 机构调研

## 技术架构

### 后端架构
```
akshare-stock/
├── client/
│   └── StockClient.java (统一入口)
├── service/
│   ├── StockRealtimeService.java (实时行情)
│   ├── StockHistoryService.java (历史数据)
│   ├── StockInfoService.java (基本信息)
│   ├── StockFinancialService.java (财务数据) [新增]
│   ├── StockHolderService.java (股东数据) [新增]
│   ├── StockInstitutionService.java (机构数据) [新增]
│   ├── StockDividendService.java (分红数据) [新增]
│   └── StockFundFlowService.java (资金流向) [新增]
└── util/
    └── StockDataParser.java (数据解析工具)
```

### Web可视化架构
```
akshare-web/
├── controller/
│   ├── StockRealtimeController.java
│   ├── StockHistoryController.java
│   ├── StockInfoController.java
│   ├── StockFinancialController.java [新增]
│   ├── StockHolderController.java [新增]
│   ├── StockInstitutionController.java [新增]
│   └── StockVisualizationController.java [新增]
└── frontend/src/views/
    ├── StockRealtime.vue
    ├── StockHistory.vue
    ├── StockList.vue
    ├── MarketOverview.vue
    ├── StockFinancial.vue [新增]
    ├── StockHolder.vue [新增]
    ├── StockInstitution.vue [新增]
    ├── StockDividend.vue [新增]
    └── StockFundFlow.vue [新增]
```

## ADDED Requirements

### Requirement: 市场概况与股票名录功能
The system SHALL provide comprehensive market overview and stock listing information.

#### Scenario: 获取交易所市场总貌
- **WHEN** 用户请求上交所市场总貌
- **THEN** 系统返回主板、科创板的总市值、上市公司数量、流通市值、平均市盈率等数据

#### Scenario: 获取股票名录
- **WHEN** 用户请求A股完整股票名录
- **THEN** 系统返回所有A股股票的代码、名称、所属市场、行业等信息

#### Scenario: 获取退市股票列表
- **WHEN** 用户请求退市股票信息
- **THEN** 系统返回已退市股票的代码、名称、上市日期、退市日期等信息

### Requirement: 财务数据功能
The system SHALL provide detailed financial data for stocks.

#### Scenario: 获取财务报表
- **WHEN** 用户请求指定股票的财务报表
- **THEN** 系统返回资产负债表、利润表、现金流量表数据

#### Scenario: 获取财务指标
- **WHEN** 用户请求财务分析指标
- **THEN** 系统返回盈利能力、偿债能力、成长能力、营运能力等指标

### Requirement: 股东数据功能
The system SHALL provide shareholder and holding data.

#### Scenario: 获取十大股东
- **WHEN** 用户请求十大股东信息
- **THEN** 系统返回十大流通股东和十大股东的持股详情

#### Scenario: 获取股东户数
- **WHEN** 用户请求股东户数变化
- **THEN** 系统返回股东户数及变化趋势

### Requirement: 机构数据功能
The system SHALL provide institutional trading data.

#### Scenario: 获取龙虎榜数据
- **WHEN** 用户请求龙虎榜信息
- **THEN** 系统返回上榜股票、买卖金额、机构席位等数据

#### Scenario: 获取机构买卖
- **WHEN** 用户请求机构买卖详情
- **THEN** 系统返回机构买入、卖出金额及净额

### Requirement: Web可视化展示
The system SHALL provide comprehensive web-based visualization for all stock data.

#### Scenario: 财务数据可视化
- **WHEN** 用户访问财务数据页面
- **THEN** 系统以图表形式展示财务报表和指标

#### Scenario: 龙虎榜可视化
- **WHEN** 用户访问龙虎榜页面
- **THEN** 系统展示上榜股票、买卖席位、资金流向等可视化信息

#### Scenario: 股东数据可视化
- **WHEN** 用户访问股东数据页面
- **THEN** 系统展示股东持股变化、股东户数趋势等图表

## MODIFIED Requirements

### Requirement: StockClient统一入口
The StockClient SHALL serve as the unified entry point for all stock data APIs.

**Changes**:
- Add methods for financial data queries
- Add methods for shareholder data queries
- Add methods for institutional data queries
- Maintain backward compatibility with existing methods

## 数据源

- 东方财富网 (主要数据源)
- 新浪财经
- 上交所官网
- 深交所官网
- 北交所官网

## 性能要求

- API响应时间 < 3秒（正常网络条件下）
- 支持并发请求 100+
- 缓存策略：热点数据缓存5分钟
- 数据更新频率：实时数据秒级，历史数据日级

## 安全要求

- 所有API接口支持CORS跨域
- 请求频率限制：每分钟100次
- 数据脱敏：不涉及用户隐私数据

## 依赖要求

- Java 8+
- Spring Boot 2.7+
- Vue 2.6+
- Element UI 2.15+
- ECharts 5.0+
