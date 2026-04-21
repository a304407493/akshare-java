package com.akshare.stock;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.stock.client.StockClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StockClient
 */
public class StockClientTest {

    private StockClient stockClient;

    @BeforeEach
    void setUp() {
        stockClient = new StockClient();
        stockClient.setTimeout(30000);
    }

    @Test
    void testStockZhASpotEm() {
        DataFrame df = stockClient.stockZhASpotEm();
        assertNotNull(df);
        // Note: Actual data may vary depending on market hours
        System.out.println("A-share spot data from East Money:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(5).print();
        }
    }

    @Test
    void testStockZhAHist() {
        // Test with Ping An Bank (000001)
        DataFrame df = stockClient.stockZhAHist("000001", "daily", "20240101", "20240301", "qfq");
        assertNotNull(df);
        assertTrue(df.rowCount() > 0, "Should have historical data");

        System.out.println("Historical data for 000001:");
        df.printInfo();
        df.head(5).print();

        // Verify columns
        assertTrue(df.getColumns().contains("日期"));
        assertTrue(df.getColumns().contains("开盘"));
        assertTrue(df.getColumns().contains("收盘"));
        assertTrue(df.getColumns().contains("最高"));
        assertTrue(df.getColumns().contains("最低"));
    }

    @Test
    void testStockZhAHistWeekly() {
        DataFrame df = stockClient.stockZhAHist("000001", "weekly", "20230101", "20240301", "qfq");
        assertNotNull(df);
        System.out.println("Weekly data for 000001:");
        df.printInfo();
    }

    @Test
    void testStockZhAHistMonthly() {
        DataFrame df = stockClient.stockZhAHist("000001", "monthly", "20200101", "20240301", "qfq");
        assertNotNull(df);
        System.out.println("Monthly data for 000001:");
        df.printInfo();
    }

    @Test
    void testStockZhAHistMinEm() {
        DataFrame df = stockClient.stockZhAHistMinEm("000001", 5, "qfq");
        assertNotNull(df);
        System.out.println("5-minute data for 000001:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(5).print();
        }
    }

    @Test
    void testInvalidStockCode() {
        assertThrows(com.akshare.core.exception.AkShareValidationException.class, () -> {
            stockClient.stockZhAHist("invalid", "daily", "20240101", "20240301", "qfq");
        });
    }

    @Test
    void testCache() {
        // First call
        long start1 = System.currentTimeMillis();
        DataFrame df1 = stockClient.stockZhASpotEm();
        long time1 = System.currentTimeMillis() - start1;

        // Second call (should be cached)
        long start2 = System.currentTimeMillis();
        DataFrame df2 = stockClient.stockZhASpotEm();
        long time2 = System.currentTimeMillis() - start2;

        System.out.println("First call time: " + time1 + "ms");
        System.out.println("Second call time (cached): " + time2 + "ms");

        // Cached call should be much faster
        assertTrue(time2 < time1, "Cached call should be faster");
    }
}
