package com.akshare.core.dataframe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Row class representing a single row in DataFrame
 * Supports dynamic columns with type conversion
 */
public class Row {

    private final Map<String, Object> data;

    public Row() {
        this.data = new HashMap<>();
    }

    public Row(Map<String, Object> data) {
        this.data = new HashMap<>(data);
    }

    /**
     * Get value by column name
     *
     * @param columnName column name
     * @return value
     */
    public Object get(String columnName) {
        return data.get(columnName);
    }

    /**
     * Get value as String
     *
     * @param columnName column name
     * @return string value
     */
    public String getString(String columnName) {
        Object value = data.get(columnName);
        return value == null ? null : value.toString();
    }

    /**
     * Get value as Integer
     *
     * @param columnName column name
     * @return integer value
     */
    public Integer getInt(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Long
     *
     * @param columnName column name
     * @return long value
     */
    public Long getLong(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Double
     *
     * @param columnName column name
     * @return double value
     */
    public Double getDouble(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as Boolean
     *
     * @param columnName column name
     * @return boolean value
     */
    public Boolean getBoolean(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Get value as LocalDate
     *
     * @param columnName column name
     * @return LocalDate value
     */
    public LocalDate getDate(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        // Try to parse from string
        return null; // Date parsing handled by DateUtils
    }

    /**
     * Get value as LocalDateTime
     *
     * @param columnName column name
     * @return LocalDateTime value
     */
    public LocalDateTime getDateTime(String columnName) {
        Object value = data.get(columnName);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    /**
     * Set value
     *
     * @param columnName column name
     * @param value value to set
     */
    public void set(String columnName, Object value) {
        data.put(columnName, value);
    }

    /**
     * Check if column exists
     *
     * @param columnName column name
     * @return true if exists
     */
    public boolean hasColumn(String columnName) {
        return data.containsKey(columnName);
    }

    /**
     * Get all column names
     *
     * @return set of column names
     */
    public Set<String> getColumnNames() {
        return data.keySet();
    }

    /**
     * Convert to map
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        return new HashMap<>(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
