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
    public void merge(TableStringPool stringPool){
        if(stringPool==null||stringPool==this){
            return;
        }
        StringArray<TableString> existArray = getStringsArray();
        if(existArray.childesCount()!=0){
            return;
        }
        StringArray<TableString> comingArray = stringPool.getStringsArray();
        int count=comingArray.childesCount();
        existArray.ensureSize(count);
        for(int i=0;i<count;i++){
            TableString exist = existArray.get(i);
            TableString coming = comingArray.get(i);
            exist.set(coming.get());
        }
        getStyleArray().merge(stringPool.getStyleArray());
    }
}
