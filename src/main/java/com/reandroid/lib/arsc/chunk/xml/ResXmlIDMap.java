package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;

public class ResXmlIDMap extends BaseChunk {
    private final ResXmlIDArray mResXmlIDArray;
    public ResXmlIDMap() {
        super(ChunkType.XML_RESOURCE_MAP, 1);
        this.mResXmlIDArray=new ResXmlIDArray(getHeaderBlock());
        addChild(mResXmlIDArray);
    }
    public ResXmlIDArray getResXmlIDArray(){
        return mResXmlIDArray;
    }

    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
}
