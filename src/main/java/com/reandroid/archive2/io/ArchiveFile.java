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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class ArchiveFile extends ZipSource{
    private final File file;
    private FileChannel fileChannel;
    private SlicedInputStream mCurrentInputStream;
    public ArchiveFile(File file){
        this.file = file;
    }
    @Override
    public long getLength(){
        return this.file.length();
    }
    @Override
    public byte[] getFooter(int minLength) throws IOException {
        long position = getLength();
        if(minLength>position){
            minLength = (int) position;
        }
        position = position - minLength;
        FileChannel fileChannel = getFileChannel();
        fileChannel.position(position);
        ByteBuffer buffer = ByteBuffer.allocate(minLength);
        fileChannel.read(buffer);
        return buffer.array();
    }
    @Override
    public InputStream getInputStream(long offset, long length) throws IOException {
        close();
        mCurrentInputStream = new SlicedInputStream(new FileInputStream(this.file), offset, length);
        return mCurrentInputStream;
    }
    @Override
    public OutputStream getOutputStream(long offset) throws IOException {
        return null;
    }
    private FileChannel getFileChannel() throws IOException {
        FileChannel fileChannel = this.fileChannel;
        if(fileChannel != null){
            return fileChannel;
        }
        synchronized (this){
            fileChannel = FileChannel.open(this.file.toPath(), StandardOpenOption.READ);
            this.fileChannel = fileChannel;
            return fileChannel;
        }
    }
    @Override
    public void close() throws IOException {
        closeChannel();
        closeCurrentInputStream();
    }
    private void closeChannel() throws IOException {
        FileChannel fileChannel = this.fileChannel;
        if(fileChannel == null){
            return;
        }
        synchronized (this){
            fileChannel.close();
            this.fileChannel = null;
        }
    }
    private void closeCurrentInputStream() throws IOException {
        SlicedInputStream current = this.mCurrentInputStream;
        if(current == null){
            return;
        }
        current.close();
        mCurrentInputStream = null;
    }
    @Override
    public String toString(){
        return "File: " + this.file;
    }
}
