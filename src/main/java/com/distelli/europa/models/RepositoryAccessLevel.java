package com.distelli.europa.models;

import java.util.Arrays;
import java.util.Objects;

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
