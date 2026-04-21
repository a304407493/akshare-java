# AKShare Web 可视化项目

基于 Java 8 + Spring Boot + Vue 2 + Element UI + ECharts 的 AKShare 金融数据可视化系统。

## 项目结构

```
akshare-web/
├── pom.xml                              # Maven 配置
├── README.md                            # 项目说明
├── src/
│   ├── main/
│   │   ├── java/com/akshare/web/
│   │   │   ├── AkshareWebApplication.java    # Spring Boot 启动类
│   │   │   ├── common/
│   │   │   │   └── Result.java               # 通用响应类
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java           # CORS 配置
│   │   │   │   └── AkShareConfig.java        # AKShare 配置
│   │   │   └── controller/
│   │   │       └── StockController.java      # 股票数据控制器
│   │   ├── resources/
│   │   │   └── application.yml               # Spring Boot 配置
│   │   └── frontend/                         # Vue 前端项目
│   │       ├── package.json
│   │       ├── vue.config.js
│   │       ├── public/
│   │       │   └── index.html
│   │       └── src/
│   │           ├── main.js
│   │           ├── App.vue
│   │           ├── router/
│   │           │   └── index.js
│   │           ├── utils/
│   │           │   └── api.js
│   │           └── views/
│   │               ├── StockRealtime.vue      # 实时行情页面
│   │               ├── StockHistory.vue       # 历史K线页面
│   │               ├── StockList.vue          # 股票列表页面
│   │               └── MarketOverview.vue     # 市场概况页面
```

## 技术栈

### 后端
- Java 8
- Spring Boot 2.7.18
- Maven

### 前端
- Vue 2.6.14
- Vue Router 3.5.3
- Element UI 2.15.13
- ECharts 5.4.0
- Axios 0.27.2

## 功能特性

### 股票模块
- 实时行情展示（东方财富、新浪财经）
- 历史K线图（日线、周线、月线，支持复权）
- 股票代码名称列表
- 市场概况（上交所、深交所）
- 个股基础资料查询

## 快速开始

### 后端启动

1. 编译项目
```bash
cd akshare-web
mvn clean install
```

2. 运行 Spring Boot 应用
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录
```bash
cd akshare-web/src/main/frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## API 接口

### 股票相关接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/stock/realtime/em` | GET | 获取A股实时行情（东方财富） |
| `/api/stock/realtime/sina` | GET | 获取A股实时行情（新浪） |
| `/api/stock/history` | GET | 获取历史K线数据 |
| `/api/stock/history/minute` | GET | 获取分钟K线数据 |
| `/api/stock/list` | GET | 获取股票列表 |
| `/api/stock/market/sse-summary` | GET | 获取上交所市场概况 |
| `/api/stock/market/szse-summary` | GET | 获取深交所市场概况 |
| `/api/stock/info/{symbol}` | GET | 获取个股基础资料 |

## 开发说明

### 后端开发

- 控制器位于 `controller` 包
- 配置类位于 `config` 包
- 通用工具类位于 `common` 包

### 前端开发

- 页面组件位于 `views` 目录
- 路由配置位于 `router/index.js`
- API 调用封装位于 `utils/api.js`

## 注意事项

1. 确保 Java 版本为 8 或更高
2. 确保 Node.js 版本为 12 或更高
3. 后端和前端需要同时启动才能正常使用
4. 数据仅供学习研究使用，不构成投资建议

## License

MIT License
