package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.util.Iterator;


public class EntryBlockArray extends OffsetBlockArray<EntryBlock> {
    public EntryBlockArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
    }
    public boolean isEmpty(){
        return !iterator(true).hasNext();
    }
    public void setEntry(short entryId, EntryBlock entryBlock){
        setItem(entryId, entryBlock);
    }
    public EntryBlock getOrCreate(short entryId){
        EntryBlock entryBlock=get(entryId);
        if(entryBlock!=null){
            return entryBlock;
        }
        ensureSize(entryId+1);
        return get(entryId);
    }
    public EntryBlock getEntry(short entryId){
        return get(entryId);
    }
    @Override
    public EntryBlock newInstance() {
        return new EntryBlock();
    }

    @Override
    public EntryBlock[] newInstance(int len) {
        return new EntryBlock[len];
    }

}
