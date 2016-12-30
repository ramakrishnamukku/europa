package com.distelli.europa;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import javax.servlet.WriteListener;
import java.io.UnsupportedEncodingException;

public class ServletByteArrayOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public void write(int i) throws IOException {
        out.write(i);
    }

    @Override
    public String toString() {
        try {
            return out.toString("UTF8");
        } catch ( UnsupportedEncodingException ex ) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }
}
