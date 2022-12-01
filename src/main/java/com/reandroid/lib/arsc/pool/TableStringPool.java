package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.TableStringArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TableString;

public class TableStringPool extends BaseStringPool<TableString> {
    public TableStringPool(boolean is_utf8) {
        super(is_utf8);
    }

    @Override
    StringArray<TableString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new TableStringArray(offsets, itemCount, itemStart, is_utf8);
    }
}
