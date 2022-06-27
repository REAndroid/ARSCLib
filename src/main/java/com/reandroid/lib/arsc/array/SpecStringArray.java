package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.SpecString;

public class SpecStringArray extends StringArray<SpecString> {
    public SpecStringArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public SpecString newInstance() {
        return new SpecString(isUtf8());
    }
    @Override
    public SpecString[] newInstance(int len) {
        return new SpecString[len];
    }
}
