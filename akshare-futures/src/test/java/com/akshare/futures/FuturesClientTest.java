package com.akshare.futures;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.futures.client.FuturesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FuturesClient
 */
public class FuturesClientTest {

    private FuturesClient futuresClient;

    @BeforeEach
    void setUp() {
        futuresClient = new FuturesClient();
        futuresClient.setTimeout(30000);
    }

    @Test
    void testFuturesZhSpot() {
        DataFrame df = futuresClient.futuresZhSpot();
        assertNotNull(df);
        System.out.println("Futures spot data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }

    @Test
    void testFuturesZhDaily() {
        DataFrame df = futuresClient.futuresZhDaily("RB0");
        assertNotNull(df);
        System.out.println("Futures daily data for RB0:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(5).print();
        }
    }
}
