package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;

public class ResXmlEndElement extends BaseXmlChunk  {
    private ResXmlStartElement mResXmlStartElement;
    public ResXmlEndElement(){
        super(ChunkType.XML_END_ELEMENT, 0);
    }

    public void setResXmlStartElement(ResXmlStartElement element){
        mResXmlStartElement=element;
    }
    public ResXmlStartElement getResXmlStartElement(){
        return mResXmlStartElement;
    }

}
