import com.akshare.stock.client.StockClient;
import com.akshare.fund.client.FundClient;
import com.akshare.futures.client.FuturesClient;
import com.akshare.macro.client.MacroClient;
import com.akshare.bond.client.BondClient;
import com.akshare.core.dataframe.DataFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * AKShare Java 快速入门示例
 */
@Slf4j
public class QuickStart {

    public static void main(String[] args) {
        System.out.println("=== AKShare Java 快速入门 ===\n");

        // 股票示例
        stockExample();

        // 基金示例
        fundExample();

        // 期货示例（可能403，添加异常处理）
        futuresExample();

        // 宏观示例
        macroExample();

        // 债券示例
        bondExample();
    }

    static void stockExample() {
        System.out.println("--- 股票数据示例 ---");
        try {
            StockClient client = new StockClient();

            // 获取实时数据
            DataFrame spotData = client.stockZhASpotEm();
            System.out.println("A股实时数据数量: " + spotData.rowCount());

            // 获取历史数据（使用最近3个月的日期范围）
            String endDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, -3);
            String startDate = new java.text.SimpleDateFormat("yyyyMMdd").format(cal.getTime());
            DataFrame histData = client.stockZhAHist("000001", "daily", startDate, endDate, "qfq");
            System.out.println("历史数据数量: " + histData.rowCount());

            // 获取分钟数据
            DataFrame minData = client.stockZhAHistMinEm("000001", 5, "qfq");
            System.out.println("5分钟数据数量: " + minData.rowCount());
        } catch (Exception e) {
            System.err.println("获取股票数据失败: " + e.getMessage());
        }
        System.out.println();
    }

    static void fundExample() {
        System.out.println("--- 基金数据示例 ---");
        try {
            FundClient client = new FundClient();

            // 获取开放基金日数据
            DataFrame fundData = client.fundEmOpenFundDaily();
            System.out.println("开放基金数量: " + fundData.rowCount());

            // 获取基金名称列表
            DataFrame fundNames = client.fundEmFundName();
            System.out.println("基金名称数量: " + fundNames.rowCount());

            // 获取ETF数据
            DataFrame etfData = client.fundEtfSpotEm();
            System.out.println("ETF数量: " + etfData.rowCount());
        } catch (Exception e) {
            System.err.println("获取基金数据失败: " + e.getMessage());
        }
        System.out.println();
    }

    static void futuresExample() {
        System.out.println("--- 期货数据示例 ---");
        try {
            FuturesClient client = new FuturesClient();

            // 获取期货实时数据（可能403）
            DataFrame futuresData = client.futuresZhSpot();
            System.out.println("期货数量: " + futuresData.rowCount());

            // 获取期货日数据
            DataFrame dailyData = client.futuresZhDaily("RB0");
            System.out.println("螺纹钢日数据数量: " + dailyData.rowCount());
        } catch (Exception e) {
            System.err.println("获取期货数据失败（可能是反爬虫限制）: " + e.getMessage());
        }
        System.out.println();
    }

    static void macroExample() {
        System.out.println("--- 宏观数据示例 ---");
        try {
            MacroClient client = new MacroClient();

            // 获取GDP数据
            DataFrame gdpData = client.macroChinaGdp();
            System.out.println("GDP数据数量: " + gdpData.rowCount());

            // 获取CPI数据
            DataFrame cpiData = client.macroChinaCpi();
            System.out.println("CPI数据数量: " + cpiData.rowCount());

            // 获取PPI数据
            DataFrame ppiData = client.macroChinaPpi();
            System.out.println("PPI数据数量: " + ppiData.rowCount());

            // 获取PMI数据
            DataFrame pmiData = client.macroChinaPmi();
            System.out.println("PMI数据数量: " + pmiData.rowCount());
        } catch (Exception e) {
            System.err.println("获取宏观数据失败: " + e.getMessage());
        }
        System.out.println();
    }

    static void bondExample() {
        System.out.println("--- 债券数据示例 ---");
        try {
            BondClient client = new BondClient();

            // 获取可转债数据
            DataFrame bondData = client.bondZhCov();
            System.out.println("可转债数量: " + bondData.rowCount());
        } catch (Exception e) {
            System.err.println("获取债券数据失败: " + e.getMessage());
        }
        System.out.println();
    }
}
