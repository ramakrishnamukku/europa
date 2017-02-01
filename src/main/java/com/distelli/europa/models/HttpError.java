package com.distelli.europa.models;

public class HttpError extends RuntimeException {
    private String _body;
    private int _code;
    public HttpError(int code, String body) {
        super(toMessage(code, body));
        _code = code;
        _body = body;
    }
    private static String toMessage(int code, String body) {
        return "HTTP error code "+code+": "+body;
    }
    public String getBody() {
        return _body;
    }
    public int getCode() {
        return _code;
    }
}
