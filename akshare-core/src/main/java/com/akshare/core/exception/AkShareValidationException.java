package com.akshare.core.exception;

/**
 * Exception thrown when parameter validation fails
 * Includes invalid stock code, invalid date format, invalid date range, etc.
 */
public class AkShareValidationException extends AkShareException {

    private final String fieldName;
    private final Object fieldValue;

    public AkShareValidationException(String fieldName, String message) {
        super("VALIDATION_ERROR", String.format("Validation failed for field [%s]: %s", fieldName, message));
        this.fieldName = fieldName;
        this.fieldValue = null;
    }

    public AkShareValidationException(String fieldName, Object fieldValue, String message) {
        super("VALIDATION_ERROR", String.format("Validation failed for field [%s] with value [%s]: %s", fieldName, fieldValue, message));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
