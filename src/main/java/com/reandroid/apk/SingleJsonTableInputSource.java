/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;

import java.io.*;

public class SingleJsonTableInputSource extends InputSource {
    private final InputSource inputSource;
    private TableBlock mCache;
    private APKLogger apkLogger;
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
    @Override
    public long getCrc() throws IOException {
        CrcOutputStream outputStream=new CrcOutputStream();
        this.write(outputStream);
        return outputStream.getCrcValue();
    }
    public TableBlock getTableBlock() throws IOException{
        if(mCache != null){
            return mCache;
        }
        logMessage("Building resources table: " + inputSource.getAlias());
        TableBlock tableBlock=newInstance();
        InputStream inputStream = inputSource.openStream();
        try{
            StringPoolBuilder poolBuilder = new StringPoolBuilder();
            JSONObject jsonObject = new JSONObject(inputStream);
            poolBuilder.build(jsonObject);
            poolBuilder.apply(tableBlock);
            tableBlock.fromJson(jsonObject);
        }catch (JSONException ex){
            throw new IOException(inputSource.getAlias(), ex);
        }
        mCache = tableBlock;
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
    void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    private void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
