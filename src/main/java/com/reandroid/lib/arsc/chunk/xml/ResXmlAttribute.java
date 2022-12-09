package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

public class ResXmlAttribute extends FixedBlockContainer
        implements Comparable<ResXmlAttribute>, JSONConvert<JSONObject> {
    private final IntegerItem mNamespaceReference;
    private final IntegerItem mNameReference;
    private final IntegerItem mValueStringReference;
    private final ShortItem mNameType;
    private final ByteItem mReserved;
    private final ByteItem mValueTypeByte;
    private final IntegerItem mRawValue;
    public ResXmlAttribute() {
        super(7);
        mNamespaceReference =new IntegerItem(-1);
        mNameReference =new IntegerItem();
        mValueStringReference =new IntegerItem();
        mNameType=new ShortItem((short) 0x0008);
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
    public String getUri(){
        return getString(getNamespaceReference());
    }
    int getNamespaceReference(){
        return mNamespaceReference.get();
    }
    void setNamespaceReference(int ref){
        mNamespaceReference.set(ref);
    }
    int getNameReference(){
        return mNameReference.get();
    }
    void setNameReference(int ref){
        mNameReference.set(ref);
    }
    int getValueStringReference(){
        return mValueStringReference.get();
    }
    void setValueStringReference(int ref){
        mValueStringReference.set(ref);
    }
    short getNameType(){
        return mNameType.get();
    }
    void setNameType(short s){
        mNameType.set(s);
    }
    byte getValueTypeByte(){
        return mValueTypeByte.get();
    }
    void setValueTypeByte(byte b){
        mValueTypeByte.set(b);
    }
    public int getRawValue(){
        return mRawValue.get();
    }
    public void setRawValue(int val){
        mRawValue.set(val);
    }

    public ResXmlString getNameString(){
        return getResXmlString(getNameReference());
    }
    public ValueType getValueType(){
        return ValueType.valueOf(getValueTypeByte());
    }
    public void setValueType(ValueType valueType){
        if(valueType!=null){
            setValueTypeByte(valueType.getByte());
        }
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
    ResXmlStartNamespace getStartNamespace(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement==null){
            return null;
        }
        return xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
    }
    public String getValueString(){
        return getString(getValueStringReference());
    }
    ResXmlString setValueString(String str){
        ResXmlString resXmlString=getOrCreateResXmlString(str);
        if(resXmlString==null){
            return null;
        }
        int ref=resXmlString.getIndex();
        setValueStringReference(ref);
        if(getValueType()==ValueType.STRING){
            setRawValue(ref);
        }
        return resXmlString;
    }
    public int getNameResourceID(){
        return getResourceId(getNameReference());
    }
    public boolean setName(String name, int resourceId){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool==null){
            return false;
        }
        String old=getName();
        if(resourceId==0){
            if(name.equals(old)){
                return false;
            }
            ResXmlString resXmlString=stringPool.getOrCreate(name);
            setNameReference(resXmlString.getIndex());
            return true;
        }
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        if(xmlIDMap==null){
            return false;
        }
        int oldId=getNameResourceID();
        if(oldId==resourceId){
            if(name.equals(old)){
                return false;
            }
        }
        ResXmlID resXmlID=xmlIDMap.getByResId(resourceId);
        if(resXmlID!=null){
            int ref=resXmlID.getIndex();
            ResXmlString idName=stringPool.get(ref);
            if(idName != null){
                if(name.equals(idName.getHtml())){
                    setNameReference(ref);
                }else {
                    idName.set(name);
                }
                return true;
            }
        }
        int stringsCount=stringPool.countStrings();
        int idCount=xmlIDMap.getResXmlIDArray().childesCount();
        if(idCount>stringsCount){
            xmlIDMap.addResourceId(idCount, resourceId);;
            stringPool.getStringsArray().ensureSize(idCount+1);
            ResXmlString resXmlString=stringPool.get(idCount);
            resXmlString.set(name);
            setNameReference(idCount);
            return true;
        }
        xmlIDMap.addResourceId(stringsCount, resourceId);
        stringPool.getStringsArray().ensureSize(stringsCount+1);
        ResXmlString resXmlString=stringPool.get(stringsCount);
        resXmlString.set(name);
        setNameReference(stringsCount);
        return true;

    }
    private int getResourceId(int ref){
        if(ref<0){
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
    private ResXmlString getOrCreateResXmlString(String str){
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool!=null){
            return stringPool.getOrCreate(str);
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
    public ResXmlElement getParentResXmlElement(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlElement){
                return (ResXmlElement)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public String getValueAsString(){
        int ref=getRawValue();
        ResXmlString xmlString=getResXmlString(ref);
        if(xmlString==null){
            return null;
        }
        return xmlString.getHtml();
    }
    public boolean getValueAsBoolean(){
        int ref=getRawValue();
        return ref!=0;
    }
    public void setValueAsString(String str){
        setValueType(ValueType.STRING);
        ResXmlString xmlString=getOrCreateResXmlString(str);
        if(xmlString==null){
            throw new IllegalStateException("ResXmlString is null, attribute must be added to parent element first");
        }
        int ref=xmlString.getIndex();
        setRawValue(ref);
        setValueStringReference(ref);
    }
    public void setValueAsBoolean(boolean val){
        setValueType(ValueType.INT_BOOLEAN);
        int ref=val?0xffff:0;
        setRawValue(ref);
        setValueStringReference(-1);
    }
    public void setValueAsInteger(int val){
        setValueType(ValueType.FIRST_INT);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsHex(int val){
        setValueType(ValueType.INT_HEX);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsFraction(float fraction){
        int val=Float.floatToIntBits(fraction);
        setValueAsFraction(val);
    }
    public void setValueAsFraction(int val){
        setValueType(ValueType.FRACTION);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsResourceId(int resId){
        setValueType(ValueType.REFERENCE);
        setRawValue(resId);
        setValueStringReference(-1);
    }
    public void setValueAsAttributeId(int attrId){
        setValueType(ValueType.ATTRIBUTE);
        setRawValue(attrId);
        setValueStringReference(-1);
    }
    public void setValueAsColorRGB4(int val){
        setValueType(ValueType.INT_COLOR_RGB4);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsColorRGB8(int val){
        setValueType(ValueType.INT_COLOR_RGB8);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsColorARGB4(int val){
        setValueType(ValueType.INT_COLOR_ARGB4);
        setRawValue(val);
        setValueStringReference(-1);
    }
    public void setValueAsColorARGB8(int val){
        setValueType(ValueType.INT_COLOR_ARGB8);
        setRawValue(val);
        setValueStringReference(-1);
    }

    private String getCompareName(){
        int id=getNameResourceID();
        StringBuilder builder=new StringBuilder();
        if(id!=0){
            builder.append("0 ");
            builder.append(String.format("%08x", id));
        }else {
            builder.append("1 ");
            builder.append(getName());
        }
        return builder.toString();
    }
    @Override
    public int compareTo(ResXmlAttribute other) {
        return getCompareName().compareTo(other.getCompareName());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_name, getName());
        jsonObject.put(NAME_id, getNameResourceID());
        jsonObject.put(NAME_namespace_uri, getUri());
        ValueType valueType=getValueType();
        jsonObject.put(NAME_value_type, valueType.name());
        if(valueType==ValueType.STRING){
            jsonObject.put(NAME_data, getValueAsString());
        }else if(valueType==ValueType.INT_BOOLEAN){
            jsonObject.put(NAME_data, getValueAsBoolean());
        }else {
            jsonObject.put(NAME_data, getRawValue());
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        String name = json.getString(NAME_name);
        int id =  json.optInt(NAME_id, 0);
        setName(name, id);
        String uri= json.optString(NAME_namespace_uri, null);
        if(uri!=null){
            ResXmlStartNamespace ns = getParentResXmlElement().getStartNamespaceByUri(uri);
            setNamespaceReference(ns.getUriReference());
        }
        ValueType valueType=ValueType.fromName(json.getString(NAME_value_type));
        if(valueType==ValueType.STRING){
            setValueAsString(json.getString(NAME_data));
        }else if(valueType==ValueType.INT_BOOLEAN){
            setValueAsBoolean(json.getBoolean(NAME_data));
        }else {
            setValueType(valueType);
            setRawValue(json.getInt(NAME_data));
        }
    }
    @Override
    public String toString(){
        String fullName=getFullName();
        if(fullName!=null ){
            int id=getNameResourceID();
            if(id>0){
                fullName=fullName+"(@"+String.format("0x%08x",id)+")";
            }
            String valStr;
            ValueType valueType=getValueType();
            if(valueType==ValueType.STRING){
                valStr=getValueAsString();
            }else if (valueType==ValueType.INT_BOOLEAN){
                valStr=String.valueOf(getValueAsBoolean());
            }else {
                valStr="["+valueType+"] "+getRawValue();
            }
            if(valStr!=null){
                return fullName+"=\""+valStr+"\"";
            }
            return fullName+"["+valueType+"]=\""+getRawValue()+"\"";
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
    static final String NAME_id = "id";
    public static final String NAME_value_type = "value_type";
    static final String NAME_name = "name";
    public static final String NAME_namespace_uri = "namespace_uri";
    public static final String NAME_data= "data";
}
