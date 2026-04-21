package com.akshare.bond;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.bond.client.BondClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BondClient
 */
public class BondClientTest {

    private BondClient bondClient;

    @BeforeEach
    void setUp() {
        bondClient = new BondClient();
        bondClient.setTimeout(30000);
    }

    @Test
    void testBondZhCov() {
        DataFrame df = bondClient.bondZhCov();
        assertNotNull(df);
        System.out.println("Convertible bond data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }
}
