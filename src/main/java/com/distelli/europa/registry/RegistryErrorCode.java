package com.distelli.europa.registry;

public enum RegistryErrorCode {
    SERVER_ERROR(500),
    UNAUTHORIZED(401);
    private int statusCode;
    private RegistryErrorCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getStatusCode() {
        return statusCode;
    }
}
