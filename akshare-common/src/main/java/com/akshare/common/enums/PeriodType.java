package com.akshare.common.enums;

import lombok.Getter;

/**
 * Period type for K-line data
 */
@Getter
public enum PeriodType {

    DAILY("daily", "日K", "101", 1),
    WEEKLY("weekly", "周K", "102", 7),
    MONTHLY("monthly", "月K", "103", 30),
    QUARTERLY("quarterly", "季K", "104", 90),
    YEARLY("yearly", "年K", "105", 365);

    private final String code;
    private final String name;
    private final String eastMoneyCode;
    private final int days;

    PeriodType(String code, String name, String eastMoneyCode, int days) {
        this.code = code;
        this.name = name;
        this.eastMoneyCode = eastMoneyCode;
        this.days = days;
    }

    public static PeriodType fromCode(String code) {
        for (PeriodType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return DAILY;
    }

    public static PeriodType fromEastMoneyCode(String code) {
        for (PeriodType type : values()) {
            if (type.eastMoneyCode.equals(code)) {
                return type;
            }
        }
        return DAILY;
    }
}
