package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.container.SpecTypePair;

public class SpecTypePairArray extends BlockArray<SpecTypePair> {
    public SpecTypePairArray(){
        super();
    }
    @Override
    public SpecTypePair newInstance() {
        return new SpecTypePair();
    }
    @Override
    public SpecTypePair[] newInstance(int len) {
        return new SpecTypePair[len];
    }
    @Override
    protected void onRefreshed() {

    }
}
