package com.distelli.europa.util;

import java.util.regex.Pattern;

public class Tag {
    private static final Pattern SHA256_PATTERN = Pattern.compile("sha256:[0-9a-fA-F]{64}");
    public static boolean isDigest(String tag) {
        if ( null == tag ) return false;
        return SHA256_PATTERN.matcher(tag).matches();
    }
}
