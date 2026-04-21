# 快速开始

本指南将帮助您在5分钟内开始使用 AKShare Java。

## 环境准备

### 1. 检查Java版本

```bash
java -version
```

要求 Java 8 或更高版本。

### 2. 检查Maven版本

```bash
mvn -version
```

要求 Maven 3.6 或更高版本。

## 安装

### 方式一：Maven依赖（推荐）

在您的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.akshare</groupId>
    <artifactId>akshare-stock</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 方式二：源码构建

```bash
# 克隆仓库
git clone https://github.com/a304407493/akshare-java.git
cd akshare-java

# 编译安装
mvn clean install
```

## 第一个程序

创建一个 Java 文件 `FirstProgram.java`：

```java
import com.akshare.stock.client.StockClient;
import com.akshare.core.dataframe.DataFrame;

public class FirstProgram {
    public static void main(String[] args) {
        // 创建股票客户端
        StockClient client = new StockClient();
        
        // 获取A股实时行情
        System.out.println("正在获取A股实时行情...");
        DataFrame spotData = client.stockZhASpotEm();
        
        // 打印前10条数据
        System.out.println("\n前10条数据：");
        spotData.head(10).print();
        
        // 打印数据统计信息
        System.out.println("\n数据统计：");
        System.out.println("总行数: " + spotData.rowCount());
        System.out.println("总列数: " + spotData.columnCount());
        System.out.println("列名: " + spotData.getColumns());
    }
}
```

编译并运行：

```bash
# 编译
javac -cp "target/*:lib/*" FirstProgram.java

# 运行
java -cp ".:target/*:lib/*" FirstProgram
```

## 获取历史数据

```java
import com.akshare.stock.client.StockClient;
import com.akshare.core.dataframe.DataFrame;

public class HistoryDataExample {
    public static void main(String[] args) {
        StockClient client = new StockClient();
        
        // 获取平安银行(000001)的历史数据
        DataFrame histData = client.stockZhAHist(
            "000001",           // 股票代码
            "daily",            // 周期：daily/weekly/monthly
            "20240101",         // 开始日期
            "20240301",         // 结束日期
            "qfq"               // 复权类型：qfq(前复权)/hfq(后复权)/none(不复权)
        );
        
        System.out.println("平安银行历史数据：");
        histData.print();
    }
}
```

## 获取基金数据

```java
import com.akshare.fund.client.FundClient;
import com.akshare.core.dataframe.DataFrame;

public class FundExample {
    public static void main(String[] args) {
        FundClient client = new FundClient();
        
        // 获取开放式基金每日净值
        DataFrame fundData = client.fundEmOpenFundDaily();
        System.out.println("开放式基金数据：");
        fundData.head(10).print();
        
        // 获取ETF实时行情
        DataFrame etfData = client.fundEtfSpotEm();
        System.out.println("\nETF数据：");
        etfData.head(10).print();
    }
}
```

## DataFrame基本操作

```java
// 选择特定列
DataFrame selected = df.select("代码", "名称", "最新价", "涨跌幅");

// 过滤数据
DataFrame filtered = df.filter(row -> row.getDouble("涨跌幅") > 5.0);

// 排序
DataFrame sorted = df.sort("涨跌幅", false); // false表示降序

// 获取前N行
DataFrame top10 = df.head(10);

// 导出为JSON
String json = df.toJson();

// 导出为CSV
String csv = df.toCsv();
```

## 下一步

- 查看 [API文档](api/stock.md) 了解所有可用的数据接口
- 查看 [Web可视化](web/introduction.md) 了解可视化系统
- 查看 [故障排查](troubleshooting.md) 解决常见问题

## 获取帮助

如果在使用过程中遇到问题，请：

1. 查看 [FAQ](faq.md)
2. 搜索 [GitHub Issues](https://github.com/a304407493/akshare-java/issues)
3. 提交新的 Issue
