package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TableString;

public class TableStringArray extends StringArray<TableString> {
    public TableStringArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart, is_utf8);
    }
    @Override
    public TableString newInstance() {
        return new TableString(isUtf8());
    }
    @Override
    public TableString[] newInstance(int len) {
        return new TableString[len];
    }
}
