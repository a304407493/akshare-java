# 贡献指南

感谢您对 AKShare Java 项目的关注！我们欢迎所有形式的贡献，包括但不限于提交 Bug 报告、功能建议、代码改进和文档完善。

## 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
  - [报告 Bug](#报告-bug)
  - [提交功能建议](#提交功能建议)
  - [提交代码](#提交代码)
- [开发环境设置](#开发环境设置)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [审查流程](#审查流程)

## 行为准则

参与本项目即表示您同意遵守以下行为准则：

- 尊重所有参与者，无论其经验水平如何
- 接受建设性的批评，并优雅地处理它
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

## 如何贡献

### 报告 Bug

如果您发现了 Bug，请通过 [GitHub Issues](https://github.com/a304407493/akshare-java/issues) 提交报告，并包含以下信息：

1. **问题描述**：清晰简洁地描述 Bug
2. **复现步骤**：详细说明如何复现该问题
3. **期望行为**：描述您期望发生的行为
4. **实际行为**：描述实际发生的行为
5. **环境信息**：
   - Java 版本
   - Maven 版本
   - 操作系统
6. **代码示例**：如果可能，提供最小可复现代码

### 提交功能建议

如果您有新功能建议，请通过 GitHub Issues 提交，并包含：

1. **功能描述**：清晰描述您想要的功能
2. **使用场景**：说明该功能的使用场景
3. **预期行为**：描述该功能应该如何工作
4. **可能的实现方案**（可选）：如果您有实现思路，可以分享

### 提交代码

1. **Fork 仓库**：点击右上角的 Fork 按钮
2. **克隆仓库**：
   ```bash
   git clone https://github.com/a304407493/akshare-java.git
   cd akshare-java
   ```
3. **创建分支**：
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix-name
   ```
4. **进行更改**：编写代码并添加测试
5. **提交更改**：
   ```bash
   git add .
   git commit -m "feat: 添加新功能描述"
   ```
6. **推送到 Fork**：
   ```bash
   git push origin feature/your-feature-name
   ```
7. **创建 Pull Request**：在 GitHub 上创建 PR

## 开发环境设置

### 环境要求

- Java 8 或更高版本
- Maven 3.6 或更高版本
- Git

### 构建项目

```bash
# 克隆仓库
git clone https://github.com/your-username/akshare-java.git
cd akshare-java

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

### 导入 IDE

#### IntelliJ IDEA

1. 打开 IDEA
2. 选择 `File` -> `Open`
3. 选择项目根目录的 `pom.xml`
4. 等待 Maven 导入完成

#### Eclipse

1. 打开 Eclipse
2. 选择 `File` -> `Import` -> `Maven` -> `Existing Maven Projects`
3. 选择项目根目录
4. 点击 `Finish`

#### VS Code

1. 安装 Java Extension Pack
2. 打开项目文件夹
3. 等待 Java 项目导入完成

## 代码规范

### Java 代码规范

1. **命名规范**：
   - 类名：PascalCase（如 `StockClient`）
   - 方法名：camelCase（如 `getStockData`）
   - 变量名：camelCase（如 `stockCode`）
   - 常量：UPPER_SNAKE_CASE（如 `MAX_RETRY_COUNT`）

2. **代码格式**：
   - 使用 4 个空格缩进
   - 每行最大长度 120 个字符
   - 大括号使用 K&R 风格

3. **注释规范**：
   - 所有公共类和方法必须添加 JavaDoc 注释
   - 复杂逻辑需要添加行内注释
   - 使用中文注释

示例：

```java
/**
 * 获取A股实时行情数据
 * 
 * @param symbol 股票代码
 * @return 包含实时行情数据的DataFrame
 * @throws AkShareNetworkException 网络请求失败时抛出
 */
public DataFrame stockZhASpotEm(String symbol) {
    // 参数校验
    if (symbol == null || symbol.isEmpty()) {
        throw new AkShareValidationException("股票代码不能为空");
    }
    
    // 构建请求URL
    String url = buildUrl(symbol);
    
    // 发送请求并解析数据
    return executeRequest(url);
}
```

### 测试规范

1. 所有新功能必须包含单元测试
2. 测试类命名：`被测试类名Test`（如 `StockClientTest`）
3. 测试方法命名：`test被测试方法名` 或使用 `@DisplayName`
4. 使用 JUnit 5 进行测试

示例：

```java
@Test
@DisplayName("测试获取A股实时数据")
void testStockZhASpotEm() {
    StockClient client = new StockClient();
    DataFrame df = client.stockZhASpotEm();
    
    assertNotNull(df);
    assertTrue(df.rowCount() > 0);
}
```

### 日志规范

1. 使用 SLF4J 进行日志记录
2. 日志级别使用规范：
   - `ERROR`：错误，需要立即处理
   - `WARN`：警告，需要注意
   - `INFO`：重要信息，如服务启动
   - `DEBUG`：调试信息
   - `TRACE`：详细跟踪信息

3. 使用占位符格式，避免字符串拼接：

```java
// 正确
log.info("获取股票数据成功，代码: {}, 数据条数: {}", stockCode, rowCount);

// 错误
log.info("获取股票数据成功，代码: " + stockCode + ", 数据条数: " + rowCount);
```

## 提交规范

### 提交信息格式

提交信息应遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型（Type）

- `feat`：新功能
- `fix`：Bug 修复
- `docs`：文档更新
- `style`：代码格式（不影响代码运行的变动）
- `refactor`：重构（既不是新增功能，也不是修改 Bug）
- `perf`：性能优化
- `test`：增加测试
- `chore`：构建过程或辅助工具的变动

### 范围（Scope）

范围是可选的，用于说明提交影响的范围，如：

- `stock`：股票模块
- `fund`：基金模块
- `core`：核心模块
- `web`：Web模块
- `docs`：文档

### 示例

```
feat(stock): 添加获取股票财务数据功能

新增 StockFinancialService 类，支持获取：
- 财务报表
- 利润表
- 资产负债表
- 现金流量表

Closes #123
```

```
fix(core): 修复缓存过期时间计算错误

缓存过期时间使用了错误的单位，导致缓存过早失效。
将毫秒转换为秒，修复此问题。

Fixes #456
```

## 审查流程

1. **创建 PR 后**：
   - 确保所有 CI 检查通过
   - 确保代码审查者已分配

2. **代码审查**：
   - 维护者会审查您的代码
   - 可能会提出修改建议
   - 请及时响应审查意见

3. **合并前**：
   - 解决所有审查意见
   - 确保分支与主分支保持同步
   - 所有测试通过

4. **合并后**：
   - 您的贡献将被合并到主分支
   - 感谢您对项目的贡献！

## 发布流程

项目维护者会定期发布新版本：

1. 更新 `CHANGELOG.md`
2. 更新版本号
3. 创建 Git Tag
4. 发布到 Maven Central（计划中）

## 获取帮助

如果您在贡献过程中遇到问题，可以通过以下方式获取帮助：

- 查看 [README.md](README.md) 和 [文档](docs/)
- 在 [GitHub Issues](https://github.com/a304407493/akshare-java/issues) 中提问
- 查看已有的 Issues 和 PR

## 致谢

再次感谢您对 AKShare Java 项目的贡献！
