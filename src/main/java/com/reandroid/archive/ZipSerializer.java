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
package com.reandroid.archive;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipSerializer {
    private final List<InputSource> mSourceList;
    private WriteProgress writeProgress;
    private WriteInterceptor writeInterceptor;
    public ZipSerializer(List<InputSource> sourceList){
        this.mSourceList=sourceList;
    }

    public void setWriteInterceptor(WriteInterceptor writeInterceptor) {
        this.writeInterceptor = writeInterceptor;
    }
    public void setWriteProgress(WriteProgress writeProgress){
        this.writeProgress=writeProgress;
    }
    public long writeZip(File outZip) throws IOException{
        File dir=outZip.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        File tmp=toTmpFile(outZip);
        FileOutputStream fileOutputStream=new FileOutputStream(tmp);
        long length= writeZip(fileOutputStream);
        fileOutputStream.close();
        outZip.delete();
        tmp.renameTo(outZip);
        return length;
    }
    private File toTmpFile(File file){
        File dir=file.getParentFile();
        String name=file.getName()+".tmp";
        return new File(dir, name);
    }
    public long writeZip(OutputStream outputStream) throws IOException{
        long length=0;
        WriteProgress progress=writeProgress;
        ZipOutputStream zipOutputStream=new ZipOutputStream(outputStream);
        for(InputSource inputSource:mSourceList){
            inputSource = interceptWrite(inputSource);
            if(inputSource==null){
                continue;
            }
            if(progress!=null){
                progress.onCompressFile(inputSource.getAlias(), inputSource.getMethod(), length);
            }
            length+=write(zipOutputStream, inputSource);
            zipOutputStream.closeEntry();
            inputSource.disposeInputSource();
        }
        zipOutputStream.close();
        return length;
    }
    private long write(ZipOutputStream zipOutputStream, InputSource inputSource) throws IOException{
        ZipEntry zipEntry=createZipEntry(inputSource);
        zipOutputStream.putNextEntry(zipEntry);
        return inputSource.write(zipOutputStream);
    }
    private ZipEntry createZipEntry(InputSource inputSource) throws IOException {
        String name=inputSource.getAlias();
        ZipEntry zipEntry=new ZipEntry(name);
        int method = inputSource.getMethod();
        zipEntry.setMethod(method);
        if(method==ZipEntry.STORED){
            zipEntry.setCrc(inputSource.getCrc());
            zipEntry.setSize(inputSource.getLength());
        }
        return zipEntry;
    }
    private InputSource interceptWrite(InputSource inputSource){
        WriteInterceptor interceptor=writeInterceptor;
        if(interceptor!=null){
            return interceptor.onWriteArchive(inputSource);
        }
        return inputSource;
    }
}
