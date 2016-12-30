package com.distelli.europa.registry;

import java.util.Map;
import java.util.Collections;

public class RegistryError extends RuntimeException {
    private static int DEFAULT_CODE = 500;
    private int _code;

    public RegistryError(String message) {
        this(message, DEFAULT_CODE);
    }

    public RegistryError(String message, Throwable cause) {
        this(message, cause, DEFAULT_CODE);
    }

    public RegistryError(Throwable cause) {
        this(cause, DEFAULT_CODE);
    }

    public RegistryError(String message, int code) {
        super(message);
        _code = code;
    }

    public RegistryError(String message, Throwable cause, int code) {
        super(message, cause);
        _code = code;
    }

    public RegistryError(Throwable cause, int code) {
        super(cause);
        _code = code;
    }

    public int getStatusCode() {
        return _code;
    }

    public Map<String, String> getResponseHeaders() {
        return Collections.emptyMap();
    }

    public Object getDetail() {
        if ( null != getCause() ) {
            return getCause().getClass().toString();
        }
        // Override this if you want to return other details...
        return null;
    }

    private class ErrorMessage {
        public int getCode() {
            return _code;
        }
        public String getMessage() {
            return RegistryError.this.getMessage();
        }
        public Object getDetail() {
            return RegistryError.this.getDetail();
        }
    }

    public Object getResponseBody() {
        return Collections.singletonList(new ErrorMessage());
    }
}
