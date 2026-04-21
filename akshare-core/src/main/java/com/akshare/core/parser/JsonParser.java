package com.akshare.core.parser;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.dataframe.Row;
import com.akshare.core.exception.AkShareParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * JSON Parser implementation
 * Parses JSON data to DataFrame
 */
@Slf4j
public class JsonParser implements DataParser {

    private final ObjectMapper objectMapper;

    public JsonParser() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public DataFrame parse(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(data);
            return parseJsonNode(rootNode);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON", e);
            throw new AkShareParseException("JSON", data, "Invalid JSON format", e);
        }
    }

    /**
     * Parse JSON from a specific path
     *
     * @param data raw JSON string
     * @param path JSON path to extract data from (e.g., "data.result")
     * @return DataFrame
     */
    public DataFrame parse(String data, String path) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(data);
            JsonNode targetNode = getNodeAtPath(rootNode, path);
            return parseJsonNode(targetNode);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON", e);
            throw new AkShareParseException("JSON", data, "Invalid JSON format", e);
        }
    }

    /**
     * Parse JSON array with column mapping
     *
     * @param data raw JSON string
     * @param columnMapping map of JSON field names to column names
     * @return DataFrame
     */
    public DataFrame parse(String data, Map<String, String> columnMapping) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            JsonNode rootNode = objectMapper.readTree(data);
            return parseJsonNodeWithMapping(rootNode, columnMapping);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON", e);
            throw new AkShareParseException("JSON", data, "Invalid JSON format", e);
        }
    }

    private DataFrame parseJsonNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return new DataFrameImpl();
        }

        if (node.isArray()) {
            return parseJsonArray(node);
        } else if (node.isObject()) {
            // Single object - wrap in list
            List<Map<String, Object>> list = new ArrayList<>();
            list.add(convertObjectNodeToMap(node));
            return DataFrameImpl.fromList(list);
        }

        return new DataFrameImpl();
    }

    private DataFrame parseJsonArray(JsonNode arrayNode) {
        if (!arrayNode.isArray() || arrayNode.isEmpty()) {
            return new DataFrameImpl();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> allColumns = new LinkedHashSet<>();

        // First pass: collect all column names
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                Iterator<String> fieldNames = item.fieldNames();
                while (fieldNames.hasNext()) {
                    allColumns.add(fieldNames.next());
                }
            }
        }

        // Second pass: create rows
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                Map<String, Object> row = new HashMap<>();
                for (String column : allColumns) {
                    JsonNode valueNode = item.get(column);
                    row.put(column, convertJsonNodeToValue(valueNode));
                }
                rows.add(row);
            }
        }

        return DataFrameImpl.fromList(rows);
    }

    private DataFrame parseJsonNodeWithMapping(JsonNode node, Map<String, String> columnMapping) {
        if (node == null || node.isNull()) {
            return new DataFrameImpl();
        }

        if (node.isArray()) {
            List<Map<String, Object>> rows = new ArrayList<>();

            for (JsonNode item : node) {
                if (item.isObject()) {
                    Map<String, Object> row = new HashMap<>();
                    for (Map.Entry<String, String> mapping : columnMapping.entrySet()) {
                        JsonNode valueNode = item.get(mapping.getKey());
                        row.put(mapping.getValue(), convertJsonNodeToValue(valueNode));
                    }
                    rows.add(row);
                }
            }

            return DataFrameImpl.fromList(rows);
        }

        return new DataFrameImpl();
    }

    private JsonNode getNodeAtPath(JsonNode rootNode, String path) {
        if (path == null || path.isEmpty()) {
            return rootNode;
        }

        String[] parts = path.split("\\.");
        JsonNode current = rootNode;

        for (String part : parts) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(part);
        }

        return current;
    }

    private Map<String, Object> convertObjectNodeToMap(JsonNode node) {
        Map<String, Object> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            map.put(entry.getKey(), convertJsonNodeToValue(entry.getValue()));
        }

        return map;
    }

    private Object convertJsonNodeToValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isInt()) {
            return node.asInt();
        }
        if (node.isLong()) {
            return node.asLong();
        }
        if (node.isDouble() || node.isFloat()) {
            return node.asDouble();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(convertJsonNodeToValue(item));
            }
            return list;
        }
        if (node.isObject()) {
            return convertObjectNodeToMap(node);
        }
        return node.toString();
    }

    @Override
    public boolean supports(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        String trimmed = data.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
