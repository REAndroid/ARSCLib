package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.ExpandableBlockContainer;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;

public abstract class BaseChunk extends ExpandableBlockContainer {
    private final HeaderBlock mHeaderBlock;
    protected BaseChunk(short chunkType, int initialChildesCount) {
        super(initialChildesCount+1);
        mHeaderBlock=new HeaderBlock(chunkType);
        addChild(mHeaderBlock);
    }
    protected BaseChunk(ChunkType chunkType, int initialChildesCount) {
        this(chunkType.ID, initialChildesCount);
    }
    protected void addToHeader(Block block){
        mHeaderBlock.addChild(block);
    }
    public HeaderBlock getHeaderBlock(){
        return mHeaderBlock;
    }
    @Override
    protected final void onRefreshed() {
        mHeaderBlock.refreshHeader();
        onChunkRefreshed();
    }
    protected abstract void onChunkRefreshed();
    public void onChunkLoaded(){

    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        BlockReader chunkReader=reader.create(reader.getPosition(), headerBlock.getChunkSize());
        super.onReadBytes(chunkReader);
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        builder.append(mHeaderBlock.toString());
        return builder.toString();
    }
}
