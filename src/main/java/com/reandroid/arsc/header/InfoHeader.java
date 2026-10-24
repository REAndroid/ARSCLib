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
package com.reandroid.arsc.header;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.FileChannelInputStream;
import com.reandroid.utils.ObjectsUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class InfoHeader extends HeaderBlock {

    public InfoHeader(int type) {
        super(1, type);
        setupBlock(3, new ByteItem());
    }
    public InfoHeader() {
        this(0);
    }

    public int getId() {
        return id().get();
    }

    public IntegerReference id() {
        return block(3);
    }

    @Override
    public int getMinimumSize() {
        return INFO_MIN_SIZE;
    }

    @Override
    boolean readExtraBytes() {
        return false;
    }

    @Override
    public int countBytes() {
        return 8;
    }

    public boolean isSpecOrType() {
        ChunkType chunkType = getChunkType();
        return chunkType == ChunkType.SPEC || chunkType == ChunkType.TYPE;
    }

    public void reset() {
        type().set(0);
        headerSize().set(0);
        chunkSize().set(0);
        id().set(0);
    }

    public boolean readInfo(BlockReader reader) {
        reset();
        boolean result = false;
        int available = reader.available();
        int min = getMinimumSize();
        if (available >= min) {
            int position = reader.getPosition();
            try {
                Object[] blocks = blocks();
                for (int i = 0; i < 3; i++) {
                    ((Block) blocks[i]).readBytes(reader);
                }
                if (isSpecOrType() && available > min) {
                    ((Block) blocks[3]).readBytes(reader);
                }
                result = true;
            } catch (IOException ignored) {
            }
            reader.seek(position);
        }
        return result;
    }

    public static InfoHeader readHeaderBlock(File file) throws IOException {
        return readHeaderBlock(FileChannelInputStream.read(file, INFO_MIN_SIZE));
    }
    public static InfoHeader readHeaderBlock(InputStream inputStream) throws IOException {
        InfoHeader infoHeader = new InfoHeader();
        infoHeader.readBytes(inputStream);
        return infoHeader;
    }
    public static InfoHeader readHeaderBlock(BlockReader blockReader) throws IOException {
        InfoHeader infoHeader = new InfoHeader();
        infoHeader.readBytes(blockReader);
        return infoHeader;
    }
    public static InfoHeader readHeaderBlock(byte[] bytes) throws IOException {
        BlockReader reader = new BlockReader(bytes);
        InfoHeader infoHeader = new InfoHeader();
        infoHeader.readBytes(reader);
        return infoHeader;
    }

    public static InfoHeader read(BlockReader reader) throws IOException {
        InfoHeader infoHeader = new InfoHeader();
        if (infoHeader.readInfo(reader)) {
            return infoHeader;
        }
        return ObjectsUtil.getNull();
    }

    public static final int INFO_MIN_SIZE = 8;
}
