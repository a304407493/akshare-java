package com.akshare.stock;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.stock.client.StockClient;
import org.junit.jupiter.api.Test;

/**
 * Integration test for StockClient
 * Demonstrates complete usage of the API
 */
public class StockClientIntegrationTest {

    @Test
    void testCompleteWorkflow() {
        StockClient client = new StockClient();

        System.out.println("=== AKShare Java Integration Test ===\n");

        // 1. Get real-time spot data
        System.out.println("1. Fetching A-share real-time data from East Money...");
        DataFrame spotData = client.stockZhASpotEm();
        System.out.println("   Total stocks: " + spotData.rowCount());
        System.out.println("   Sample data:");
        spotData.head(5).print();
        System.out.println();

        // 2. Get historical data for a specific stock
        System.out.println("2. Fetching historical data for Ping An Bank (000001)...");
        DataFrame histData = client.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
        System.out.println("   Total records: " + histData.rowCount());
        System.out.println("   First 5 records:");
        histData.head(5).print();
        System.out.println();

        // 3. Get weekly data
        System.out.println("3. Fetching weekly data for Ping An Bank (000001)...");
        DataFrame weeklyData = client.stockZhAHist("000001", "weekly", "20230101", "20240301", "qfq");
        System.out.println("   Total records: " + weeklyData.rowCount());
        weeklyData.head(5).print();
        System.out.println();

        // 4. Get minute data
        System.out.println("4. Fetching 5-minute data for Ping An Bank (000001)...");
        DataFrame minData = client.stockZhAHistMinEm("000001", 5, "qfq");
        System.out.println("   Total records: " + minData.rowCount());
        if (minData.rowCount() > 0) {
            minData.head(5).print();
        }
        System.out.println();

        // 5. DataFrame operations
        System.out.println("5. DataFrame operations demo:");
        System.out.println("   Filtering stocks with change > 5%...");
        DataFrame filtered = spotData.filter(row -> {
            Double change = row.getDouble("涨跌幅");
            return change != null && change > 5.0;
        });
        System.out.println("   Filtered count: " + filtered.rowCount());
        if (filtered.rowCount() > 0) {
            filtered.select("代码", "名称", "最新价", "涨跌幅").head(5).print();
        }
        System.out.println();

        // 6. Export data
        System.out.println("6. Export data demo:");
        String csv = histData.head(3).toCsv();
        System.out.println("   CSV export (first 3 rows):");
        System.out.println(csv);

        System.out.println("=== Integration Test Complete ===");
    }
}
