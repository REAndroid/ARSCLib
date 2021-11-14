package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.arsc.value.ValueType;

public class ResXmlAttribute extends FixedBlockContainer {
    private final IntegerItem mNamespaceReference;
    private final IntegerItem mNameReference;
    private final IntegerItem mValueStringReference;
    private final ShortItem mNameType;
    private final ByteItem mReserved;
    private final ByteItem mValueTypeByte;
    private final IntegerItem mRawValue;
    public ResXmlAttribute() {
        super(7);
        mNamespaceReference =new IntegerItem();
        mNameReference =new IntegerItem();
        mValueStringReference =new IntegerItem();
        mNameType=new ShortItem();
        mReserved =new ByteItem();
        mValueTypeByte=new ByteItem();
        mRawValue=new IntegerItem();
        addChild(0, mNamespaceReference);
        addChild(1, mNameReference);
        addChild(2, mValueStringReference);
        addChild(3, mNameType);
        addChild(4, mReserved);
        addChild(5, mValueTypeByte);
        addChild(6, mRawValue);
    }
    public int getNamespaceReference(){
        return mNamespaceReference.get();
    }
    public void setNamespaceReference(int ref){
        mNamespaceReference.set(ref);
    }
    public int getNameReference(){
        return mNameReference.get();
    }
    public void setNameReference(int ref){
        mNameReference.set(ref);
    }
    public int getValueStringReference(){
        return mValueStringReference.get();
    }
    public void setValueStringReference(int ref){
        mValueStringReference.set(ref);
    }
    public short getNameType(){
        return mNameType.get();
    }
    public void setNameType(short s){
        mNameType.set(s);
    }
    public byte getValueTypeByte(){
        return mValueTypeByte.get();
    }
    public void setValueTypeByte(byte b){
        mValueTypeByte.set(b);
    }
    public int getRawValue(){
        return mRawValue.get();
    }
    public void setRawValue(int val){
        mRawValue.set(val);
    }

    public ValueType getValueType(){
        return ValueType.valueOf(getValueTypeByte());
    }
    public String getFullName(){
        String name=getName();
        if(name==null){
            return null;
        }
        String prefix=getNamePrefix();
        if(prefix==null){
            return name;
        }
        return prefix+":"+name;
    }
    public String getName(){
        return getString(getNameReference());
    }
    public String getNamePrefix(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement==null){
            return null;
        }
        ResXmlStartNamespace startNamespace=xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
        if(startNamespace==null){
            return null;
        }
        return startNamespace.getPrefix();
    }
    public String getValueString(){
        return getString(getValueStringReference());
    }
    public int getNameResourceID(){
        return getResourceId(getNameReference());
    }
    private int getResourceId(int ref){
        if(ref<=0){
            return 0;
        }
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        if(xmlIDMap==null){
            return 0;
        }
        ResXmlIDArray xmlIDArray = xmlIDMap.getResXmlIDArray();
        ResXmlID xmlID = xmlIDArray.get(ref);
        if(xmlID!=null){
            return xmlID.get();
        }
        return 0;
    }

    private String getString(int ref){
        if(ref<0){
            return null;
        }
        ResXmlString xmlString=getResXmlString(ref);
        if(xmlString!=null){
            return xmlString.getHtml();
        }
        return null;
    }
    private ResXmlString getResXmlString(int ref){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool!=null){
            return stringPool.get(ref);
        }
        return null;
    }
    private ResXmlStringPool getStringPool(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStringPool();
        }
        return null;
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getResXmlIDMap();
        }
        return null;
    }
    private ResXmlElement getParentResXmlElement(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlElement){
                return (ResXmlElement)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }

    @Override
    public String toString(){
        String fullName=getFullName();
        if(fullName!=null ){
            int id=getNameResourceID();
            if(id>0){
                fullName=fullName+"(@"+String.format("0x%08x",id)+")";
            }
            String valStr=getValueString();
            if(valStr!=null){
                return getIndex()+" {"+fullName+"=\""+valStr+"\""+"}";
            }
            return getIndex()+" {"+fullName+"}"+"["+getValueType()+"]=\""+getRawValue()+"\"";
        }
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        builder.append(getIndex());
        builder.append("{NamespaceReference=").append(getNamespaceReference());
        builder.append(", NameReference=").append(getNameReference());
        builder.append(", ValueStringReference=").append(getValueStringReference());
        builder.append(", NameType=").append(getNameType());
        builder.append(", ReservedByte=").append(mReserved.get());
        builder.append(", ValueTypeByte=").append(getValueTypeByte());
        builder.append(", RawValue=").append(getRawValue());
        builder.append("}");
        return builder.toString();
    }
}
