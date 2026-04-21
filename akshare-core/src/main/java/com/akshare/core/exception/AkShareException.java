package com.akshare.core.exception;

import lombok.Getter;

/**
 * Base exception for AKShare Java
 * All AKShare exceptions should extend this class
 */
@Getter
public class AkShareException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public AkShareException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public AkShareException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public AkShareException(String errorMessage) {
        super(errorMessage);
        this.errorCode = "AKSHARE_ERROR";
        this.errorMessage = errorMessage;
    }

    public AkShareException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = "AKSHARE_ERROR";
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format("AkShareException[code=%s, message=%s]", errorCode, errorMessage);
    }
}
