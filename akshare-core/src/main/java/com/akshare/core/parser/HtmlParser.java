package com.akshare.core.parser;

import com.akshare.core.dataframe.DataFrame;
import com.akshare.core.dataframe.DataFrameImpl;
import com.akshare.core.dataframe.Row;
import com.akshare.core.exception.AkShareParseException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * HTML Parser implementation based on Jsoup
 * Parses HTML tables to DataFrame
 */
@Slf4j
public class HtmlParser implements DataParser {

    @Override
    public DataFrame parse(String data) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            Document doc = Jsoup.parse(data);
            return parseDocument(doc);
        } catch (Exception e) {
            log.error("Failed to parse HTML", e);
            throw new AkShareParseException("HTML", data, "Invalid HTML format", e);
        }
    }

    /**
     * Parse HTML table by CSS selector
     *
     * @param data raw HTML string
     * @param tableSelector CSS selector for the table
     * @return DataFrame
     */
    public DataFrame parseTable(String data, String tableSelector) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            Document doc = Jsoup.parse(data);
            Element table = doc.selectFirst(tableSelector);
            if (table == null) {
                log.warn("Table not found with selector: {}", tableSelector);
                return new DataFrameImpl();
            }
            return parseTableElement(table);
        } catch (Exception e) {
            log.error("Failed to parse HTML table", e);
            throw new AkShareParseException("HTML", data, "Invalid HTML format", e);
        }
    }

    /**
     * Parse HTML table by index
     *
     * @param data raw HTML string
     * @param tableIndex index of the table (0-based)
     * @return DataFrame
     */
    public DataFrame parseTableByIndex(String data, int tableIndex) {
        if (data == null || data.trim().isEmpty()) {
            return new DataFrameImpl();
        }

        try {
            Document doc = Jsoup.parse(data);
            Elements tables = doc.select("table");
            if (tableIndex >= tables.size()) {
                log.warn("Table index {} out of range, total tables: {}", tableIndex, tables.size());
                return new DataFrameImpl();
            }
            return parseTableElement(tables.get(tableIndex));
        } catch (Exception e) {
            log.error("Failed to parse HTML table", e);
            throw new AkShareParseException("HTML", data, "Invalid HTML format", e);
        }
    }

    private DataFrame parseDocument(Document doc) {
        // Try to find the first table with data
        Elements tables = doc.select("table");

        for (Element table : tables) {
            DataFrame df = parseTableElement(table);
            if (df.rowCount() > 0) {
                return df;
            }
        }

        return new DataFrameImpl();
    }

    private DataFrame parseTableElement(Element table) {
        // Extract headers
        List<String> headers = new ArrayList<>();
        Element thead = table.selectFirst("thead");
        if (thead != null) {
            Elements ths = thead.select("th");
            for (Element th : ths) {
                headers.add(th.text().trim());
            }
        } else {
            // Try to get headers from first row
            Element firstRow = table.selectFirst("tr");
            if (firstRow != null) {
                Elements ths = firstRow.select("th");
                if (!ths.isEmpty()) {
                    for (Element th : ths) {
                        headers.add(th.text().trim());
                    }
                } else {
                    // Use td as headers
                    Elements tds = firstRow.select("td");
                    for (int i = 0; i < tds.size(); i++) {
                        headers.add("Column" + (i + 1));
                    }
                }
            }
        }

        // Extract data rows
        List<Row> rows = new ArrayList<>();
        Element tbody = table.selectFirst("tbody");
        Elements dataRows = tbody != null ? tbody.select("tr") : table.select("tr");

        boolean isFirstRow = thead == null; // Skip first row if no thead (it's headers)

        for (Element row : dataRows) {
            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }

            Elements tds = row.select("td");
            if (tds.isEmpty()) {
                continue;
            }

            Map<String, Object> rowData = new HashMap<>();
            for (int i = 0; i < tds.size() && i < headers.size(); i++) {
                rowData.put(headers.get(i), tds.get(i).text().trim());
            }
            rows.add(new Row(rowData));
        }

        return new DataFrameImpl(headers, rows);
    }

    /**
     * Extract text content by CSS selector
     *
     * @param data raw HTML string
     * @param selector CSS selector
     * @return extracted text
     */
    public String extractText(String data, String selector) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(data);
            Element element = doc.selectFirst(selector);
            return element != null ? element.text().trim() : null;
        } catch (Exception e) {
            log.error("Failed to extract text from HTML", e);
            return null;
        }
    }

    /**
     * Extract attribute value by CSS selector
     *
     * @param data raw HTML string
     * @param selector CSS selector
     * @param attribute attribute name
     * @return extracted attribute value
     */
    public String extractAttribute(String data, String selector, String attribute) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(data);
            Element element = doc.selectFirst(selector);
            return element != null ? element.attr(attribute) : null;
        } catch (Exception e) {
            log.error("Failed to extract attribute from HTML", e);
            return null;
        }
    }

    @Override
    public boolean supports(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        String trimmed = data.trim().toLowerCase();
        return trimmed.startsWith("<!doctype html") ||
               trimmed.startsWith("<html") ||
               trimmed.startsWith("<table") ||
               trimmed.startsWith("<div") ||
               trimmed.startsWith("<span");
    }
}
