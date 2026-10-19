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

import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.item.CountedBlockItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.HexBytesWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;

public class HeaderBlock extends FixedBlockContainer implements DirectStreamReader {
    
    private final ShortItem mType;
    private final ShortItem mHeaderSize;
    private final IntegerItem mChunkSize;
    private final CountedBlockItem extraBytes;

    public HeaderBlock(int childesCount, int type) {
        super(childesCount + 4);
        this.mType = new ShortItem(type);
        this.mHeaderSize = new ShortItem();
        this.mChunkSize = new IntegerItem();
        this.extraBytes = new ExtraBytesBlock(this);
        addChild(0, mType);
        addChild(1, mHeaderSize);
        addChild(2, mChunkSize);
        addChild(childesCount + 3, extraBytes);
    }
    public HeaderBlock(int childesCount, ChunkType chunkType) {
        this(childesCount, chunkType.ID);
    }
    public HeaderBlock(ChunkType chunkType) {
        this(0, chunkType.ID);
    }
    
    public int getMinimumSize() {
        return countBytes();
    }

    public CountedBlockItem getExtraBytes() {
        return extraBytes;
    }
    public ChunkType getChunkType() {
        return ChunkType.get(mType.get());
    }
    public int getType() {
        return mType.get();
    }
    public void setType(ChunkType chunkType) {
        setType(chunkType == null ? 0 : chunkType.ID);
    }
    public void setType(int type) {
        mType.set(type);
    }

    public int getHeaderSize() {
        return headerSize().get();
    }
    public void setHeaderSize(int headerSize) {
        headerSize().set(headerSize);
    }
    public int getChunkSize() {
        return chunkSize().get();
    }
    public void setChunkSize(int chunkSize) {
        chunkSize().set(chunkSize);
    }

    public IntegerReference type() {
        return mType;
    }
    public IntegerReference headerSize() {
        return mHeaderSize;
    }
    public IntegerItem chunkSize() {
        return mChunkSize;
    }

    public final void refreshHeader() {
        refreshHeaderSize();
        refreshChunkSize();
    }
    private void refreshHeaderSize() {
        setHeaderSize(countBytes());
    }
    private void refreshChunkSize() {
        Block parent = getParentInstance(Chunk.class);
        if (parent != null) {
            setChunkSize(parent.countBytes());
        }
    }
    boolean readExtraBytes() {
        return true;
    }

    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        int result = DirectStreamReader.Util.readBlock(this, inputStream);
        super.notifyBlockLoad();
        return result;
    }

    @Override
    protected void onRefreshed() {
        // Not required, the parent should call refreshHeader()
    }
    @Override
    protected void refreshChildes() {
        // Not required
    }
    /**
     * Prints bytes in hex for debug/testing
     * */
    public String toHex() {
        return HexBytesWriter.toHex(getBytes());
    }
    @Override
    public String toString() {
        int t = getType();
        ChunkType type = ChunkType.get(t);
        StringBuilder builder = new StringBuilder();
        if (type != null) {
            builder.append(type.toString());
        } else {
            builder.append("Unknown type=");
            builder.append(HexUtil.toHex(t, 4));
        }
        builder.append("{Header=");
        builder.append(getHeaderSize());
        builder.append(", Chunk=");
        builder.append(getChunkSize());
        builder.append("}");
        return builder.toString();
    }

    static class ExtraBytesBlock extends CountedBlockItem implements IntegerReference {

        private final HeaderBlock header;

        public ExtraBytesBlock(HeaderBlock header) {
            super(null);
            this.header = header;
        }
        @Override
        public IntegerReference countReference() {
            return this;
        }

        @Override
        public boolean isNull() {
            return !header.readExtraBytes();
        }

        @Override
        public int get() {
            HeaderBlock header = this.header;
            int count = 0;
            if (header.readExtraBytes()) {
                count = header.countBytes() - this.countBytes();
                count = header.headerSize().get() - count;
                if (count < 0) {
                    count = 0;
                }
            }
            return count;
        }
        @Override
        public void set(int value) {
        }

        @Override
        public String toString() {
            return Integer.toString(countBytes());
        }
    }
}
