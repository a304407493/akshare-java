package com.akshare.core.parser;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.dataframe.Row;
import com.akshare.core.exception.AkShareParseException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV Parser implementation
 * Parses CSV data to DataFrame
 */
@Slf4j
public class CsvParser implements DataParser {

    private static final char DEFAULT_DELIMITER = ',';
    private static final char DEFAULT_QUOTE = '"';

    private final char delimiter;
    private final char quote;
    private final boolean hasHeader;

    public CsvParser() {
        this(DEFAULT_DELIMITER, DEFAULT_QUOTE, true);
    }

    public CsvParser(char delimiter) {
        this(delimiter, DEFAULT_QUOTE, true);
    }

    public CsvParser(char delimiter, char quote, boolean hasHeader) {
        this.delimiter = delimiter;
        this.quote = quote;
        this.hasHeader = hasHeader;
    }

    @Override
    public DataFrame parse(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
            return parseReader(reader);
        } catch (IOException e) {
            log.error("Failed to parse CSV", e);
            throw new AkShareParseException("CSV", data, "Invalid CSV format", e);
        }
    }

    private DataFrame parseReader(BufferedReader reader) throws IOException {
        List<String> headers = new ArrayList<>();
        List<Row> rows = new ArrayList<>();

        String line;
        boolean isFirstLine = true;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            List<String> values = parseLine(line);

            if (isFirstLine) {
                isFirstLine = false;
                if (hasHeader) {
                    headers = values;
                    continue;
                } else {
                    // Generate column names
                    for (int i = 0; i < values.size(); i++) {
                        headers.add("Column" + (i + 1));
                    }
                }
            }

            Map<String, Object> rowData = new HashMap<>();
            for (int i = 0; i < values.size() && i < headers.size(); i++) {
                rowData.put(headers.get(i), parseValue(values.get(i)));
            }
            rows.add(new Row(rowData));
        }

        return new DataFrameImpl(headers, rows);
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == quote) {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == quote) {
                    // Escaped quote
                    currentValue.append(quote);
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // Add last value
        values.add(currentValue.toString().trim());

        return values;
    }

    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Try to parse as integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }

        // Try to parse as long
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }

        // Try to parse as double
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException ignored) {
        }

        // Return as string
        return value;
    }

    @Override
    public boolean supports(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }

        // Check if it looks like CSV
        String[] lines = data.split("\n");
        if (lines.length == 0) {
            return false;
        }

        // Check first line for comma-separated values
        String firstLine = lines[0];
        return firstLine.contains(",") && !firstLine.contains("{") && !firstLine.contains("<");
    }
}
