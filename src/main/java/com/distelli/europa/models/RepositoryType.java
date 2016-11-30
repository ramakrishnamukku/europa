package com.distelli.europa.models;

import java.util.Arrays;
import java.util.Objects;

public enum RepositoryType
{
    HG,
    GIT;

    private static final RepositoryType[] values = values();

    public static RepositoryType valueOf(int ordinal) {
        if ( ordinal < 0 || ordinal >= values.length ) return null;
        return values[ordinal];
    }

}
