package com.akshare.core.util;

import com.akshare.core.exception.AkShareValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Stock code utility class
 * Provides stock code validation and conversion utilities
 */
@Slf4j
public class StockCodeUtils {

    // Shanghai Stock Exchange prefixes
    private static final Set<String> SH_PREFIXES = new HashSet<>(Arrays.asList(
            "600", "601", "603", "605", "688", "689"
    ));

    // Shenzhen Stock Exchange prefixes
    private static final Set<String> SZ_PREFIXES = new HashSet<>(Arrays.asList(
            "000", "001", "002", "003", "300", "301"
    ));

    // Beijing Stock Exchange prefixes
    private static final Set<String> BJ_PREFIXES = new HashSet<>(Arrays.asList(
            "430", "830", "87", "88", "89"
    ));

    // Pattern for 6-digit stock code
    private static final Pattern STOCK_CODE_PATTERN = Pattern.compile("^\\d{6}$");

    /**
     * Validate A-share stock code
     *
     * @param stockCode stock code
     * @return true if valid
     */
    public static boolean isValidStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return false;
        }

        String code = stockCode.trim();
        if (!STOCK_CODE_PATTERN.matcher(code).matches()) {
            return false;
        }

        String prefix = code.substring(0, 3);
        return SH_PREFIXES.contains(prefix) || SZ_PREFIXES.contains(prefix) ||
               BJ_PREFIXES.contains(code.substring(0, 2)) ||
               BJ_PREFIXES.contains(prefix);
    }

    /**
     * Validate and throw exception if invalid
     *
     * @param stockCode stock code
     * @throws AkShareValidationException if invalid
     */
    public static void validateStockCode(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            throw new AkShareValidationException("stockCode", stockCode,
                    "Invalid A-share stock code. Must be 6 digits.");
        }
    }

    /**
     * Get exchange code for stock
     *
     * @param stockCode stock code
     * @return exchange code (sh, sz, bj)
     */
    public static String getExchange(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            return null;
        }

        String prefix = stockCode.substring(0, 3);
        String prefix2 = stockCode.substring(0, 2);

        if (SH_PREFIXES.contains(prefix)) {
            return "sh";
        } else if (SZ_PREFIXES.contains(prefix)) {
            return "sz";
        } else if (BJ_PREFIXES.contains(prefix) || BJ_PREFIXES.contains(prefix2)) {
            return "bj";
        }

        return null;
    }

    /**
     * Get full stock code with exchange prefix
     *
     * @param stockCode stock code
     * @return full code (e.g., sh600000, sz000001)
     */
    public static String getFullCode(String stockCode) {
        String exchange = getExchange(stockCode);
        if (exchange == null) {
            return null;
        }
        return exchange + stockCode;
    }

    /**
     * Check if stock is from Shanghai exchange
     *
     * @param stockCode stock code
     * @return true if Shanghai
     */
    public static boolean isShanghai(String stockCode) {
        return "sh".equals(getExchange(stockCode));
    }

    /**
     * Check if stock is from Shenzhen exchange
     *
     * @param stockCode stock code
     * @return true if Shenzhen
     */
    public static boolean isShenzhen(String stockCode) {
        return "sz".equals(getExchange(stockCode));
    }

    /**
     * Check if stock is from Beijing exchange
     *
     * @param stockCode stock code
     * @return true if Beijing
     */
    public static boolean isBeijing(String stockCode) {
        return "bj".equals(getExchange(stockCode));
    }

    /**
     * Check if stock is a main board stock
     *
     * @param stockCode stock code
     * @return true if main board
     */
    public static boolean isMainBoard(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            return false;
        }
        String prefix = stockCode.substring(0, 3);
        return prefix.startsWith("600") || prefix.startsWith("601") ||
               prefix.startsWith("603") || prefix.startsWith("605") ||
               prefix.startsWith("000") || prefix.startsWith("001");
    }

    /**
     * Check if stock is a SME (Small and Medium Enterprise) board stock
     *
     * @param stockCode stock code
     * @return true if SME board
     */
    public static boolean isSMEBoard(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            return false;
        }
        String prefix = stockCode.substring(0, 3);
        return prefix.startsWith("002") || prefix.startsWith("003");
    }

    /**
     * Check if stock is a ChiNext (Growth Enterprise Market) stock
     *
     * @param stockCode stock code
     * @return true if ChiNext
     */
    public static boolean isChiNext(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            return false;
        }
        String prefix = stockCode.substring(0, 3);
        return prefix.startsWith("300") || prefix.startsWith("301");
    }

    /**
     * Check if stock is a STAR Market (Sci-Tech Innovation Board) stock
     *
     * @param stockCode stock code
     * @return true if STAR Market
     */
    public static boolean isSTARMarket(String stockCode) {
        if (!isValidStockCode(stockCode)) {
            return false;
        }
        String prefix = stockCode.substring(0, 3);
        return prefix.startsWith("688") || prefix.startsWith("689");
    }
}
