package com.reandroid.dex.item;

import com.reandroid.arsc.item.IntegerReference;

public class FieldDefArray extends DefArray<FieldDef>{
    public FieldDefArray(IntegerReference itemCount){
        super(itemCount);
    }
    @Override
    public FieldDef[] newInstance(int length) {
        return new FieldDef[length];
    }
    @Override
    public FieldDef newInstance() {
        return new FieldDef();
    }
}
