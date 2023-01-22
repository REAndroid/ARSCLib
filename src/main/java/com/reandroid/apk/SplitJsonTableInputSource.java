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

import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.TableBlock;

import java.io.*;

public class SplitJsonTableInputSource extends InputSource {
    private final File dir;
    private TableBlock mCache;
    private APKLogger apkLogger;
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
    @Override
    public long getCrc() throws IOException {
        CrcOutputStream outputStream=new CrcOutputStream();
        this.write(outputStream);
        return outputStream.getCrcValue();
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
    void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    void logMessage(String msg) {
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
