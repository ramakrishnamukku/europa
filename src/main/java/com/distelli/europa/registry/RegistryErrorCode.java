package com.distelli.europa.registry;

public enum RegistryErrorCode {
    BLOB_UNKNOWN(400),
    BLOB_UPLOAD_INVALID(400),
    BLOB_UPLOAD_UNKNOWN(404),
    DIGEST_INVALID(400),
    MANIFEST_BLOB_UNKNOWN(404),
    MANIFEST_INVALID(400),
    MANIFEST_UNKNOWN(404),
    MANIFEST_UNVERIFIED(400),
    NAME_INVALID(400),
    NAME_UNKNOWN(404),
    SIZE_INVALID(400),
    TAG_INVALID(400),
    UNAUTHORIZED(401),
    RANGE_INVALID(400),

    RANGE_NOT_SATISFIABLE(416),
    DENIED(403),
    UNSUPPORTED(405),
    SERVER_ERROR(500);

    private int statusCode;
    private RegistryErrorCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getStatusCode() {
        return statusCode;
    }
}
