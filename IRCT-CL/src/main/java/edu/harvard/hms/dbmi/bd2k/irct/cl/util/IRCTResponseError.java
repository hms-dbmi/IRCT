package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

public class IRCTResponseError {

    private String status = "error";

    private Object message;

    public IRCTResponseError() {
    }

    public IRCTResponseError(String status) {
        this.status = status;
    }

    public IRCTResponseError(Object message) {
        this.message = message;
    }

    public IRCTResponseError(String status, Object message) {
        if (status != null && !status.isEmpty())
            this.status = status;
        this.message = message;
    }
}
