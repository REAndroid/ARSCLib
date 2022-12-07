package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.array.SpecStringArray;
import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.SpecString;

public class SpecStringPool extends BaseStringPool<SpecString> {
    public SpecStringPool(boolean is_utf8){
        super(is_utf8);
    }

    @Override
    StringArray<SpecString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new SpecStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public PackageBlock getPackageBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof PackageBlock){
                return (PackageBlock) parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
}
