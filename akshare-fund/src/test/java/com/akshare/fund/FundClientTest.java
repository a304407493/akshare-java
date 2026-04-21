package com.akshare.fund;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.fund.client.FundClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FundClient
 */
public class FundClientTest {

    private FundClient fundClient;

    @BeforeEach
    void setUp() {
        fundClient = new FundClient();
        fundClient.setTimeout(30000);
    }

    @Test
    void testFundEmOpenFundDaily() {
        DataFrame df = fundClient.fundEmOpenFundDaily();
        assertNotNull(df);
        System.out.println("Open fund daily data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(5).print();
        }
    }

    @Test
    void testFundEmFundName() {
        DataFrame df = fundClient.fundEmFundName();
        assertNotNull(df);
        assertTrue(df.rowCount() > 0, "Should have fund name data");

        System.out.println("Fund name list:");
        df.printInfo();
        df.head(10).print();

        // Verify columns
        assertTrue(df.getColumns().contains("基金代码"));
        assertTrue(df.getColumns().contains("基金简称"));
    }

    @Test
    void testFundEtfSpotEm() {
        DataFrame df = fundClient.fundEtfSpotEm();
        assertNotNull(df);
        System.out.println("ETF spot data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(5).print();
        }
    }
}
