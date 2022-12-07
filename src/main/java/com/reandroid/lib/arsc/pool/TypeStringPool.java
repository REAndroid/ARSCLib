package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.TypeStringArray;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TypeString;

import java.io.IOException;

public class TypeStringPool extends BaseStringPool<TypeString> {
    private final IntegerItem mTypeIdOffset;
    public TypeStringPool(boolean is_utf8, IntegerItem typeIdOffset) {
        super(is_utf8);
        this.mTypeIdOffset = typeIdOffset;
    }
    public TypeString getById(int id){
        int index=id-mTypeIdOffset.get()-1;
        return super.get(index);
    }
    public TypeString getOrCreate(int typeId, String typeName){
        int size=typeId-mTypeIdOffset.get();
        getStringsArray().ensureSize(size);
        TypeString typeString=getById(typeId);
        typeString.set(typeName);
        return typeString;
    }
    @Override
    StringArray<TypeString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TypeStringArray(offsets, itemCount, itemStart, is_utf8);
    }
}
