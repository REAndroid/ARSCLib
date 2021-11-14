package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TypeString;

public class TypeStringArray extends StringArray<TypeString> {
    public TypeStringArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public TypeString newInstance() {
        return new TypeString(isUtf8());
    }
    @Override
    public TypeString[] newInstance(int len) {
        return new TypeString[len];
    }
}
