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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.item.CountedBlockItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.BytesOutputStream;

import java.io.*;

/**
 * This class can load any valid chunk, aimed to
 * handle any future android changes
 * */
public class UnknownChunk extends Chunk<HeaderBlock> {

    private final CountedBlockItem body;

    UnknownChunk(HeaderBlock header) {
        super(1, header);

        this.body = new CountedBlockItem(new IntegerReference() {
            @Override
            public int get() {
                return header.chunkSize().get() - header.headerSize().get();
            }
            @Override
            public void set(int value) {
            }
        });

        setupBlock(body);
    }
    public UnknownChunk() {
        this(new HeaderBlock(0, INITIAL_CHUNK_TYPE));
    }
    public CountedBlockItem getBody() {
        return body;
    }

    @Override
    void checkInvalidChunk(HeaderBlock headerBlock) {
    }
    @Override
    public byte[] getBytes() {
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeaderBlock().getChunkSize());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }
    public int readBytes(File file) throws IOException{
        FileInputStream inputStream = new FileInputStream(file);
        int result = readBytes(inputStream);
        inputStream.close();
        return result;
    }
    public int readBytes(InputStream inputStream) throws IOException{
        int result;
        result = getHeaderBlock().readBytes(inputStream);
        result += getBody().readBytes(inputStream);
        return result;
    }
    public final int writeBytes(File file) throws IOException{
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            if (dir.mkdirs()) {
                throw new IOException("Can not create directory: "+dir);
            }
        }
        OutputStream outputStream=new FileOutputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }
    @Override
    public String toString() {
        return getHeaderBlock()
                +" {Body=" + getBody().size()+"}";
    }

    private static final int INITIAL_CHUNK_TYPE = 0x0000;

    public static final Creator<UnknownChunk> CREATOR = UnknownChunk::new;
}
