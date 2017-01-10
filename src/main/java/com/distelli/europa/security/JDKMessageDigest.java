package com.distelli.europa.security;

// Can't use org.bouncycastle.jce.provider.JDKMessageDigest
// since the constructor is protected :(.

import org.bouncycastle.crypto.Digest;
import java.security.MessageDigest;

public class JDKMessageDigest extends MessageDigest {
    private Digest digest;
    public JDKMessageDigest(Digest digest) {
        super(digest.getAlgorithmName());
        this.digest = digest;
    }

    public void engineReset() {
        digest.reset();
    }
    public void engineUpdate(byte input) {
        digest.update(input);
    }

    public void engineUpdate(byte[] input, int offset, int len) {
        digest.update(input, offset, len);
    }

    public byte[] engineDigest() {
        byte[]  digestBytes = new byte[digest.getDigestSize()];
        digest.doFinal(digestBytes, 0);
        return digestBytes;
    }
}
