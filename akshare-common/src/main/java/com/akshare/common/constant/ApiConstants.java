package com.akshare.common.constant;

/**
 * API Constants for AKShare Java
 * Contains URLs, endpoints, and other API-related constants
 */
public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    // ==================== East Money (东方财富) ====================
    public static final String EASTMONEY_BASE_URL = "https://push2.eastmoney.com";
    public static final String EASTMONEY_API_URL = "https://push2.eastmoney.com/api/qt";
    public static final String EASTMONEY_STOCK_URL = "https://push2.eastmoney.com/api/qt/stock";
    public static final String EASTMONEY_KLINE_URL = "https://push2.eastmoney.com/api/qt/stock/kline";
    public static final String EASTMONEY_LIST_URL = "https://push2.eastmoney.com/api/qt/clist/get";
    public static final String EASTMONEY_FINANCE_URL = "https://datacenter.eastmoney.com";

    // ==================== Sina Finance (新浪财经) ====================
    public static final String SINA_BASE_URL = "https://hq.sinajs.cn";
    public static final String SINA_LIST_URL = "https://hq.sinajs.cn/list=";
    public static final String SINA_KLINE_URL = "https://quotes.sina.cn/cn/api/quotes.php";

    // ==================== 10jqka (同花顺) ====================
    public static final String THS_BASE_URL = "https://basic.10jqka.com.cn";
    public static final String THS_API_URL = "https://basic.10jqka.com.cn/api/stock";
    public static final String THS_FINANCE_URL = "https://basic.10jqka.com.cn/api/stock/finance";

    // ==================== Fund (基金) ====================
    public static final String FUND_EASTMONEY_URL = "https://fund.eastmoney.com";
    public static final String FUND_API_URL = "https://fund.eastmoney.com/data";
    public static final String FUND_DAILY_URL = "https://fund.eastmoney.com/data/rankhandler.aspx";

    // ==================== Futures (期货) ====================
    public static final String FUTURES_SINA_URL = "https://hq.sinajs.cn/list=";
    public static final String FUTURES_DCE_URL = "http://www.dce.com.cn";
    public static final String FUTURES_SHFE_URL = "https://www.shfe.com.cn";
    public static final String FUTURES_CZCE_URL = "http://www.czce.com.cn";

    // ==================== Macro (宏观) ====================
    public static final String MACRO_EASTMONEY_URL = "https://datainterface.eastmoney.com";
    public static final String MACRO_CHINA_URL = "https://datainterface.eastmoney.com/EM_DataCenter/JS.aspx";

    // ==================== Bond (债券) ====================
    public static final String BOND_EASTMONEY_URL = "https://datacenter.eastmoney.com";
    public static final String BOND_CONVERTIBLE_URL = "https://datacenter.eastmoney.com/api/data/v1/get";

    // ==================== Default Values ====================
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyyMMddHHmmss";
    public static final int DEFAULT_TIMEOUT_MS = 30000;
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;
    public static final long DEFAULT_REQUEST_INTERVAL_MS = 1000;
}
