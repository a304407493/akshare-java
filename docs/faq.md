# 常见问题 (FAQ)

## 一般问题

### Q: AKShare Java 是什么？

A: AKShare Java 是 Python 版本 [AKShare](https://www.akshare.xyz/) 的 Java 实现，提供访问中国金融市场数据的功能，包括股票、基金、期货、宏观经济指标和债券等数据。

### Q: AKShare Java 和 Python 版本的 AKShare 有什么关系？

A: AKShare Java 灵感来源于 Python 版本的 AKShare，但它是完全独立的 Java 实现。两者的 API 设计有所不同，但数据源相同。

### Q: 数据是实时的吗？

A: 数据来源于东方财富、新浪财经等公开数据源，实时性取决于数据源本身的更新频率。股票实时行情通常延迟15秒左右。

### Q: 数据可以免费使用吗？

A: 数据仅供学习研究使用，不构成投资建议。请遵守数据源网站的使用条款。

## 安装问题

### Q: 编译失败，提示找不到依赖？

A: 请检查：
1. Maven 版本是否为 3.6+
2. Java 版本是否为 8+
3. 网络连接是否正常（需要下载依赖）
4. 尝试使用 `mvn clean install -U` 强制更新依赖

### Q: 如何导入到 IDE 中？

A: 
- **IntelliJ IDEA**: File -> Open -> 选择 pom.xml
- **Eclipse**: File -> Import -> Maven -> Existing Maven Projects
- **VS Code**: 安装 Java Extension Pack 后打开项目文件夹

### Q: 支持哪些 Java 版本？

A: 支持 Java 8 及以上版本。推荐使用 Java 8 或 Java 11。

## 使用问题

### Q: 获取数据时返回空数据？

A: 可能的原因：
1. 股票代码错误（如缺少后缀）
2. 日期格式错误（应为 yyyyMMdd 格式）
3. 该日期没有交易数据（节假日）
4. 数据源暂时不可用

### Q: 请求超时怎么办？

A: 可以设置超时时间：

```java
StockClient client = new StockClient();
client.setTimeout(60000); // 设置为60秒
```

### Q: 如何设置代理？

A: 

```java
StockClient client = new StockClient();
client.setProxy("proxy.example.com", 8080);
```

### Q: 请求频率有限制吗？

A: 建议每秒不超过1次请求，避免给数据源造成压力。库内置了缓存机制，可以缓存频繁访问的数据。

### Q: 如何清除缓存？

A:

```java
StockClient client = new StockClient();
client.clearCache();
```

## 数据问题

### Q: 股票代码格式是什么？

A: 使用纯数字代码，如：
- 平安银行：`000001`
- 贵州茅台：`600519`
- 宁德时代：`300750`

不需要添加后缀（如.SZ、.SH）。

### Q: 日期格式是什么？

A: 使用 `yyyyMMdd` 格式，如 `20240101` 表示2024年1月1日。

### Q: 复权类型有哪些？

A:
- `qfq`：前复权
- `hfq`：后复权
- `none`：不复权

### Q: 周期参数有哪些？

A:
- `daily`：日线
- `weekly`：周线
- `monthly`：月线

分钟级数据使用数字：1、5、15、30、60

## Web模块问题

### Q: Web模块如何启动？

A: 需要分别启动后端和前端：

```bash
# 启动后端（端口8765）
cd akshare-web
mvn spring-boot:run

# 启动前端（端口9876）
cd akshare-web/src/main/frontend
npm install
npm run dev
```

### Q: 前端启动失败，提示找不到模块？

A: 请确保已安装 Node.js 12+，并执行 `npm install` 安装依赖。

### Q: 如何修改端口？

A:
- 后端端口：修改 `akshare-web/src/main/resources/application.yml` 中的 `server.port`
- 前端端口：修改 `akshare-web/src/main/frontend/vue.config.js` 中的 `devServer.port`

### Q: 前后端无法通信？

A: 检查：
1. 后端服务是否已启动
2. 前端配置的后端地址是否正确（在 `api.js` 中配置）
3. 浏览器控制台是否有跨域错误

## 性能问题

### Q: 内存不足怎么办？

A: 可以调整 JVM 内存参数：

```bash
set MAVEN_OPTS=-Xmx1024m -Xms512m
mvn spring-boot:run
```

### Q: 如何提高数据获取速度？

A:
1. 使用缓存机制
2. 减少请求频率
3. 只获取需要的列
4. 使用合适的数据过滤

### Q: 缓存数据会过期吗？

A: 是的，缓存有过期时间。默认情况下，数据会缓存一段时间，过期后会自动重新获取。

## 开发问题

### Q: 如何添加新的数据接口？

A: 参考现有模块的实现：
1. 在对应模块的 service 包中添加服务类
2. 实现数据获取和解析逻辑
3. 在 client 类中添加对应方法
4. 添加单元测试

### Q: 如何调试 HTTP 请求？

A: 可以开启日志：

```xml
<!-- 在 logback.xml 中添加 -->
<logger name="com.akshare.core.http" level="DEBUG"/>
```

### Q: 如何运行测试？

A:

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=StockClientTest
```

## 其他问题

### Q: 发现 Bug 如何报告？

A: 请在 [GitHub Issues](https://github.com/a304407493/akshare-java/issues) 提交，并包含：
1. 问题描述
2. 复现步骤
3. 期望行为和实际行为
4. 环境信息（Java版本、操作系统等）

### Q: 如何贡献代码？

A: 请查看 [贡献指南](../CONTRIBUTING.md)。

### Q: 有交流群吗？

A: 目前主要通过 GitHub Issues 进行交流。

## 还是没有找到答案？

如果以上 FAQ 没有解决您的问题，请：

1. 查看 [故障排查](troubleshooting.md)
2. 搜索 [GitHub Issues](https://github.com/a304407493/akshare-java/issues)
3. 提交新的 Issue
