package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.EntryBlockArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public EntryBlock getOrCreateEntry(short entryId){
        return getEntryBlockArray().getOrCreate(entryId);
    }
    public EntryBlock getEntry(short entryId){
        return getEntryBlockArray().getEntry(entryId);
    }
    public ResConfig getResConfig(){
        return mResConfig;
    }
    public EntryBlockArray getEntryBlockArray(){
        return mEntryArray;
    }
    public List<EntryBlock> listEntries(){
        return listEntries(false);
    }
    public List<EntryBlock> listEntries(boolean skipNullBlock){
        List<EntryBlock> results=new ArrayList<>();
        Iterator<EntryBlock> itr = mEntryArray.iterator(skipNullBlock);
        while (itr.hasNext()){
            EntryBlock block=itr.next();
            results.add(block);
        }
        return results;
    }
    public EntryBlock getEntryBlock(int entryId){
        return mEntryArray.get(entryId);
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
