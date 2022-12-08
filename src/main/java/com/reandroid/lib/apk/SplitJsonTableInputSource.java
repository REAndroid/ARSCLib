package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.TableBlock;

import java.io.*;

public class SplitJsonTableInputSource extends InputSource {
    private final File dir;
    private TableBlock mCache;
    public SplitJsonTableInputSource(File dir) {
        super(TableBlock.FILE_NAME);
        this.dir=dir;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getTableBlock().writeBytes(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        TableBlock tableBlock = getTableBlock();
        return new ByteArrayInputStream(tableBlock.getBytes());
    }
    @Override
    public long getLength() throws IOException{
        TableBlock tableBlock = getTableBlock();
        return tableBlock.countBytes();
    }
    public TableBlock getTableBlock() throws IOException {
        if(mCache!=null){
            return mCache;
        }
        TableBlockJsonBuilder builder=new TableBlockJsonBuilder();
        TableBlock tableBlock=builder.scanDirectory(dir);
        mCache=tableBlock;
        return tableBlock;
    }
}
