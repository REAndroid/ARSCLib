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
package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.container.ExpandableBlockContainer;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;

public abstract class BaseChunk<T extends HeaderBlock> extends ExpandableBlockContainer {
    private final T mHeaderBlock;
    protected BaseChunk(T headerBlock, int initialChildesCount) {
        super(initialChildesCount+1);
        this.mHeaderBlock = headerBlock;
        addChild(headerBlock);
    }
    void setHeaderLoaded(HeaderBlock.HeaderLoaded headerLoaded){
        getHeaderBlock().setHeaderLoaded(headerLoaded);
    }
    public final T getHeaderBlock(){
        return mHeaderBlock;
    }
    @Override
    protected final void onRefreshed() {
        getHeaderBlock().refreshHeader();
        onChunkRefreshed();
    }
    protected abstract void onChunkRefreshed();
    public void onChunkLoaded(){

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        checkInvalidChunk(headerBlock);
        BlockReader chunkReader = reader.create(
                reader.getPosition(),
                headerBlock.getChunkSize());
        super.onReadBytes(chunkReader);
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    void checkInvalidChunk(HeaderBlock headerBlock) throws IOException {
        ChunkType chunkType = headerBlock.getChunkType();
        if(chunkType==null || chunkType==ChunkType.NULL){
            throw new IOException("Invalid chunk: "+headerBlock);
        }
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        builder.append(getHeaderBlock());
        return builder.toString();
    }
}
