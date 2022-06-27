package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.SpecBlock;

public class SpecBlockArray extends BlockArray<SpecBlock> {
    public SpecBlockArray(){
        super();
    }
    @Override
    public SpecBlock newInstance() {
        return new SpecBlock();
    }

    @Override
    public SpecBlock[] newInstance(int len) {
        return new SpecBlock[len];
    }

    @Override
    protected void onRefreshed() {

    }
}
