package com.reandroid.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteInputSource extends InputSource{
    private final byte[] inBytes;
    public ByteInputSource(byte[] inBytes, String name) {
        super(name);
        this.inBytes=inBytes;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        byte[] bts=getBytes();
        outputStream.write(bts);
        return bts.length;
    }
    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(getBytes());
    }
    public byte[] getBytes() {
        return inBytes;
    }
}
