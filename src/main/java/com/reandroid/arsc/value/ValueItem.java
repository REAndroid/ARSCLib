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
package com.reandroid.arsc.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.MainChunk;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.ReferenceBlock;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;

 public abstract class ValueItem extends BlockItem implements Value,
         JSONConvert<JSONObject>{
     private ReferenceItem mStringReference;
     public ValueItem(int bytesLength) {
         super(bytesLength);
         writeSize();
     }
     public void onRemoved(){
         unLinkStringReference();
     }
     public Entry getParentEntry(){
         Block parent=getParent();
         while (parent!=null){
             if(parent instanceof Entry){
                 return (Entry) parent;
             }
             parent = parent.getParent();
         }
         return null;
     }
     public void refresh(){
         writeSize();
     }
     public ReferenceItem getTableStringReference(){
         return mStringReference;
     }

     abstract int getSizeOffset();

     byte getRes0(){
         return getBytesInternal()[getSizeOffset()+OFFSET_RES0];
     }
     public byte getType(){
         return getBytesInternal()[getSizeOffset()+OFFSET_TYPE];
     }
     public void setType(byte type){
         if(type == getType()){
             return;
         }
         byte[] bts = getBytesInternal();
         int offset = getSizeOffset()+OFFSET_TYPE;
         byte old = bts[offset];
         bts[offset] = type;
         onTypeChanged(old, type);
     }
     public int getSize(){
         return 0xffff & getShort(getBytesInternal(), getSizeOffset()+OFFSET_SIZE);
     }
     public void setSize(int size){
         size = getSizeOffset() + size;
         setBytesLength(size, false);
         writeSize();
     }
     private void writeSize(){
         int offset = getSizeOffset();
         int size = countBytes() - offset;
         putShort(getBytesInternal(), offset + OFFSET_SIZE, (short) size);
     }
     private void onDataLoaded(){
         if(getValueType() == ValueType.STRING){
             linkStringReference();
         }else {
             unLinkStringReference();
         }
     }
     @Override
     public ValueType getValueType(){
         return ValueType.valueOf(getType());
     }
     @Override
     public void setValueType(ValueType valueType){
         byte type = 0;
         if(valueType!=null){
             type = valueType.getByte();
         }
         setType(type);
     }
     @Override
     public int getData(){
         return getInteger(getBytesInternal(), getSizeOffset()+OFFSET_DATA);
     }
     @Override
     public void setData(int data){
         byte[] bts = getBytesInternal();
         int old = getInteger(bts, getSizeOffset()+OFFSET_DATA);
         if(old == data){
             return;
         }
         unLinkStringReference();
         putInteger(bts, getSizeOffset()+OFFSET_DATA, data);
         if(ValueType.STRING==getValueType()){
             linkStringReference();
         }
     }


     public StringItem getDataAsPoolString(){
         StringPool<?> stringPool = getStringPool();
         if(stringPool == null){
             return null;
         }
         return stringPool.get(getData());
     }
     private void onTypeChanged(byte old, byte type){
         byte typeString = ValueType.STRING.getByte();
         if(old == typeString){
             unLinkStringReference();
         }else if(type == typeString){
             linkStringReference();
         }
     }
     private void linkStringReference(){
         StringItem tableString = getDataAsPoolString();
         if(tableString==null){
             return;
         }
         ReferenceItem stringReference = mStringReference;
         if(stringReference!=null){
             unLinkStringReference();
         }
         stringReference = new ReferenceBlock<>(this, getSizeOffset()+OFFSET_DATA);
         mStringReference = stringReference;
         tableString.addReference(stringReference);
     }
     private void unLinkStringReference(){
         ReferenceItem stringReference = mStringReference;
         if(stringReference==null){
             return;
         }
         mStringReference = null;
         StringPool<?> stringPool = getStringPool();
         if(stringPool == null){
             return;
         }
         stringPool.removeReference(stringReference);
     }
     private StringPool<?> getStringPool(){
         Block parent = getParent();
         while (parent!=null){
             if(parent instanceof MainChunk){
                 return ((MainChunk) parent).getStringPool();
             }
             parent=parent.getParent();
         }
         return null;
     }
     @Override
     public void onReadBytes(BlockReader reader) throws IOException {
         initializeBytes(reader);
         super.onReadBytes(reader);
         onDataLoaded();
     }
     private void initializeBytes(BlockReader reader) throws IOException {
         int position = reader.getPosition();
         int offset = getSizeOffset();
         reader.offset(offset);
         int size = reader.readUnsignedShort();
         reader.seek(position);
         setBytesLength(offset + size, false);
     }
     public String getValueAsString(){
         StringItem stringItem = getDataAsPoolString();
         if(stringItem!=null){
             return stringItem.getHtml();
         }
         return null;
     }
     public void setValueAsString(String str){
         StringItem stringItem = getStringPool().getOrCreate(str);
         setData(stringItem.getIndex());
         setValueType(ValueType.STRING);
     }
     public boolean getValueAsBoolean(){
         return getData()!=0;
     }
     public void setValueAsBoolean(boolean val){
         setValueType(ValueType.INT_BOOLEAN);
         int data=val?0xffffffff:0;
         setData(data);
     }
     public void setTypeAndData(ValueType valueType, int data){
         setData(data);
         setValueType(valueType);
     }
     public void merge(ValueItem valueItem){
         if(valueItem == null || valueItem==this){
             return;
         }
         setSize(valueItem.getSize());
         ValueType coming = valueItem.getValueType();
         if(coming == ValueType.STRING){
             setValueAsString(valueItem.getValueAsString());
         }else {
             setTypeAndData(coming, valueItem.getData());
         }
     }
     @Override
     public JSONObject toJson() {
         if(isNull()){
             return null;
         }
         JSONObject jsonObject = new JSONObject();
         ValueType valueType = getValueType();
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
         ValueType valueType = ValueType.fromName(json.getString(NAME_value_type));
         if(valueType==ValueType.STRING){
             setValueAsString(json.optString(NAME_data, ""));
         }else if(valueType==ValueType.INT_BOOLEAN){
             setValueAsBoolean(json.getBoolean(NAME_data));
         }else {
             setValueType(valueType);
             setData(json.getInt(NAME_data));
         }
     }

     @Override
     public String toString(){
         StringBuilder builder = new StringBuilder();
         int size = getSize();
         if(size!=8){
             builder.append("size=").append(getSize());
             builder.append(", ");
         }
         builder.append("type=");
         ValueType valueType=getValueType();
         if(valueType!=null){
             builder.append(valueType);
         }else {
             builder.append(String.format("0x%02x", (0xff & getType())));
         }
         builder.append(", data=");
         int data = getData();
         if(valueType==ValueType.STRING){
             StringItem tableString = getDataAsPoolString();
             if(tableString!=null){
                 builder.append(tableString.getHtml());
             }else {
                 builder.append(String.format("0x%08x", data));
             }
         }else {
             builder.append(String.format("0x%08x", data));
         }
         return builder.toString();
     }

     private static final int OFFSET_SIZE = 0;
     private static final int OFFSET_RES0 = 2;
     private static final int OFFSET_TYPE = 3;
     private static final int OFFSET_DATA = 4;


     static final String NAME_data = "data";
     static final String NAME_value_type = "value_type";
 }
