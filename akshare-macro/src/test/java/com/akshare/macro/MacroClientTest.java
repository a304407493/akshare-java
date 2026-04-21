package com.akshare.macro;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.macro.client.MacroClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MacroClient
 */
public class MacroClientTest {

    private MacroClient macroClient;

    @BeforeEach
    void setUp() {
        macroClient = new MacroClient();
        macroClient.setTimeout(30000);
    }

    @Test
    void testMacroChinaGdp() {
        DataFrame df = macroClient.macroChinaGdp();
        assertNotNull(df);
        System.out.println("China GDP data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }

    @Test
    void testMacroChinaCpi() {
        DataFrame df = macroClient.macroChinaCpi();
        assertNotNull(df);
        System.out.println("China CPI data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }

    @Test
    void testMacroChinaPpi() {
        DataFrame df = macroClient.macroChinaPpi();
        assertNotNull(df);
        System.out.println("China PPI data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }

    @Test
    void testMacroChinaPmi() {
        DataFrame df = macroClient.macroChinaPmi();
        assertNotNull(df);
        System.out.println("China PMI data:");
        df.printInfo();
        if (df.rowCount() > 0) {
            df.head(10).print();
        }
    }
}
