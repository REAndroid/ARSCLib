package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.TypeStringArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TypeString;

public class TypeStringPool extends BaseStringPool<TypeString> {
    public TypeStringPool(boolean is_utf8) {
        super(is_utf8);
    }
    public TypeString getById(int id){
        return super.get(id-1);
    }
    @Override
    StringArray<TypeString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TypeStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public void recreateStyles(){
    }
}
