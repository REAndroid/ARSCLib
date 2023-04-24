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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelInputStream extends InputStream {
    private final FileChannel fileChannel;
    private final long length;
    private long total;
    private final byte[] buffer;
    private int pos;
    private int bufferLength;

    public FileChannelInputStream(FileChannel fileChannel, long length){
        this.fileChannel = fileChannel;
        this.length = length;
        int len = 1024 * 1000 * 100;
        if(length < len){
            len = (int) length;
        }
        this.buffer = new byte[len];
        this.bufferLength = len;
        this.pos = len;
    }
    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if(isFinished()){
            return -1;
        }
        if(length==0){
            return 0;
        }
        loadBuffer();
        int result = 0;
        int read = readBuffer(bytes, offset, length);
        result += read;
        length = length - read;
        offset = offset + read;
        while (length>0 && !isFinished()){
            loadBuffer();
            read = readBuffer(bytes, offset, length);
            result += read;
            length = length - read;
            offset = offset + read;
        }
        return result;
    }
    private int readBuffer(byte[] bytes, int offset, int length){
        int avail = bufferLength - pos;
        if(avail == 0){
            return 0;
        }
        int read = length;
        if(read > avail){
            read = avail;
        }
        System.arraycopy(buffer, pos, bytes, offset, read);
        pos += read;
        total += read;
        return read;
    }
    private void loadBuffer() throws IOException {
        byte[] buffer = this.buffer;
        if(this.pos < buffer.length){
            return;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        bufferLength = fileChannel.read(byteBuffer);
        pos = 0;
    }
    private boolean isFinished(){
        return total >= length;
    }
    @Override
    public int read() throws IOException {
        throw new IOException("Why one byte?");
    }
}
