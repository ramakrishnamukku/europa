package com.distelli.europa.registry;

public enum RegistryErrorCode {
    SERVER_ERROR(500),
    NAME_UNKNOWN(404),

    NAME_INVALID(400),
    TAG_INVALID(400),
    MANIFEST_INVALID(400),
    MANIFEST_UNVERIFIED(400),
    BLOB_UNKNOWN(400),

    UNSUPPORTED(405),
    DENIED(403),
    UNAUTHORIZED(401);
    private int statusCode;
    private RegistryErrorCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getStatusCode() {
        return statusCode;
    }
}
