package com.akshare.common.enums;

import lombok.Getter;

/**
 * Stock exchange type
 */
@Getter
public enum ExchangeType {

    SHANGHAI("sh", "上海证券交易所", "XSHG", "1"),
    SHENZHEN("sz", "深圳证券交易所", "XSHE", "0"),
    BEIJING("bj", "北京证券交易所", "XBSE", "2"),
    HONG_KONG("hk", "香港交易所", "XHKG", "116"),
    NEW_YORK("ny", "纽约证券交易所", "XNYS", ""),
    NASDAQ("nq", "纳斯达克", "XNAS", "");

    private final String code;
    private final String name;
    private final String micCode;
    private final String eastMoneyCode;

    ExchangeType(String code, String name, String micCode, String eastMoneyCode) {
        this.code = code;
        this.name = name;
        this.micCode = micCode;
        this.eastMoneyCode = eastMoneyCode;
    }

    public static ExchangeType fromCode(String code) {
        for (ExchangeType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public static ExchangeType fromEastMoneyCode(String code) {
        for (ExchangeType type : values()) {
            if (type.eastMoneyCode.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static ExchangeType fromStockCode(String stockCode) {
        if (stockCode == null || stockCode.length() < 3) {
            return null;
        }

        String prefix = stockCode.substring(0, 3);

        // Shanghai
        if (prefix.startsWith("600") || prefix.startsWith("601") ||
            prefix.startsWith("603") || prefix.startsWith("605") ||
            prefix.startsWith("688") || prefix.startsWith("689")) {
            return SHANGHAI;
        }

        // Shenzhen
        if (prefix.startsWith("000") || prefix.startsWith("001") ||
            prefix.startsWith("002") || prefix.startsWith("003") ||
            prefix.startsWith("300") || prefix.startsWith("301")) {
            return SHENZHEN;
        }

        // Beijing
        String prefix2 = stockCode.substring(0, 2);
        if (prefix2.equals("43") || prefix2.equals("83") ||
            prefix2.equals("87") || prefix2.equals("88") || prefix2.equals("89")) {
            return BEIJING;
        }

        return null;
    }
}
