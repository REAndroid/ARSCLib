package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

public class ResXmlBlock extends BaseChunk {
    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private final ResXmlElement mResXmlElement;
    public ResXmlBlock() {
        super(ChunkType.XML,3);
        this.mResXmlStringPool=new ResXmlStringPool(true);
        this.mResXmlIDMap=new ResXmlIDMap();
        this.mResXmlElement=new ResXmlElement();
        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mResXmlElement);
    }
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getResXmlElement(){
        return mResXmlElement;
    }
    @Override
    protected void onChunkRefreshed() {

    }
}
