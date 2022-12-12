package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.ResXmlAttributeArray;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ShortItem;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

import java.io.IOException;
import java.util.Collection;


public class ResXmlStartElement extends BaseXmlChunk {
    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeUnitSize;
    private final ShortItem mAttributeCount;
    private final ShortItem mIdAttributePosition;
    private final ShortItem mClassAttributePosition;
    private final ShortItem mStyleAttributePosition;
    private final ResXmlAttributeArray mAttributeArray;
    private ResXmlEndElement mResXmlEndElement;
    public ResXmlStartElement() {
        super(ChunkType.XML_START_ELEMENT, 6);
        mAttributeStart=new ShortItem(ATTRIBUTES_DEFAULT_START);
        mAttributeUnitSize =new ShortItem(ATTRIBUTES_UNIT_SIZE);
        mAttributeCount=new ShortItem();
        mIdAttributePosition =new ShortItem();
        mClassAttributePosition=new ShortItem();
        mStyleAttributePosition=new ShortItem();
        mAttributeArray=new ResXmlAttributeArray(getHeaderBlock(), mAttributeStart, mAttributeCount);
        addChild(mAttributeStart);
        addChild(mAttributeUnitSize);
        addChild(mAttributeCount);
        addChild(mIdAttributePosition);
        addChild(mClassAttributePosition);
        addChild(mStyleAttributePosition);
        addChild(mAttributeArray);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        if(mClassAttributePosition.get()==0 && mStyleAttributePosition.get()==0){
            return;
        }
        ResXmlStringPool stringPool=getStringPool();
        int c=(mClassAttributePosition.get() & 0xffff)-1;
        int style = (mStyleAttributePosition.get() >>> 16) - 1;
        int z=c+style;
        stringPool.childesCount();
    }
    @Override
    protected void onPreRefreshRefresh(){
        sortAttributes();
    }
    private void sortAttributes(){
        ResXmlAttributeArray array = getResXmlAttributeArray();

        ResXmlAttribute idAttribute=array.get(mIdAttributePosition.get()-1);
        ResXmlAttribute classAttribute=array.get(mClassAttributePosition.get()-1);
        ResXmlAttribute styleAttribute=array.get(mStyleAttributePosition.get()-1);

        array.sortAttributes();
        if(idAttribute!=null){
            mIdAttributePosition.set((short) (idAttribute.getIndex()+1));
        }
        if(classAttribute!=null){
            mClassAttributePosition.set((short) (classAttribute.getIndex()+1));
            // In case obfuscation
            if(!"class".equals(classAttribute.getName())){
                classAttribute.setName("class", 0);
            }
        }
        if(styleAttribute!=null){
            mStyleAttributePosition.set((short) (styleAttribute.getIndex()+1));
            // In case obfuscation
            if(!"style".equals(styleAttribute.getName())){
                styleAttribute.setName("style", 0);
            }
        }
    }
    void calculatePositions(){

        int android_id=0x010100d0;
        ResXmlAttribute idAttribute=getAttribute(android_id);
        ResXmlAttribute classAttribute=getNoIdAttribute("class");
        ResXmlAttribute styleAttribute=getNoIdAttribute("style");

        if(idAttribute!=null){
            mIdAttributePosition.set((short) (idAttribute.getIndex()+1));
        }
        if(classAttribute!=null){
            mClassAttributePosition.set((short) (classAttribute.getIndex()+1));
        }
        if(styleAttribute!=null){
            mStyleAttributePosition.set((short) (styleAttribute.getIndex()+1));
        }
    }
    public ResXmlAttribute getAttribute(int resourceId){
        for(ResXmlAttribute attribute:listResXmlAttributes()){
            if(resourceId==attribute.getNameResourceID()){
                return attribute;
            }
        }
        return null;
    }
    private ResXmlAttribute getNoIdAttribute(String name){
        for(ResXmlAttribute attribute:listResXmlAttributes()){
            if(attribute.getNameResourceID()!=0){
                continue;
            }
            if(name.equals(attribute.getName())){
                return attribute;
            }
        }
        return null;
    }
    public ResXmlAttribute getAttribute(String uri, String name){
        if(name==null){
            return null;
        }
        for(ResXmlAttribute attribute:listResXmlAttributes()){
            if(name.equals(attribute.getName())||name.equals(attribute.getFullName())){
                if(uri!=null){
                    if(uri.equals(attribute.getUri())){
                        return attribute;
                    }
                    continue;
                }
                return attribute;
            }
        }
        return null;
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
    public ResXmlAttribute searchAttributeByResourceId(int resourceId){
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

    public String getUri(){
        int uriRef=getNamespaceReference();
        if(uriRef<0){
            return null;
        }
        ResXmlElement parentElement=getParentResXmlElement();
        ResXmlStartNamespace startNamespace=parentElement.getStartNamespaceByUriRef(uriRef);
        if(startNamespace!=null){
            return startNamespace.getUri();
        }
        return null;
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
