package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.ResXmlStringArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ResXmlString;

public class ResXmlStringPool extends BaseStringPool<ResXmlString> {
    public ResXmlStringPool(boolean is_utf8) {
        super(is_utf8);
    }
    @Override
    StringArray<ResXmlString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new ResXmlStringArray(offsets, itemCount, itemStart, is_utf8);
    }
}
