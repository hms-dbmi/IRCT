package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize
public class IRCTResponseError implements Serializable {

    private String errorType = "error";

    private Object message;

    public IRCTResponseError() {
    }

    public IRCTResponseError(String errorType) {
        this.errorType = errorType;
    }

    public IRCTResponseError(Object message) {
        this.message = message;
    }

    public IRCTResponseError(String errorType, Object message) {
        if (errorType != null && !errorType.isEmpty())
            this.errorType = errorType;
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
