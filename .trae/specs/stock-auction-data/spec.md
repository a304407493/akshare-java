# 竞价数据获取功能 - 产品需求文档

## Overview
- **Summary**: 开发股票竞价数据获取功能，提供集合竞价数据、实时买卖五档数据及竞价明细数据的获取和展示
- **Purpose**: 为用户提供完整的股票竞价相关数据，帮助用户更好地分析股票交易情况和市场情绪
- **Target Users**: 股票投资者、交易员、金融分析师

## Goals
- 提供集合竞价数据获取接口
- 提供实时买卖五档数据获取接口
- 提供竞价明细数据获取接口
- 前端页面展示竞价数据
- 与现有系统架构保持一致

## Non-Goals (Out of Scope)
- 不提供竞价预测功能
- 不提供基于竞价数据的自动交易功能
- 不提供历史竞价数据回溯（除非外部API支持）

## Background & Context
- 当前系统已支持实时行情、历史K线、股东数据、财务数据等功能
- 竞价数据是股票交易的重要组成部分，对交易决策有重要参考价值
- 需要复用现有的 StockClient、Service、Controller 架构模式
- 数据来源主要是东方财富网等第三方金融数据平台

## Functional Requirements
- **FR-1**: 获取A股集合竞价数据
- **FR-2**: 获取A股实时买卖五档数据
- **FR-3**: 获取A股竞价明细数据
- **FR-4**: 提供竞价数据的REST API接口
- **FR-5**: 前端页面展示竞价数据

## Non-Functional Requirements
- **NFR-1**: API响应时间 < 3秒
- **NFR-2**: 支持缓存机制，减少外部API调用
- **NFR-3**: 数据格式与现有DataFrame保持一致
- **NFR-4**: 错误处理友好，提供清晰的错误信息

## Constraints
- **Technical**: Java 8, Spring Boot 2.7.18, Vue.js 2.x
- **Business**: 遵守数据源的使用条款和限制
- **Dependencies**: 东方财富网API, OkHttp, Jackson

## Assumptions
- 东方财富网提供竞价数据相关API
- 竞价数据在交易时间内可正常获取
- 用户了解竞价数据的含义和使用方法

## Acceptance Criteria

### AC-1: 集合竞价数据获取
- **Given**: 用户请求获取集合竞价数据
- **When**: 调用集合竞价数据API
- **Then**: 返回包含开盘价、成交量、成交额等集合竞价相关字段的数据
- **Verification**: `programmatic`
- **Notes**: 数据格式为DataFrame，字段清晰可识别

### AC-2: 买卖五档数据获取
- **Given**: 用户请求获取指定股票的买卖五档数据
- **When**: 调用买卖五档数据API并传入股票代码
- **Then**: 返回包含买一到买五、卖一到卖五的价格和数量的数据
- **Verification**: `programmatic`
- **Notes**: 数据格式为DataFrame，包含10个档位的价格和数量

### AC-3: 竞价明细数据获取
- **Given**: 用户请求获取竞价明细数据
- **When**: 调用竞价明细数据API
- **Then**: 返回包含每笔竞价记录的时间、价格、数量等信息的数据
- **Verification**: `programmatic`
- **Notes**: 如外部API不支持则此功能可跳过

### AC-4: REST API接口可用性
- **Given**: 竞价数据功能已实现
- **When**: 通过HTTP请求调用竞价数据API
- **Then**: API返回200状态码和正确格式的JSON数据
- **Verification**: `programmatic`
- **Notes**: 使用统一的Result响应格式

### AC-5: 前端页面展示
- **Given**: 用户访问竞价数据页面
- **When**: 页面加载并获取竞价数据
- **Then**: 页面清晰展示竞价数据，包括集合竞价、买卖五档等信息
- **Verification**: `human-judgment`
- **Notes**: 页面样式与现有页面保持一致

### AC-6: 缓存机制正常工作
- **Given**: 连续多次请求相同的竞价数据
- **When**: 第二次及后续请求在缓存有效期内
- **Then**: 直接返回缓存数据，不调用外部API
- **Verification**: `programmatic`
- **Notes**: 缓存时间可配置，默认5分钟

## Open Questions
- [ ] 东方财富网是否提供竞价明细数据API？
- [ ] 是否需要支持历史竞价数据查询？
- [ ] 竞价数据的更新频率应该是多少？
