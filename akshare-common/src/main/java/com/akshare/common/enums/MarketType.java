package com.akshare.common.enums;

import lombok.Getter;

/**
 * Market type for stocks
 */
@Getter
public enum MarketType {

    MAIN_BOARD("main", "主板", "主板"),
    SME_BOARD("sme", "中小板", "中小板"),
    CHINEXT("chinext", "创业板", "创业板"),
    STAR_MARKET("star", "科创板", "科创板"),
    BSE("bse", "北交所", "北交所"),
    B_SHARE("bshare", "B股", "B股");

    private final String code;
    private final String name;
    private final String description;

    MarketType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static MarketType fromCode(String code) {
        for (MarketType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public static MarketType fromStockCode(String stockCode) {
        if (stockCode == null || stockCode.length() < 3) {
            return null;
        }

        String prefix = stockCode.substring(0, 3);

        // STAR Market
        if (prefix.startsWith("688") || prefix.startsWith("689")) {
            return STAR_MARKET;
        }

        // ChiNext
        if (prefix.startsWith("300") || prefix.startsWith("301")) {
            return CHINEXT;
        }

        // SME Board
        if (prefix.startsWith("002") || prefix.startsWith("003")) {
            return SME_BOARD;
        }

        // Main Board
        if (prefix.startsWith("600") || prefix.startsWith("601") ||
            prefix.startsWith("603") || prefix.startsWith("605") ||
            prefix.startsWith("000") || prefix.startsWith("001")) {
            return MAIN_BOARD;
        }

        // BSE
        String prefix2 = stockCode.substring(0, 2);
        if (prefix2.equals("43") || prefix2.equals("83") ||
            prefix2.equals("87") || prefix2.equals("88") || prefix2.equals("89")) {
            return BSE;
        }

        return null;
    }
}
