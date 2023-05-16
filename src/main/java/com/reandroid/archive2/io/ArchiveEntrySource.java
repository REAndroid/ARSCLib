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

import com.reandroid.archive.InputSource;
import com.reandroid.archive2.ArchiveEntry;
import com.reandroid.archive2.block.LocalFileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;

public class ArchiveEntrySource extends InputSource {
    private final ZipInput zipInput;
    private final ArchiveEntry archiveEntry;
    public ArchiveEntrySource(ZipInput zipInput, ArchiveEntry archiveEntry){
        super(archiveEntry.getName());
        this.zipInput = zipInput;
        this.archiveEntry = archiveEntry;
        setMethod(archiveEntry.getMethod());
    }

    @Override
    public byte[] getBytes(int length) throws IOException {
        if(getMethod() != ZipEntry.STORED){
            return super.getBytes(length);
        }
        FileChannel fileChannel = getFileChannel();
        byte[] bytes = new byte[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        fileChannel.read(byteBuffer);
        return bytes;
    }

    public FileChannel getFileChannel() throws IOException {
        FileChannel fileChannel = getZipSource().getFileChannel();
        fileChannel.position(getFileOffset());
        return fileChannel;
    }
    public ZipInput getZipSource(){
        return zipInput;
    }
    public ArchiveEntry getArchiveEntry() {
        return archiveEntry;
    }
    public long getFileOffset(){
        return getArchiveEntry().getFileOffset();
    }
    @Override
    public long getLength() throws IOException{
        return getArchiveEntry().getDataSize();
    }
    @Override
    public long getCrc() throws IOException{
        return getArchiveEntry().getCrc();
    }
    @Override
    public InputStream openStream() throws IOException {
        ArchiveEntry archiveEntry = getArchiveEntry();
        LocalFileHeader lfh = archiveEntry.getLocalFileHeader();
        InputStream inputStream = getZipSource().getInputStream(
                archiveEntry.getFileOffset(), archiveEntry.getDataSize());
        if(lfh.getSize() == lfh.getCompressedSize()){
            return inputStream;
        }
        return new InflaterInputStream(inputStream,
                new Inflater(true), 512);
    }
}
