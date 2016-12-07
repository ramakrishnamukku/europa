package com.distelli.europa.models;

public enum RepositoryAccessLevel
{
    PUBLIC,
    INTERNAL,
    PRIVATE;

    private static final RepositoryAccessLevel[] values = values();

    public static RepositoryAccessLevel valueOf(int ordinal) {
        if ( ordinal < 0 || ordinal >= values.length ) return null;
        return values[ordinal];
    }

}
