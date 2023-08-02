package com.reandroid.dex.sections;

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.dex.base.DexOffsetArray;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.header.OffsetAndCount;
import com.reandroid.dex.item.StringIndex;

import java.util.Iterator;

public class DexStringPool extends FixedBlockContainer implements Iterable<StringIndex> {

    private final DexStringArray dexStringArray;

    public DexStringPool(OffsetAndCount offsetAndCount) {
        super(2);
        DexOffsetArray offsetArray = new DexOffsetArray(offsetAndCount);
        DexStringArray dexStringArray = new DexStringArray(offsetAndCount, offsetArray);
        this.dexStringArray = dexStringArray;

        addChild(0, offsetArray);
        addChild(1, dexStringArray);
    }
    public DexStringPool(DexHeader dexHeader) {
        this(dexHeader.strings);
    }

    public StringIndex get(int index){
        return dexStringArray.get(index);
    }
    public int size(){
        return dexStringArray.childesCount();
    }
    @Override
    public Iterator<StringIndex> iterator() {
        return dexStringArray.iterator();
    }
}
