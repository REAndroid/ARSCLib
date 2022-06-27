package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.xml.BaseXmlChunk;

public class ResXmlChunkArray extends BlockArray<BaseXmlChunk> {

    @Override
    public BaseXmlChunk newInstance() {
        return null;
    }

    @Override
    public BaseXmlChunk[] newInstance(int len) {
        return new BaseXmlChunk[len];
    }

    @Override
    protected void onRefreshed() {

    }
}
