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
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.CountedBlockItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.HexBytesWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;

public class HeaderBlock extends ExpandableBlockContainer implements DirectStreamReader {

    public HeaderBlock(int blocksSize, int type) {
        super(blocksSize + 4);

        setupBlock(
                new ShortItem(type), // chunk type
                new ShortItem(),     // header size
                new IntegerItem()    // chunk size
        );

        setupLast(new ExcessHeaderBytesBlock());
    }
    public HeaderBlock(int blocksSize, ChunkType chunkType) {
        this(blocksSize, chunkType.ID);
    }
    public HeaderBlock(ChunkType chunkType) {
        this(0, chunkType.ID);
    }
    
    public int getMinimumSize() {
        return countBytes();
    }

    public ChunkType getChunkType() {
        return ChunkType.get(type().get());
    }
    public int getType() {
        return type().get();
    }
    public void setType(ChunkType chunkType) {
        setType(chunkType == null ? 0 : chunkType.ID);
    }
    public void setType(int type) {
        type().set(type);
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
        return block(0);
    }
    public IntegerReference headerSize() {
        return block(1);
    }
    public IntegerReference chunkSize() {
        return block(2);
    }
    public CountedBlockItem excessBytes() {
        return blockLast();
    }

    public final void refreshHeader(Chunk<?> chunk) {
        setHeaderSize(countBytes());
        AlignItem alignItem = chunk.getAlignment();
        alignItem.clear();
        int count = chunk.countBytes();
        count += alignItem.align(count);
        setChunkSize(count);
    }
    boolean readExtraBytes() {
        return true;
    }

    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        return DirectStreamReader.Util.readBlock(this, inputStream);
    }

    @Override
    protected void onRefreshed() {
        // Not required, the parent should call refreshHeader()
    }
    @Override
    protected void refreshBlocks() {
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
        int excess = excessBytes().size();
        if (excess != 0) {
            builder.append(", excessBytes=");
            builder.append(excess);
        }
        builder.append("}");
        return builder.toString();
    }

    // For handling excess header bytes, unlikely used only when
    // the value of headerSize is greater than the defined structure
    static class ExcessHeaderBytesBlock extends CountedBlockItem implements IntegerReference {

        public ExcessHeaderBytesBlock() {
            super(null);
        }

        @Override
        public IntegerReference countReference() {
            return this;
        }

        private HeaderBlock header() {
            return getParentInstance(HeaderBlock.class);
        }

        @Override
        public boolean isNull() {
            return !header().readExtraBytes();
        }

        @Override
        public int get() {
            HeaderBlock header = header();
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
