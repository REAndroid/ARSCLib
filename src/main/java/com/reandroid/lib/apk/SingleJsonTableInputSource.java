package com.reandroid.lib.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.json.JSONObject;

import java.io.*;

public class SingleJsonTableInputSource extends InputSource {
    private final InputSource inputSource;
    private TableBlock mCache;
    public SingleJsonTableInputSource(InputSource inputSource) {
        super(inputSource.getAlias());
        this.inputSource=inputSource;
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
    private TableBlock getTableBlock() throws IOException{
        if(mCache!=null){
            return mCache;
        }
        TableBlock tableBlock=newInstance();
        InputStream inputStream=inputSource.openStream();
        JSONObject jsonObject=new JSONObject(inputStream);
        StringPoolBuilder poolBuilder=new StringPoolBuilder();
        poolBuilder.build(jsonObject);
        poolBuilder.apply(tableBlock);
        tableBlock.fromJson(jsonObject);
        mCache=tableBlock;
        return tableBlock;
    }
    TableBlock newInstance(){
        return new TableBlock();
    }
    public static SingleJsonTableInputSource fromFile(File rootDir, File jsonFile){
        String path=ApkUtil.toArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource=new FileInputSource(jsonFile, path);
        return new SingleJsonTableInputSource(fileInputSource);
    }
}
