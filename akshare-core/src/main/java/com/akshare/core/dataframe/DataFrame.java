package com.akshare.core.dataframe;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * DataFrame interface - Java implementation similar to pandas.DataFrame
 * Provides data manipulation and analysis capabilities
 */
public interface DataFrame {

    // ==================== Column Operations ====================

    /**
     * Select specific columns
     *
     * @param columns column names to select
     * @return new DataFrame with selected columns
     */
    DataFrame select(String... columns);

    /**
     * Filter rows based on condition
     *
     * @param condition predicate to filter rows
     * @return new DataFrame with filtered rows
     */
    DataFrame filter(Predicate<Row> condition);

    /**
     * Get first n rows
     *
     * @param n number of rows
     * @return new DataFrame with first n rows
     */
    DataFrame head(int n);

    /**
     * Get last n rows
     *
     * @param n number of rows
     * @return new DataFrame with last n rows
     */
    DataFrame tail(int n);

    /**
     * Sort by column
     *
     * @param column column name to sort by
     * @param ascending true for ascending, false for descending
     * @return new sorted DataFrame
     */
    DataFrame sort(String column, boolean ascending);

    /**
     * Drop rows with null values
     *
     * @return new DataFrame without null rows
     */
    DataFrame dropNull();

    /**
     * Drop specific columns
     *
     * @param columns column names to drop
     * @return new DataFrame without dropped columns
     */
    DataFrame drop(String... columns);

    // ==================== Data Access ====================

    /**
     * Get all column names
     *
     * @return list of column names
     */
    List<String> getColumns();

    /**
     * Get all rows
     *
     * @return list of rows
     */
    List<Row> getRows();

    /**
     * Get value at specific position
     *
     * @param rowIndex row index (0-based)
     * @param columnName column name
     * @return value at the position
     */
    Object get(int rowIndex, String columnName);

    /**
     * Get column values
     *
     * @param columnName column name
     * @return list of column values
     */
    List<Object> getColumn(String columnName);

    /**
     * Get row at index
     *
     * @param rowIndex row index (0-based)
     * @return row object
     */
    Row getRow(int rowIndex);

    // ==================== Data Export ====================

    /**
     * Convert to JSON string
     *
     * @return JSON representation
     */
    String toJson();

    /**
     * Convert to CSV string
     *
     * @return CSV representation
     */
    String toCsv();

    /**
     * Convert to list of maps
     *
     * @return list of maps where each map represents a row
     */
    List<Map<String, Object>> toList();

    /**
     * Convert to array of arrays
     *
     * @return 2D array representation
     */
    Object[][] toArray();

    // ==================== Metadata ====================

    /**
     * Get number of rows
     *
     * @return row count
     */
    int rowCount();

    /**
     * Get number of columns
     *
     * @return column count
     */
    int columnCount();

    /**
     * Check if DataFrame is empty
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Get column type
     *
     * @param columnName column name
     * @return class type of the column
     */
    Class<?> getColumnType(String columnName);

    /**
     * Print DataFrame info
     */
    void printInfo();

    /**
     * Print DataFrame content
     */
    void print();
}
