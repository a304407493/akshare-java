package com.akshare.core.exception;

/**
 * Exception thrown when data parsing fails
 * Includes JSON parse error, HTML parse error, CSV parse error, etc.
 */
public class AkShareParseException extends AkShareException {

    private final String dataType;
    private final String rawData;

    public AkShareParseException(String dataType, String message) {
        super("PARSE_ERROR", String.format("Failed to parse %s: %s", dataType, message));
        this.dataType = dataType;
        this.rawData = null;
    }

    public AkShareParseException(String dataType, String message, Throwable cause) {
        super("PARSE_ERROR", String.format("Failed to parse %s: %s", dataType, message), cause);
        this.dataType = dataType;
        this.rawData = null;
    }

    public AkShareParseException(String dataType, String rawData, String message, Throwable cause) {
        super("PARSE_ERROR", String.format("Failed to parse %s: %s", dataType, message), cause);
        this.dataType = dataType;
        this.rawData = rawData;
    }

    public String getDataType() {
        return dataType;
    }

    public String getRawData() {
        return rawData;
    }
}
