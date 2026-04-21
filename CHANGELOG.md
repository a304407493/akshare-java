# 更新日志

所有重要的变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 计划添加更多股票数据接口
- 计划添加期权数据支持
- 计划发布到 Maven Central

## [1.0.0] - 2026-04-21

### 新增

#### 核心功能
- **akshare-core 模块**：HTTP客户端、数据解析器、缓存管理、DataFrame实现
- **akshare-common 模块**：公共枚举和常量定义
- **异常处理体系**：
  - `AkShareException` - 基础异常类
  - `AkShareNetworkException` - 网络异常
  - `AkShareParseException` - 数据解析异常
  - `AkShareValidationException` - 参数验证异常
  - `AkShareRateLimitException` - 限流异常

#### 股票模块 (akshare-stock)
- 实时行情数据（东方财富、新浪财经双数据源）
- 历史K线数据（日K、周K、月K）
- 分钟级K线数据（1/5/15/30/60分钟）
- 股票代码名称列表
- 上交所/深交所市场概况
- 个股基础信息
- 股东数据（前十大股东、股东户数趋势）
- 财务数据（财务报表、利润表、资产负债表、现金流量表、财务指标）
- 机构数据（龙虎榜详情、营业部排行）
- 退市股票数据
- 竞价数据

#### 基金模块 (akshare-fund)
- 开放式基金每日净值
- 基金名称列表
- ETF实时行情

#### 期货模块 (akshare-futures)
- 期货实时行情
- 期货日线数据

#### 宏观模块 (akshare-macro)
- 中国GDP数据
- 中国CPI数据
- 中国PPI数据
- 中国PMI数据

#### 债券模块 (akshare-bond)
- 可转债数据

#### Web可视化模块 (akshare-web)
- Spring Boot 后端服务
- Vue 2 + Element UI 前端
- 实时行情页面
- 历史K线图（ECharts）
- 股票列表页面
- 市场概况页面
- 股东数据页面
- 财务数据页面
- 机构数据页面

#### 示例模块 (examples)
- QuickStart 快速入门示例

### 技术特性
- 基于 Java 8 开发
- Maven 多模块项目管理
- OkHttp HTTP客户端
- Jackson JSON处理
- Jsoup HTML解析
- Caffeine 本地缓存
- SLF4J + Logback 日志
- JUnit 5 单元测试

### 修复
- 修复个股龙虎榜详情接口返回空数据的问题
- 修复缓存过期时间计算错误

## [0.1.0] - 2026-03-01

### 新增
- 项目初始化
- 基础架构搭建
- 核心模块开发
- 股票基础数据接口

---

## 版本说明

### 版本号格式
版本号格式：主版本号.次版本号.修订号（MAJOR.MINOR.PATCH）

- **主版本号**：做了不兼容的 API 修改
- **次版本号**：做了向下兼容的功能性新增
- **修订号**：做了向下兼容的问题修正

### 标签说明
- `[新增]` - 新功能
- `[修复]` - Bug修复
- `[变更]` - 对现有功能的变更
- `[弃用]` - 即将移除的功能
- `[移除]` - 移除的功能
- `[安全]` - 安全相关的修复
