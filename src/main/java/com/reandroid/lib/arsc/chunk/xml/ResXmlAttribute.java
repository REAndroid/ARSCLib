 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

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
        mNameReference =new IntegerItem(-1);
        mValueStringReference =new IntegerItem(-1);
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
     Set<ResXmlString> clearStringReferences(){
         Set<ResXmlString> results=new HashSet<>();
         ResXmlString xmlString;
         xmlString=unLinkStringReference(mNamespaceReference);
         if(xmlString!=null){
             results.add(xmlString);
         }
         xmlString=unLinkStringReference(mNameReference);
         if(xmlString!=null){
             results.add(xmlString);
         }
         xmlString=unLinkStringReference(mValueStringReference);
         if(xmlString!=null){
             results.add(xmlString);
         }
         xmlString=unLinkStringReference(mRawValue);
         if(xmlString!=null){
             results.add(xmlString);
         }
         return results;
     }
    public void linkStringReferences(){
        linkStringReference(mNamespaceReference);
        linkStringReference(mNameReference);
        linkStringReference(mValueStringReference);
        if(getValueType()==ValueType.STRING){
            linkStringReference(mRawValue);
        }
    }
    private void linkStringReference(IntegerItem item){
        ResXmlString xmlString = getResXmlString(item.get());
        if(xmlString!=null){
            xmlString.addReferenceIfAbsent(item);
        }
    }
    private ResXmlString unLinkStringReference(IntegerItem item){
        ResXmlString xmlString = getResXmlString(item.get());
        if(xmlString!=null){
            xmlString.removeReference(item);
        }
        return xmlString;
    }
    public String getUri(){
        return getString(getNamespaceReference());
    }
    int getNamespaceReference(){
        return mNamespaceReference.get();
    }
    public void setNamespaceReference(int ref){
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
    public ResXmlStartNamespace getStartNamespace(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement==null){
            return null;
        }
        return xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
    }
    @Deprecated
    public String getValueString(){
        return getString(getValueStringReference());
    }
    public int getNameResourceID(){
        return getResourceId(getNameReference());
    }
    public void setNameResourceID(int resourceId){
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        if(xmlIDMap==null){
            return;
        }
        ResXmlID xmlID = xmlIDMap.getOrCreate(resourceId);
        setNameReference(xmlID.getIndex());
    }
    public void setName(String name, int resourceId){
        if(resourceId!=0){
            setNameResourceID(resourceId);
            return;
        }
        if(name==null){
            name="";
        }
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        ResXmlStringPool stringPool=getStringPool();
        if(stringPool==null || xmlIDMap==null){
            return;
        }
        ResXmlString xmlString = stringPool.getOrCreateAttributeName(xmlIDMap.getResXmlIDArray().childesCount(), name);
        setNameReference(xmlString.getIndex());
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
        int ref=val?0xffffffff:0;
        setRawValue(ref);
        setValueStringReference(-1);
    }
    public void setValueAsIntegerDec(int val){
        setValueType(ValueType.INT_DEC);
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
    @Override
    public int compareTo(ResXmlAttribute other) {
        int id1=getNameResourceID();
        int id2=other.getNameResourceID();
        if(id1==0 && id2!=0){
            return 1;
        }
        if(id2==0 && id1!=0){
            return -1;
        }
        if(id1!=0){
            return Integer.compare(id1, id2);
        }
        String name1=getName();
        if(name1==null){
            name1="";
        }
        String name2=other.getName();
        if(name2==null){
            name2="";
        }
        return name1.compareTo(name2);
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
    public static final String NAME_name = "name";
    public static final String NAME_namespace_uri = "namespace_uri";
    public static final String NAME_data= "data";
}
