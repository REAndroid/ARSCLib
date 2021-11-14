package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ResXmlString;

public class ResXmlStringArray extends StringArray<ResXmlString> {
    public ResXmlStringArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public ResXmlString newInstance() {
        return new ResXmlString(isUtf8());
    }
    @Override
    public ResXmlString[] newInstance(int len) {
        return new ResXmlString[len];
    }
}
