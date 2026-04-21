# 故障排查指南

本文档帮助您解决使用 AKShare Java 过程中遇到的常见问题。

## 目录

- [编译问题](#编译问题)
- [运行时问题](#运行时问题)
- [数据获取问题](#数据获取问题)
- [网络问题](#网络问题)
- [Web模块问题](#web模块问题)
- [性能问题](#性能问题)

## 编译问题

### 问题：Maven编译失败，提示"找不到符号"

**症状**：
```
[ERROR] /path/to/File.java:[10,20] 找不到符号
```

**解决方案**：
1. 清理并重新编译：
   ```bash
   mvn clean install
   ```
2. 检查依赖是否完整：
   ```bash
   mvn dependency:tree
   ```
3. 强制更新依赖：
   ```bash
   mvn clean install -U
   ```

### 问题：编码错误，中文字符显示乱码

**症状**：
```
[ERROR] 编码GBK的不可映射字符
```

**解决方案**：
在 `pom.xml` 中确保设置了 UTF-8 编码：
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

### 问题：Lombok注解不生效

**症状**：
编译时提示找不到 getter/setter 方法

**解决方案**：
1. 确保 IDE 已安装 Lombok 插件
2. 在 `pom.xml` 中启用注解处理器：
   ```xml
   <annotationProcessorPaths>
       <path>
           <groupId>org.projectlombok</groupId>
           <artifactId>lombok</artifactId>
           <version>${lombok.version}</version>
       </path>
   </annotationProcessorPaths>
   ```

## 运行时问题

### 问题：ClassNotFoundException

**症状**：
```
Exception in thread "main" java.lang.ClassNotFoundException: com.akshare.stock.client.StockClient
```

**解决方案**：
1. 确保已正确添加依赖到 classpath
2. 检查 jar 包是否已正确打包
3. 使用 Maven 运行：
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.YourClass"
   ```

### 问题：NoClassDefFoundError

**症状**：
```
Exception in thread "main" java.lang.NoClassDefFoundError: okhttp3/OkHttpClient
```

**解决方案**：
1. 确保所有依赖都已添加到 classpath
2. 使用 Maven 打包可执行 jar：
   ```bash
   mvn clean package
   java -jar target/your-project-1.0.0.jar
   ```

### 问题：OutOfMemoryError

**症状**：
```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

**解决方案**：
1. 增加 JVM 内存：
   ```bash
   java -Xmx1024m -Xms512m YourClass
   ```
2. 优化代码，避免一次性加载大量数据
3. 使用分页获取数据

## 数据获取问题

### 问题：返回空数据

**症状**：
DataFrame 的 rowCount 为 0

**排查步骤**：
1. 检查股票代码是否正确
2. 检查日期格式是否为 `yyyyMMdd`
3. 检查日期范围是否合理（非交易日可能无数据）
4. 开启调试日志查看原始响应：
   ```java
   // 在 logback.xml 中设置
   <logger name="com.akshare" level="DEBUG"/>
   ```

### 问题：数据解析错误

**症状**：
```
com.akshare.core.exception.AkShareParseException: 数据解析失败
```

**解决方案**：
1. 检查数据源是否已更改页面结构
2. 更新到最新版本
3. 提交 Issue 报告问题

### 问题：股票代码格式错误

**症状**：
```
com.akshare.core.exception.AkShareValidationException: 股票代码格式错误
```

**解决方案**：
- 使用纯数字代码，如 `000001`（平安银行）
- 不要添加后缀（如 `.SZ`、`.SH`）
- 创业板代码以 `300` 开头
- 科创板代码以 `688` 开头

## 网络问题

### 问题：连接超时

**症状**：
```
com.akshare.core.exception.AkShareNetworkException: Connect timed out
```

**解决方案**：
1. 增加超时时间：
   ```java
   StockClient client = new StockClient();
   client.setTimeout(60000); // 60秒
   ```
2. 检查网络连接
3. 检查是否需要代理

### 问题：需要代理才能访问

**症状**：
连接被拒绝或超时，但浏览器可以正常访问

**解决方案**：
```java
StockClient client = new StockClient();
client.setProxy("proxy.company.com", 8080);

// 如果需要认证
client.setProxy("proxy.company.com", 8080, "username", "password");
```

### 问题：403 Forbidden

**症状**：
```
Server returned HTTP response code: 403
```

**原因和解决方案**：
1. **请求频率过高**：降低请求频率，建议每秒不超过1次
2. **IP被限制**：更换IP或等待一段时间再试
3. **缺少请求头**：库已自动添加常见请求头，如仍有问题请提交 Issue

### 问题：SSL证书错误

**症状**：
```
javax.net.ssl.SSLHandshakeException
```

**解决方案**：
1. 更新 Java 版本
2. 检查系统时间是否正确
3. 如果是自签名证书，需要添加到信任库

## Web模块问题

### 问题：后端启动失败

**症状**：
```
Error starting ApplicationContext
```

**排查步骤**：
1. 检查端口是否被占用：
   ```bash
   netstat -ano | findstr 8765
   ```
2. 检查配置文件是否正确
3. 查看详细错误日志

### 问题：前端编译失败

**症状**：
```
npm ERR! code ENOENT
```

**解决方案**：
1. 确保已安装 Node.js 12+
2. 删除 node_modules 重新安装：
   ```bash
   rm -rf node_modules
   npm install
   ```
3. 清除 npm 缓存：
   ```bash
   npm cache clean --force
   ```

### 问题：前后端无法通信

**症状**：
前端页面显示"网络错误"或"无法连接"

**排查步骤**：
1. 检查后端服务是否已启动
2. 检查前端配置的 API 地址：
   ```javascript
   // akshare-web/src/main/frontend/src/utils/api.js
   const BASE_URL = 'http://localhost:8765/api';
   ```
3. 检查浏览器控制台是否有跨域错误
4. 检查后端 CORS 配置

### 问题：前端显示空白页面

**症状**：
页面加载后显示空白

**解决方案**：
1. 检查浏览器控制台是否有 JavaScript 错误
2. 检查路由配置是否正确
3. 检查是否缺少依赖：
   ```bash
   npm install
   ```

## 性能问题

### 问题：数据获取速度慢

**原因和解决方案**：
1. **网络延迟**：使用缓存减少请求次数
2. **数据量大**：使用分页或限制返回行数
3. **解析耗时**：优化解析逻辑

```java
// 使用缓存
StockClient client = new StockClient();
DataFrame data = client.stockZhASpotEm(); // 首次请求
// ... 其他操作
data = client.stockZhASpotEm(); // 从缓存获取，速度更快
```

### 问题：内存占用过高

**解决方案**：
1. 及时释放不用的 DataFrame 对象
2. 使用 `head()`、`tail()` 等方法减少数据量
3. 调整 JVM 内存参数
4. 使用流式处理大数据量

### 问题：CPU占用高

**可能原因**：
1. 频繁的数据解析操作
2. 大量的数据过滤和排序

**解决方案**：
1. 使用缓存避免重复解析
2. 优化过滤条件
3. 在数据源端进行过滤（如果支持）

## 日志调试

### 开启调试日志

在 `logback.xml` 中添加：

```xml
<!-- 开启所有 DEBUG 日志 -->
<root level="DEBUG">
    <appender-ref ref="CONSOLE"/>
</root>

<!-- 或只开启特定包的 DEBUG 日志 -->
<logger name="com.akshare.core.http" level="DEBUG"/>
<logger name="com.akshare.stock" level="DEBUG"/>
```

### 查看 HTTP 请求详情

```xml
<logger name="okhttp3.logging.HttpLoggingInterceptor" level="DEBUG"/>
```

## 获取帮助

如果以上方法都无法解决您的问题：

1. 收集以下信息：
   - 错误日志（完整堆栈跟踪）
   - Java 版本 (`java -version`)
   - Maven 版本 (`mvn -version`)
   - 操作系统版本
   - 复现代码

2. 提交 Issue：
   - 访问 [GitHub Issues](https://github.com/a304407493/akshare-java/issues)
   - 使用问题模板创建新 Issue
   - 提供上述收集的信息

3. 社区支持：
   - 查看已有的 Issues 是否有类似问题
   - 在相关 Issue 下留言讨论
