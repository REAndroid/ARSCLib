package com.reandroid.lib.arsc.header;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.ExpandableBlockContainer;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ShortItem;

public class HeaderBlock extends ExpandableBlockContainer {
    private final ShortItem mType;
    private final ShortItem mHeaderSize;
    private final IntegerItem mChunkSize;
    public HeaderBlock(short type){
        super(3);
        this.mType=new ShortItem(type);
        this.mHeaderSize=new ShortItem();
        this.mChunkSize=new IntegerItem();
        addChild(mType);
        addChild(mHeaderSize);
        addChild(mChunkSize);
    }
    public ChunkType getChunkType(){
        return ChunkType.get(mType.get());
    }
    public short getType(){
        return mType.get();
    }
    public void setType(ChunkType chunkType){
        short type;
        if(chunkType==null){
            type=0;
        }else {
            type=chunkType.ID;
        }
        setType(type);
    }
    public void setType(short type){
        mType.set(type);
    }

    public short getHeaderSize(){
        return mHeaderSize.get();
    }
    public void setHeaderSize(short headerSize){
        mHeaderSize.set(headerSize);
    }
    public int getChunkSize(){
        return mChunkSize.get();
    }
    public void setChunkSize(int chunkSize){
        mChunkSize.set(chunkSize);
    }

    public final void refreshHeader(){
        refreshHeaderSize();
        refreshChunkSize();
    }
    private void refreshHeaderSize(){
        int count=countBytes();
        setHeaderSize((short)count);
    }
    private void refreshChunkSize(){
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int count=parent.countBytes();
        setChunkSize(count);
    }

    @Override
    protected void onRefreshed() {
        // Not required, the parent should call refreshHeader()
    }
    @Override
    protected void refreshChildes(){
        // Not required
    }

    @Override
    public String toString(){
        short t= getType();
        ChunkType type= ChunkType.get(t);
        StringBuilder builder=new StringBuilder();
        if(type!=null){
            builder.append(type.toString());
        }else {
            builder.append("Unknown type=");
            builder.append(String.format("0x%02x", ((int)t)));
        }
        builder.append("{Header=");
        builder.append(getHeaderSize());
        builder.append(", Chunk=");
        builder.append(getChunkSize());
        builder.append("}");
        return builder.toString();
    }
}
