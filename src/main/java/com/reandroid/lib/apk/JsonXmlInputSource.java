package com.reandroid.lib.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.json.JSONObject;

import java.io.*;

public class JsonXmlInputSource extends InputSource {
    private final InputSource inputSource;
    public JsonXmlInputSource(InputSource inputSource) {
        super(inputSource.getAlias());
        this.inputSource=inputSource;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getResXmlBlock().writeBytes(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        ResXmlBlock resXmlBlock= getResXmlBlock();
        return new ByteArrayInputStream(resXmlBlock.getBytes());
    }
    @Override
    public long getLength() throws IOException{
        ResXmlBlock resXmlBlock = getResXmlBlock();
        return resXmlBlock.countBytes();
    }
    private ResXmlBlock getResXmlBlock() throws IOException{
        ResXmlBlock resXmlBlock=newInstance();
        InputStream inputStream=inputSource.openStream();
        JSONObject jsonObject=new JSONObject(inputStream);
        resXmlBlock.fromJson(jsonObject);
        inputStream.close();
        return resXmlBlock;
    }
    ResXmlBlock newInstance(){
        return new ResXmlBlock();
    }
    public static JsonXmlInputSource fromFile(File rootDir, File jsonFile){
        String path=ApkUtil.toArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource=new FileInputSource(jsonFile, path);
        return new JsonXmlInputSource(fileInputSource);
    }
}
