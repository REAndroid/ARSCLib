package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.EntryBlockArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.ResConfig;

public class TypeBlock extends BaseTypeBlock {
    private final IntegerItem mEntriesStart;
    private final ResConfig mResConfig;
    private final IntegerArray mEntryOffsets;
    private final EntryBlockArray mEntryArray;
    public TypeBlock() {
        super(ChunkType.TYPE, 2);
        this.mEntriesStart=new IntegerItem();
        this.mResConfig =new ResConfig();
        this.mEntryOffsets=new IntegerArray();
        this.mEntryArray=new EntryBlockArray(mEntryOffsets, getEntryCountBlock(), mEntriesStart);

        addToHeader(mEntriesStart);
        addToHeader(mResConfig);

        addChild(mEntryOffsets);
        addChild(mEntryArray);
    }
    public ResConfig getResConfig(){
        return mResConfig;
    }
    public EntryBlockArray getEntryBlockArray(){
        return mEntryArray;
    }
    @Override
    void onSetEntryCount(int count) {
        mEntryArray.setChildesCount(count);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", config=");
        builder.append(getResConfig().toString());
        return builder.toString();
    }
}
