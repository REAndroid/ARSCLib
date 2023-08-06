package com.reandroid.dex.item;

import com.reandroid.arsc.item.IntegerReference;

public class MethodDefArray extends DefArray<MethodDef> {
    public MethodDefArray(IntegerReference itemCount){
        super(itemCount);
    }
    @Override
    public MethodDef[] newInstance(int length) {
        return new MethodDef[length];
    }
    @Override
    public MethodDef newInstance() {
        return new MethodDef();
    }
}
