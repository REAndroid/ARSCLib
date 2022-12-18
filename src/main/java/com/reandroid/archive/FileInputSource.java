package com.reandroid.archive;

import java.io.*;

public class FileInputSource extends InputSource{
    private final File file;
    public FileInputSource(File file, String name){
        super(name);
        this.file=file;
    }
    @Override
    public void close(InputStream inputStream) throws IOException {
        inputStream.close();
    }
    @Override
    public FileInputStream openStream() throws IOException {
        return new FileInputStream(this.file);
    }
    public File getFile(){
        return file;
    }
}
