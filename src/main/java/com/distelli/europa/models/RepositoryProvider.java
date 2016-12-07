package com.distelli.europa.models;

public enum RepositoryProvider
{
    BITBUCKET,
    BITBUCKETSERVER,
    GITHUB,
    GITHUBENTERPRISE,
    GITLAB;

    private static final RepositoryProvider[] values = values();

    public static RepositoryProvider valueOf(int ordinal) {
        if ( ordinal < 0 || ordinal >= values.length ) return null;
        return values[ordinal];
    }
}
