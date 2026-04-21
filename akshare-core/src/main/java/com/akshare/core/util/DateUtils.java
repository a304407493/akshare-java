package com.akshare.core.util;

import com.akshare.core.exception.AkShareValidationException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date utility class for AKShare
 * Provides date parsing and formatting utilities
 */
@Slf4j
public class DateUtils {

    private static final Map<String, DateTimeFormatter> FORMATTERS = new HashMap<>();

    static {
        // Common date formats
        FORMATTERS.put("yyyyMMdd", DateTimeFormatter.ofPattern("yyyyMMdd"));
        FORMATTERS.put("yyyy-MM-dd", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        FORMATTERS.put("yyyy/MM/dd", DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        FORMATTERS.put("dd/MM/yyyy", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        FORMATTERS.put("MM/dd/yyyy", DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        FORMATTERS.put("yyyy年MM月dd日", DateTimeFormatter.ofPattern("yyyy年MM月dd日"));

        // DateTime formats
        FORMATTERS.put("yyyyMMddHHmmss", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        FORMATTERS.put("yyyy-MM-dd HH:mm:ss", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        FORMATTERS.put("yyyy/MM/dd HH:mm:ss", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }

    /**
     * Parse date string to LocalDate
     *
     * @param dateStr date string
     * @param pattern date pattern
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = FORMATTERS.get(pattern);
            if (formatter == null) {
                formatter = DateTimeFormatter.ofPattern(pattern);
            }
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse date: {} with pattern: {}", dateStr, pattern);
            throw new AkShareValidationException("date", dateStr, "Invalid date format. Expected: " + pattern);
        }
    }

    /**
     * Parse date string with auto-detect format
     *
     * @param dateStr date string
     * @return LocalDate
     */
    public static LocalDate parseDateAuto(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy年MM月dd日"};

        for (String pattern : patterns) {
            try {
                return parseDate(dateStr, pattern);
            } catch (AkShareValidationException ignored) {
                // Try next pattern
            }
        }

        throw new AkShareValidationException("date", dateStr, "Unable to parse date with any known format");
    }

    /**
     * Parse datetime string to LocalDateTime
     *
     * @param dateTimeStr datetime string
     * @param pattern datetime pattern
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = FORMATTERS.get(pattern);
            if (formatter == null) {
                formatter = DateTimeFormatter.ofPattern(pattern);
            }
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse datetime: {} with pattern: {}", dateTimeStr, pattern);
            throw new AkShareValidationException("datetime", dateTimeStr, "Invalid datetime format. Expected: " + pattern);
        }
    }

    /**
     * Format LocalDate to string
     *
     * @param date LocalDate
     * @param pattern output pattern
     * @return formatted string
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }

        DateTimeFormatter formatter = FORMATTERS.get(pattern);
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }
        return date.format(formatter);
    }

    /**
     * Format LocalDateTime to string
     *
     * @param dateTime LocalDateTime
     * @param pattern output pattern
     * @return formatted string
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }

        DateTimeFormatter formatter = FORMATTERS.get(pattern);
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern);
        }
        return dateTime.format(formatter);
    }

    /**
     * Convert date format
     *
     * @param dateStr input date string
     * @param inputPattern input pattern
     * @param outputPattern output pattern
     * @return formatted string
     */
    public static String convertFormat(String dateStr, String inputPattern, String outputPattern) {
        LocalDate date = parseDate(dateStr, inputPattern);
        return format(date, outputPattern);
    }

    /**
     * Get current date in specified format
     *
     * @param pattern date pattern
     * @return formatted current date
     */
    public static String getCurrentDate(String pattern) {
        return format(LocalDate.now(), pattern);
    }

    /**
     * Get current datetime in specified format
     *
     * @param pattern datetime pattern
     * @return formatted current datetime
     */
    public static String getCurrentDateTime(String pattern) {
        return format(LocalDateTime.now(), pattern);
    }

    /**
     * Validate date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return true if valid
     */
    public static boolean isValidRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !startDate.isAfter(endDate);
    }

    /**
     * Validate date string format
     *
     * @param dateStr date string
     * @param pattern expected pattern
     * @return true if valid
     */
    public static boolean isValidFormat(String dateStr, String pattern) {
        try {
            parseDate(dateStr, pattern);
            return true;
        } catch (AkShareValidationException e) {
            return false;
        }
    }
}
