package com.distelli.europa.models;

import java.util.Set;

public class UnknownDigests extends RuntimeException {
    private Set<String> digests;
    public UnknownDigests(String msg, Set<String> digests) {
        super(msg);
        this.digests = digests;
    }
    public Set<String> getDigests() {
        return digests;
    }
}
