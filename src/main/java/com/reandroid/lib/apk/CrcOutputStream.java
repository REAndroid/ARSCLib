package com.reandroid.lib.apk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class CrcOutputStream extends OutputStream {
    private final CRC32 crc;
    private long length;
    private long mCheckSum;
    public CrcOutputStream() {
        super();
        this.crc = new CRC32();
    }
    public long getLength(){
        return length;
    }
    public long getCrcValue(){
        if(mCheckSum==0){
            mCheckSum=crc.getValue();
        }
        return mCheckSum;
    }
    @Override
    public void write(int b) throws IOException {
        this.crc.update(b);
        length=length+1;
    }
    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.crc.update(b, off, len);
        length=length+len;
    }
}
