package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.ResXmlAttributeArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ShortItem;

import java.util.Collection;


public class ResXmlStartElement extends BaseXmlChunk {
    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeUnitSize;
    private final ShortItem mAttributeCount;
    private final ShortItem mIdAttribute;
    private final IntegerItem mClassAttribute;
    private final ResXmlAttributeArray mAttributeArray;
    private ResXmlEndElement mResXmlEndElement;
    public ResXmlStartElement() {
        super(ChunkType.XML_START_ELEMENT, 6);
        mAttributeStart=new ShortItem(ATTRIBUTES_DEFAULT_START);
        mAttributeUnitSize =new ShortItem(ATTRIBUTES_UNIT_SIZE);
        mAttributeCount=new ShortItem();
        mIdAttribute=new ShortItem();
        mClassAttribute=new IntegerItem();
        mAttributeArray=new ResXmlAttributeArray(getHeaderBlock(), mAttributeStart, mAttributeCount);
        addChild(mAttributeStart);
        addChild(mAttributeUnitSize);
        addChild(mAttributeCount);
        addChild(mIdAttribute);
        addChild(mClassAttribute);
        addChild(mAttributeArray);
    }
    public ResXmlAttribute searchAttributeByName(String name){
        if(name==null){
            return null;
        }
        for(ResXmlAttribute attribute:listResXmlAttributes()){
            if(name.equals(attribute.getFullName()) || name.equals(attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    public ResXmlAttribute searchAttributeById(int resourceId){
        if(resourceId==0){
            return null;
        }
        for(ResXmlAttribute attribute:listResXmlAttributes()){
            if(resourceId==attribute.getNameResourceID()){
                return attribute;
            }
        }
        return null;
    }
    public String getTagName(){
        String prefix=getPrefix();
        String name=getName();
        if(prefix==null){
            return name;
        }
        return prefix+":"+name;
    }
    public void setName(String name){
        setString(name);
        ResXmlEndElement endElement = getResXmlEndElement();
        if(endElement!=null){
            endElement.setString(name);
        }
    }
    public Collection<ResXmlAttribute> listResXmlAttributes(){
        return getResXmlAttributeArray().listItems();
    }
    public ResXmlAttributeArray getResXmlAttributeArray(){
        return mAttributeArray;
    }
    public String getPrefix(){
        int uriRef=getNamespaceReference();
        if(uriRef<0){
            return null;
        }
        ResXmlElement parentElement=getParentResXmlElement();
        ResXmlStartNamespace startNamespace=parentElement.getStartNamespaceByUriRef(uriRef);
        if(startNamespace!=null){
            return startNamespace.getPrefix();
        }
        return null;
    }
    public void setResXmlEndElement(ResXmlEndElement element){
        mResXmlEndElement=element;
    }
    public ResXmlEndElement getResXmlEndElement(){
        return mResXmlEndElement;
    }

    @Override
    protected void onChunkRefreshed() {
        refreshAttributeStart();
        refreshAttributeCount();
    }
    private void refreshAttributeStart(){
        int start=countUpTo(mAttributeArray);
        start=start-getHeaderBlock().getHeaderSize();
        mAttributeStart.set((short)start);
    }
    private void refreshAttributeCount(){
        int count=mAttributeArray.childesCount();
        mAttributeCount.set((short)count);
    }

    @Override
    public String toString(){
        String txt=getTagName();
        if(txt==null){
            return super.toString();
        }
        StringBuilder builder=new StringBuilder();
        builder.append(txt);
        ResXmlAttribute[] allAttr=mAttributeArray.getChildes();
        if(allAttr!=null){
            for(int i=0;i<allAttr.length;i++){
                if(i>10){
                    break;
                }
                builder.append(" ");
                builder.append(allAttr[i].toString());
            }
        }
        return builder.toString();
    }

    private static final short ATTRIBUTES_UNIT_SIZE=0x0014;
    private static final short ATTRIBUTES_DEFAULT_START=0x0014;
}
