package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RenamedInputSource<T extends InputSource> extends InputSource {
    private final T inputSource;
    public RenamedInputSource(String name, T input){
        super(name);
        this.inputSource=input;
        super.setMethod(input.getMethod());
        super.setSort(input.getSort());
    }
    public T getInputSource() {
        return inputSource;
    }
    @Override
    public void close(InputStream inputStream) throws IOException {
        getInputSource().close(inputStream);
    }
    @Override
    public long getLength() throws IOException {
        return getInputSource().getLength();
    }
    @Override
    public long getCrc() throws IOException {
        return getInputSource().getCrc();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getInputSource().write(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        return getInputSource().openStream();
    }
}
