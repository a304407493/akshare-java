package com.akshare.core.dataframe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of DataFrame interface
 */
@Slf4j
public class DataFrameImpl implements DataFrame {

    private final List<String> columns;
    private final List<Row> rows;
    private final Map<String, Class<?>> columnTypes;
    private final ObjectMapper objectMapper;

    public DataFrameImpl() {
        this.columns = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.columnTypes = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public DataFrameImpl(List<String> columns, List<Row> rows) {
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
        this.columnTypes = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        inferColumnTypes();
    }

    public DataFrameImpl(List<String> columns, List<Row> rows, Map<String, Class<?>> columnTypes) {
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
        this.columnTypes = new HashMap<>(columnTypes);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Create DataFrame from list of maps
     */
    public static DataFrame fromList(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return new DataFrameImpl();
        }

        // Extract columns from first row
        Set<String> columnSet = data.get(0).keySet();
        List<String> columns = new ArrayList<>(columnSet);

        // Create rows
        List<Row> rows = data.stream()
                .map(Row::new)
                .collect(Collectors.toList());

        return new DataFrameImpl(columns, rows);
    }

    /**
     * Create empty DataFrame with specified columns
     */
    public static DataFrame empty(List<String> columns) {
        return new DataFrameImpl(columns, new ArrayList<>());
    }

    private void inferColumnTypes() {
        for (String column : columns) {
            columnTypes.put(column, inferType(column));
        }
    }

    private Class<?> inferType(String column) {
        for (Row row : rows) {
            Object value = row.get(column);
            if (value != null) {
                return value.getClass();
            }
        }
        return String.class;
    }

    @Override
    public DataFrame select(String... columns) {
        List<String> selectedColumns = Arrays.asList(columns);
        List<Row> selectedRows = rows.stream()
                .map(row -> {
                    Map<String, Object> newData = new HashMap<>();
                    for (String col : selectedColumns) {
                        if (row.hasColumn(col)) {
                            newData.put(col, row.get(col));
                        }
                    }
                    return new Row(newData);
                })
                .collect(Collectors.toList());

        Map<String, Class<?>> newTypes = new HashMap<>();
        for (String col : selectedColumns) {
            if (columnTypes.containsKey(col)) {
                newTypes.put(col, columnTypes.get(col));
            }
        }

        return new DataFrameImpl(selectedColumns, selectedRows, newTypes);
    }

    @Override
    public DataFrame filter(Predicate<Row> condition) {
        List<Row> filteredRows = rows.stream()
                .filter(condition)
                .collect(Collectors.toList());
        return new DataFrameImpl(columns, filteredRows, columnTypes);
    }

    @Override
    public DataFrame head(int n) {
        if (n <= 0) return new DataFrameImpl();
        List<Row> headRows = rows.stream()
                .limit(n)
                .collect(Collectors.toList());
        return new DataFrameImpl(columns, headRows, columnTypes);
    }

    @Override
    public DataFrame tail(int n) {
        if (n <= 0) return new DataFrameImpl();
        int start = Math.max(0, rows.size() - n);
        List<Row> tailRows = rows.subList(start, rows.size());
        return new DataFrameImpl(columns, tailRows, columnTypes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataFrame sort(String column, boolean ascending) {
        List<Row> sortedRows = new ArrayList<>(rows);
        Comparator<Row> comparator = (r1, r2) -> {
            Object v1 = r1.get(column);
            Object v2 = r2.get(column);

            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return ascending ? -1 : 1;
            if (v2 == null) return ascending ? 1 : -1;

            int result;
            if (v1 instanceof Comparable && v2 instanceof Comparable) {
                result = ((Comparable<Object>) v1).compareTo(v2);
            } else {
                result = v1.toString().compareTo(v2.toString());
            }
            return ascending ? result : -result;
        };
        sortedRows.sort(comparator);
        return new DataFrameImpl(columns, sortedRows, columnTypes);
    }

    @Override
    public DataFrame dropNull() {
        List<Row> nonNullRows = rows.stream()
                .filter(row -> columns.stream().allMatch(col -> row.get(col) != null))
                .collect(Collectors.toList());
        return new DataFrameImpl(columns, nonNullRows, columnTypes);
    }

    @Override
    public DataFrame drop(String... columnsToDrop) {
        Set<String> dropSet = new HashSet<>(Arrays.asList(columnsToDrop));
        List<String> newColumns = columns.stream()
                .filter(col -> !dropSet.contains(col))
                .collect(Collectors.toList());

        List<Row> newRows = rows.stream()
                .map(row -> {
                    Map<String, Object> newData = new HashMap<>();
                    for (String col : newColumns) {
                        if (row.hasColumn(col)) {
                            newData.put(col, row.get(col));
                        }
                    }
                    return new Row(newData);
                })
                .collect(Collectors.toList());

        Map<String, Class<?>> newTypes = new HashMap<>();
        for (String col : newColumns) {
            if (columnTypes.containsKey(col)) {
                newTypes.put(col, columnTypes.get(col));
            }
        }

        return new DataFrameImpl(newColumns, newRows, newTypes);
    }

    @Override
    public List<String> getColumns() {
        return new ArrayList<>(columns);
    }

    @Override
    public List<Row> getRows() {
        return new ArrayList<>(rows);
    }

    @Override
    public Object get(int rowIndex, String columnName) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
        return rows.get(rowIndex).get(columnName);
    }

    @Override
    public List<Object> getColumn(String columnName) {
        return rows.stream()
                .map(row -> row.get(columnName))
                .collect(Collectors.toList());
    }

    @Override
    public Row getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
        return rows.get(rowIndex);
    }

    @Override
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(toList());
        } catch (JsonProcessingException e) {
            log.error("Failed to convert DataFrame to JSON", e);
            return "[]";
        }
    }

    @Override
    public String toCsv() {
        if (columns.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(String.join(",", columns)).append("\n");

        // Data rows
        for (Row row : rows) {
            List<String> values = new ArrayList<>();
            for (String col : columns) {
                Object value = row.get(col);
                values.add(value == null ? "" : value.toString());
            }
            sb.append(String.join(",", values)).append("\n");
        }

        return sb.toString();
    }

    @Override
    public List<Map<String, Object>> toList() {
        return rows.stream()
                .map(Row::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public Object[][] toArray() {
        Object[][] array = new Object[rows.size()][columns.size()];
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            for (int j = 0; j < columns.size(); j++) {
                array[i][j] = row.get(columns.get(j));
            }
        }
        return array;
    }

    @Override
    public int rowCount() {
        return rows.size();
    }

    @Override
    public int columnCount() {
        return columns.size();
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public Class<?> getColumnType(String columnName) {
        return columnTypes.get(columnName);
    }

    @Override
    public void printInfo() {
        System.out.println("DataFrame Info:");
        System.out.println("  Rows: " + rowCount());
        System.out.println("  Columns: " + columnCount());
        System.out.println("  Column Names: " + String.join(", ", columns));
    }

    @Override
    public void print() {
        if (columns.isEmpty()) {
            System.out.println("Empty DataFrame");
            return;
        }

        // Print header
        System.out.println(String.join(" | ", columns));
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < columns.size() * 20; i++) {
            separator.append("-");
        }
        System.out.println(separator.toString());

        // Print rows (limit to 10)
        int limit = Math.min(rows.size(), 10);
        for (int i = 0; i < limit; i++) {
            Row row = rows.get(i);
            List<String> values = new ArrayList<>();
            for (String col : columns) {
                Object value = row.get(col);
                values.add(value == null ? "null" : value.toString());
            }
            System.out.println(String.join(" | ", values));
        }

        if (rows.size() > 10) {
            System.out.println("... (" + (rows.size() - 10) + " more rows)");
        }
    }

    /**
     * Add a row to the DataFrame
     */
    public void addRow(Row row) {
        rows.add(row);
    }

    /**
     * Add a column
     */
    public void addColumn(String columnName, List<Object> values) {
        if (!columns.contains(columnName)) {
            columns.add(columnName);
        }
        for (int i = 0; i < values.size() && i < rows.size(); i++) {
            rows.get(i).set(columnName, values.get(i));
        }
    }
}
