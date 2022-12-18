package com.reandroid.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipEntrySource extends InputSource{
    private final ZipFile zipFile;
    private final ZipEntry zipEntry;
    public ZipEntrySource(ZipFile zipFile, ZipEntry zipEntry){
        super(zipEntry.getName());
        this.zipFile=zipFile;
        this.zipEntry=zipEntry;
        super.setMethod(zipEntry.getMethod());
    }
    @Override
    public InputStream openStream() throws IOException {
        return zipFile.getInputStream(zipEntry);
    }
}
