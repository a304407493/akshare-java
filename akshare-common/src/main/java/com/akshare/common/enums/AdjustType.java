package com.akshare.common.enums;

import lombok.Getter;

/**
 * Adjust type for stock price data
 */
@Getter
public enum AdjustType {

    NONE("none", "不复权", ""),
    QFQ("qfq", "前复权", "1"),
    HFQ("hfq", "后复权", "2");

    private final String code;
    private final String name;
    private final String eastMoneyCode;

    AdjustType(String code, String name, String eastMoneyCode) {
        this.code = code;
        this.name = name;
        this.eastMoneyCode = eastMoneyCode;
    }

    public static AdjustType fromCode(String code) {
        for (AdjustType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return NONE;
    }

    public static AdjustType fromEastMoneyCode(String code) {
        for (AdjustType type : values()) {
            if (type.eastMoneyCode.equals(code)) {
                return type;
            }
        }
        return NONE;
    }
}
