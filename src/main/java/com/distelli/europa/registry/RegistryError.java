package com.distelli.europa.registry;

import java.util.List;
import java.util.Map;
import java.util.Collections;

public class RegistryError extends RuntimeException {
    private static final RegistryErrorCode DEFAULT_CODE = RegistryErrorCode.SERVER_ERROR;
    private RegistryErrorCode _code;
    private Integer _statusCode;

    public RegistryError(String message) {
        this(message, DEFAULT_CODE);
    }

    public RegistryError(String message, Throwable cause) {
        this(message, cause, DEFAULT_CODE);
    }

    public RegistryError(Throwable cause) {
        this(cause, DEFAULT_CODE);
    }

    public RegistryError(String message, RegistryErrorCode code) {
        this(message, code, code.getStatusCode());
    }

    public RegistryError(String message, Throwable cause, RegistryErrorCode code) {
        this(message, cause, code, code.getStatusCode());
    }

    public RegistryError(Throwable cause, RegistryErrorCode code) {
        this(cause, code, code.getStatusCode());
    }

    public RegistryError(String message, RegistryErrorCode code, int statusCode) {
        super(message);
        _code = code;
        _statusCode = statusCode;
    }

    public RegistryError(String message, Throwable cause, RegistryErrorCode code, int statusCode) {
        super(message, cause);
        _code = code;
        _statusCode = statusCode;
    }

    public RegistryError(Throwable cause, RegistryErrorCode code, int statusCode) {
        super(cause);
        _code = code;
        _statusCode = statusCode;
    }

    public RegistryErrorCode getErrorCode() {
        return _code;
    }

    public int getStatusCode() {
        return _statusCode;
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
        public RegistryErrorCode getCode() {
            return _code;
        }
        public String getMessage() {
            return RegistryError.this.getMessage();
        }
        public Object getDetail() {
            return RegistryError.this.getDetail();
        }
    }

    private static class ErrorMessageResponse {
        private ErrorMessage msg;
        public ErrorMessageResponse(ErrorMessage msg) {
            this.msg = msg;
        }
        public List<ErrorMessage> getErrors() {
            return Collections.singletonList(msg);
        }
    }

    public Object getResponseBody() {
        return new ErrorMessageResponse(new ErrorMessage());
    }
}
