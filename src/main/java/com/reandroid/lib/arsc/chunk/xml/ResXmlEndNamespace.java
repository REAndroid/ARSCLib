package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;

public class ResXmlEndNamespace extends  ResXmlNamespace<ResXmlStartNamespace>{
    public ResXmlEndNamespace() {
        super(ChunkType.XML_END_NAMESPACE);
    }
    public ResXmlStartNamespace getStart(){
        return getPair();
    }
    public void setStart(ResXmlStartNamespace namespace){
        setPair(namespace);
    }
}
