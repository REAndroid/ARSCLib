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
package com.reandroid.archive2.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class CountingOutputStream<T extends OutputStream> extends OutputStream {
    private final T outputStream;
    private CRC32 crc;
    private long size;
    public CountingOutputStream(T outputStream, boolean disableCrc){
        this.outputStream = outputStream;
        CRC32 crc32;
        if(disableCrc){
            crc32 = null;
        }else {
            crc32 = new CRC32();
        }
        this.crc = crc32;
    }
    public CountingOutputStream(T outputStream){
        this(outputStream, false);
    }

    public void disableCrc(boolean disableCrc) {
        if(!disableCrc){
            if(crc == null){
                this.crc = new CRC32();
            }
        }else{
            this.crc = null;
        }
    }

    public void reset(){
        this.crc = new CRC32();
        this.size = 0L;
    }
    public T getOutputStream() {
        return outputStream;
    }
    public long getSize() {
        return size;
    }
    public long getCrc() {
        if(crc != null){
            return crc.getValue();
        }
        return 0;
    }
    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException{
        if(length == 0){
            return;
        }
        outputStream.write(bytes, offset, length);
        this.size += length;
        if(this.crc != null){
            this.crc.update(bytes, offset, length);
        }
    }
    @Override
    public void write(byte[] bytes) throws IOException{
        this.write(bytes, 0, bytes.length);
    }
    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte) i}, 0, 1);
    }
    @Override
    public void close() throws IOException{
        outputStream.close();
    }
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }
    @Override
    public String toString(){
        return "[" + size + "]: " + outputStream.getClass().getSimpleName();
    }
}
