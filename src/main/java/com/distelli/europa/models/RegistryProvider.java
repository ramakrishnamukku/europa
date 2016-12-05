package com.distelli.europa.models;

import java.util.Arrays;
import java.util.Objects;


public enum RegistryProvider
{
    DOCKERHUB,
    ECR,
    PRIVATE,
    GCR;

    private static final RegistryProvider[] values = values();

    public static RegistryProvider valueOf(int ordinal) {
        if ( ordinal < 0 || ordinal >= values.length ) return null;
        return values[ordinal];
    }

}
