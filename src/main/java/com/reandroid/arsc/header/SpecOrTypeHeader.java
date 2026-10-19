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

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SpecOrTypeHeader extends HeaderBlock {

    private final ByteItem id;
    private final boolean mReadExtraBytes;

    public SpecOrTypeHeader(int childesCount, int type, boolean readExtra) {
        super(childesCount + 1, type);
        this.id = new ByteItem();
        this.mReadExtraBytes = readExtra;

        addChild(3, id);
    }
    public SpecOrTypeHeader(int childesCount, int type) {
        this(childesCount, type, true);
    }
    public SpecOrTypeHeader(int childesCount, ChunkType chunkType) {
        this(childesCount, chunkType.ID, true);
    }
    SpecOrTypeHeader() {
        this(0, 0, false);
    }

    public IntegerReference id() {
        return id;
    }
    public int getId() {
        return id().get();
    }
    public void setId(int id) {
        if (id < 0 || id > 0xff) {
            throw new IllegalArgumentException("Invalid type id value: " + id);
        }
        id().set(id);
    }

    @Override
    boolean readExtraBytes() {
        return mReadExtraBytes;
    }

    @Override
    public String toString() {
        ChunkType chunkType = getChunkType();
        if (chunkType != ChunkType.SPEC && chunkType != ChunkType.TYPE) {
            return super.toString();
        }
        return chunkType + ", header=" + headerSize()
                + ", chunk=" + chunkSize() + ", id=" + HexUtil.toHex2(getId());
    }

    public static SpecOrTypeHeader read(BlockReader reader) throws IOException {
        SpecOrTypeHeader header = new SpecOrTypeHeader();
        if (reader.available() < header.countBytes()) {
            throw new IOException("Too few bytes to read spec header, available = " + reader.available());
        }
        int pos = reader.getPosition();
        header.readBytes(reader);
        reader.seek(pos);
        return header;
    }
}
