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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;

import java.io.IOException;

public abstract class Chunk<T extends HeaderBlock> extends ExpandableBlockContainer {

    private final SingleBlockContainer<Block> firstPlaceHolder;

    protected Chunk(int blocksSize, T headerBlock, SingleBlockContainer<Block> firstPlaceHolder) {
        super(blocksSize + (firstPlaceHolder == null ? 2 : 3));

        this.firstPlaceHolder = firstPlaceHolder;

        setupBlock(headerBlock);
        if (firstPlaceHolder != null) {
            setupBlock(firstPlaceHolder);
        }
        setupLast(new AlignItem(true));
    }
    protected Chunk(int initialSize, T headerBlock) {
        this(initialSize, headerBlock, new SingleBlockContainer<>());
    }

    public AlignItem getAlignment() {
        return blockLast();
    }

    public SingleBlockContainer<Block> getFirstPlaceHolder() {
        return firstPlaceHolder;
    }
    public T getHeaderBlock() {
        return block(0);
    }
    @Override
    protected final void onRefreshed() {
        onChunkRefreshed();
        getHeaderBlock().refreshHeader(this);
    }

    protected void onChunkRefreshed() {

    }
    public void onChunkLoaded() {

    }
    @Override
    public void readBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = InfoHeader.read(reader);
        checkInvalidChunk(headerBlock);
        BlockReader chunkReader = reader.create(headerBlock.getChunkSize());
        onReadChildes(chunkReader);
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    protected void onReadChildes(BlockReader reader) throws IOException {
        super.readBytes(reader);
    }
    void checkInvalidChunk(HeaderBlock headerBlock) throws IOException {
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == null || chunkType == ChunkType.NULL) {
            throw new IOException("Invalid chunk: " + headerBlock);
        }
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() +  ": " + getHeaderBlock();
    }
}
