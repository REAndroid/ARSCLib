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
 package com.reandroid.arsc.chunk.xml;

 import com.reandroid.arsc.array.ResXmlAttributeArray;
 import com.reandroid.arsc.array.ResXmlIDArray;
 import com.reandroid.arsc.base.Block;
 import com.reandroid.arsc.container.FixedBlockContainer;
 import com.reandroid.arsc.decoder.ValueDecoder;
 import com.reandroid.arsc.group.EntryGroup;
 import com.reandroid.arsc.item.*;
 import com.reandroid.arsc.pool.ResXmlStringPool;
 import com.reandroid.arsc.value.Value;
 import com.reandroid.arsc.value.ValueType;
 import com.reandroid.common.EntryStore;
 import com.reandroid.json.JSONConvert;
 import com.reandroid.json.JSONObject;
 import com.reandroid.xml.XMLAttribute;
 import com.reandroid.xml.XMLException;

 import java.util.HashSet;
 import java.util.Set;

 public class ResXmlAttribute extends FixedBlockContainer
         implements Value, Comparable<ResXmlAttribute>, JSONConvert<JSONObject> {
     private final IntegerItem mNamespaceReference;
     private final IntegerItem mNameReference;
     private final IntegerItem mValueStringReference;
     private final ShortItem mNameType;
     private final ByteItem mReserved;
     private final ByteItem mValueTypeByte;
     private final IntegerItem mData;
     private final ByteArray extraBytes;
     public ResXmlAttribute(int size) {
         super(8);
         mNamespaceReference = new IntegerItem(-1);
         mNameReference = new IntegerItem(-1);
         mValueStringReference = new IntegerItem(-1);
         mNameType = new ShortItem((short) 0x0008);
         mReserved = new ByteItem();
         mValueTypeByte = new ByteItem();
         mData = new IntegerItem();
         extraBytes = new ByteArray();
         addChild(0, mNamespaceReference);
         addChild(1, mNameReference);
         addChild(2, mValueStringReference);
         addChild(3, mNameType);
         addChild(4, mReserved);
         addChild(5, mValueTypeByte);
         addChild(6, mData);
         addChild(7, extraBytes);

         extraBytes.setSize(size-20);
     }
     public ResXmlAttribute(){
         this(20);
     }
     public void setAttributesUnitSize(int size){
         extraBytes.setSize(size-20);
         IntegerItem integerItem = new IntegerItem(this.hashCode());
         extraBytes.putByteArray(0, integerItem.getBytes());
     }

     Set<ResXmlString> clearStringReferences(){
         Set<ResXmlString> results= new HashSet<>();
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
         xmlString=unLinkStringReference(mData);
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
             linkStringReference(mData);
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
     byte getValueTypeByte(){
         return mValueTypeByte.get();
     }
     void setValueTypeByte(byte b){
         mValueTypeByte.set(b);
     }
     @Override
     public int getData(){
         return mData.get();
     }
     @Override
     public void setData(int val){
         mData.set(val);
     }
     @Override
     public ValueType getValueType(){
         return ValueType.valueOf(getValueTypeByte());
     }
     @Override
     public void setValueType(ValueType valueType){
         byte b=0;
         if(valueType!=null){
             b=valueType.getByte();
         }
         setValueTypeByte(b);
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
         int ref= getData();
         ResXmlString xmlString=getResXmlString(ref);
         if(xmlString==null){
             return null;
         }
         return xmlString.getHtml();
     }
     public boolean getValueAsBoolean(){
         int ref= getData();
         return ref!=0;
     }
     public void setValueAsString(String str){
         setValueType(ValueType.STRING);
         ResXmlString xmlString=getOrCreateResXmlString(str);
         if(xmlString==null){
             throw new IllegalStateException("ResXmlString is null, attribute must be added to parent element first");
         }
         int ref=xmlString.getIndex();
         setData(ref);
         setValueStringReference(ref);
     }
     public void setValueAsBoolean(boolean val){
         setValueType(ValueType.INT_BOOLEAN);
         int ref=val?0xffffffff:0;
         setData(ref);
         setValueStringReference(-1);
     }
     public boolean hasIntegerValue(){
         ValueType valueType=getValueType();
         return valueType==ValueType.INT_DEC;
     }
     public Integer getValueAsInteger(){
         if(hasIntegerValue()){
             return getData();
         }
         return null;
     }
     public void setValueAsInteger(int val){
         setValueType(ValueType.INT_DEC);
         setData(val);
         setValueStringReference(-1);
     }
     private ResXmlAttributeArray getParentResXmlAttributeArray(){
         Block parent=this;
         while(parent!=null){
             if(parent instanceof ResXmlAttributeArray){
                 return (ResXmlAttributeArray)parent;
             }
             parent=parent.getParent();
         }
         return null;
     }
     public void setValueAsIntegerDec(int val){
         setValueType(ValueType.INT_DEC);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsHex(int val){
         setValueType(ValueType.INT_HEX);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsFraction(float fraction){
         int val=Float.floatToIntBits(fraction);
         setValueAsFraction(val);
     }
     public void setValueAsFraction(int val){
         setValueType(ValueType.FRACTION);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsResourceId(int resId){
         setValueType(ValueType.REFERENCE);
         setData(resId);
         setValueStringReference(-1);
     }
     public void setValueAsAttributeId(int attrId){
         setValueType(ValueType.ATTRIBUTE);
         setData(attrId);
         setValueStringReference(-1);
     }
     public void setValueAsColorRGB4(int val){
         setValueType(ValueType.INT_COLOR_RGB4);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsColorRGB8(int val){
         setValueType(ValueType.INT_COLOR_RGB8);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsColorARGB4(int val){
         setValueType(ValueType.INT_COLOR_ARGB4);
         setData(val);
         setValueStringReference(-1);
     }
     public void setValueAsColorARGB8(int val){
         setValueType(ValueType.INT_COLOR_ARGB8);
         setData(val);
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
         JSONObject jsonObject= new JSONObject();
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
             jsonObject.put(NAME_data, getData());
         }
         return jsonObject;
     }
     @Override
     public void fromJson(JSONObject json) {
         String name = json.optString(NAME_name, "");
         int id =  json.optInt(NAME_id, 0);
         setName(name, id);
         String uri= json.optString(NAME_namespace_uri, null);
         if(uri!=null){
             ResXmlStartNamespace ns = getParentResXmlElement().getStartNamespaceByUri(uri);
             setNamespaceReference(ns.getUriReference());
         }
         ValueType valueType=ValueType.fromName(json.getString(NAME_value_type));
         if(valueType==ValueType.STRING){
             setValueAsString(json.optString(NAME_data, ""));
         }else if(valueType==ValueType.INT_BOOLEAN){
             setValueAsBoolean(json.getBoolean(NAME_data));
         }else {
             setValueType(valueType);
             setData(json.getInt(NAME_data));
         }
     }
     public XMLAttribute decodeToXml(EntryStore entryStore, int currentPackageId) throws XMLException {
         int resourceId=getNameResourceID();
         String name;
         if(resourceId==0){
             name=getName();
         }else {
             EntryGroup group = entryStore.getEntryGroup(resourceId);
             if(group==null){
                 //Lets ignore such error until XML encoder implemented
                 //throw new XMLException("Failed to decode attribute name: "
                 //+ String.format("@0x%08x", resourceId));
                 name=String.format("@0x%08x", resourceId);
             }else {
                 name=group.getSpecName();
             }
         }
         String prefix = getNamePrefix();
         if(prefix!=null){
             name=prefix+":"+name;
         }
         ValueType valueType=getValueType();
         int raw= getData();
         String value;
         if(valueType==ValueType.STRING){
             value = ValueDecoder.escapeSpecialCharacter(getValueAsString());
         }else {
             value = ValueDecoder.decode(entryStore,
                     currentPackageId,
                     resourceId,
                     valueType,
                     raw);
         }
         XMLAttribute attribute = new XMLAttribute(name, value);
         attribute.setNameId(resourceId);
         if(valueType==ValueType.REFERENCE||valueType==ValueType.ATTRIBUTE){
             attribute.setValueId(raw);
         }
         return attribute;
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
                 valStr="["+valueType+"] "+ getData();
             }
             if(valStr!=null){
                 return fullName+"=\""+valStr+"\"";
             }
             return fullName+"["+valueType+"]=\""+ getData()+"\"";
         }
         StringBuilder builder= new StringBuilder();
         builder.append(getClass().getSimpleName());
         builder.append(": ");
         builder.append(getIndex());
         builder.append("{NamespaceReference=").append(getNamespaceReference());
         builder.append(", NameReference=").append(getNameReference());
         builder.append(", ValueStringReference=").append(getValueStringReference());
         builder.append(", NameType=").append(mNameType.unsignedInt());
         builder.append(", ReservedByte=").append(mReserved.unsignedInt());
         builder.append(", ValueTypeByte=").append(getValueTypeByte());
         builder.append(", RawValue=").append(getData());
         builder.append("}");
         return builder.toString();
     }
     static final String NAME_id = "id";
     public static final String NAME_value_type = "value_type";
     public static final String NAME_name = "name";
     public static final String NAME_namespace_uri = "namespace_uri";
     public static final String NAME_data= "data";
 }
