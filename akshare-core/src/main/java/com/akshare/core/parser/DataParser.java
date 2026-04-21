package com.akshare.core.parser;

import com.akshare.core.dataframe.DataFrame;

/**
 * Data Parser interface
 * Defines methods for parsing different data formats
 */
public interface DataParser {

    /**
     * Parse data to DataFrame
     *
     * @param data raw data string
     * @return DataFrame
     */
    DataFrame parse(String data);

    /**
     * Check if parser supports this data format
     *
     * @param data raw data string
     * @return true if supported
     */
    boolean supports(String data);
}
