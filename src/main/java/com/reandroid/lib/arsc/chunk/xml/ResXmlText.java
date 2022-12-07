package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ResXmlString;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

public class ResXmlText extends BaseXmlChunk {
    private final IntegerItem mReserved;
    public ResXmlText() {
        super(ChunkType.XML_CDATA, 1);
        this.mReserved=new IntegerItem();
        addChild(mReserved);
        setStringReference(0);
    }
    public String getText(){
        ResXmlString xmlString=getResXmlString(getTextReference());
        if(xmlString!=null){
            return xmlString.getHtml();
        }
        return null;
    }
    public int getTextReference(){
        return getNamespaceReference();
    }
    public void setTextReference(int ref){
        setNamespaceReference(ref);
    }
    public void setText(String text){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool==null){
            return;
        }
        ResXmlString resXmlString = stringPool.getOrCreate(text);
        int ref=resXmlString.getIndex();
        setTextReference(ref);
    }
    @Override
    public String toString(){
        String txt=getText();
        if(txt!=null){
            return txt;
        }
        return super.toString();
    }
}
