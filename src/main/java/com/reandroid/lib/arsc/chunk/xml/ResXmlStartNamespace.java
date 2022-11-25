package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;

public class ResXmlStartNamespace extends ResXmlNamespace<ResXmlEndNamespace> {
    public ResXmlStartNamespace() {
        super(ChunkType.XML_START_NAMESPACE);
    }
    public ResXmlEndNamespace getEnd(){
        return getPair();
    }
    public void setEnd(ResXmlEndNamespace namespace){
        setPair(namespace);
    }
}
