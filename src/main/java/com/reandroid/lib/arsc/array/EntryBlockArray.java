package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.EntryBlock;


public class EntryBlockArray extends OffsetBlockArray<EntryBlock> {
    public EntryBlockArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
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
