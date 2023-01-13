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
package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.json.JSONObject;

public class ResValueInt extends BaseResValueItem  {
    public ResValueInt() {
        super(BYTES_COUNT);
        setHeaderSize(BYTES_SIZE);
    }
    protected void onRemoved(){
        removeTableReference();
    }
    public String getValueAsString(){
        return getString(getData());
    }
    public void setValueAsString(String str){
        setType(ValueType.STRING);
        TableString tableString=getTableStringPool().getOrCreate(str);
        setData(tableString.getIndex());
    }
    public void setValueAsBoolean(boolean val){
        setType(ValueType.INT_BOOLEAN);
        int data=val?0xffffffff:0;
        setData(data);
    }
    public boolean getValueAsBoolean(){
        return getData()!=0;
    }
    @Override
    void onDataLoaded() {
        if(getValueType()==ValueType.STRING){
            if(!hasTableReference()){
                addTableReference(getTableStringReference());
            }
        }else {
            removeTableReference();
        }
    }
    @Override
    public void setHeaderSize(short size) {
        setShort(OFFSET_SIZE, size);
    }
    @Override
    public short getHeaderSize() {
        return getShort(OFFSET_SIZE);
    }
    @Override
    public void setReserved(byte reserved) {
        setByte(OFFSET_RESERVED, reserved);
    }
    @Override
    public byte getReserved() {
        return getByte(OFFSET_RESERVED);
    }
    @Override
    public void setType(byte type){
        byte old=getType();
        if(type==old){
            return;
        }
        removeTableReference();
        setByte(OFFSET_TYPE, type);
        if(type==ValueType.STRING.getByte()){
            addTableReference(getTableStringReference());
        }
    }
    @Override
    public byte getType(){
        return getByte(OFFSET_TYPE);
    }
    @Override
    public int getData(){
        return getInt(OFFSET_DATA);
    }
    @Override
    public void setData(int data){
        int old=getData();
        if(data==old){
            return;
        }
        removeTableReference();
        setInt(OFFSET_DATA, data);
        if(getValueType()==ValueType.STRING){
            addTableReference(getTableStringReference());
        }
    }
    @Override
    public void onSetReference(int data){
        setInt(OFFSET_DATA, data);
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
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
        ValueType valueType=ValueType.fromName(json.getString(NAME_value_type));
        if(valueType==ValueType.STRING){
            setValueAsString(json.optString(NAME_data, ""));
        }else if(valueType==ValueType.INT_BOOLEAN){
            setValueAsBoolean(json.getBoolean(NAME_data));
        }else {
            setType(valueType);
            setData(json.getInt(NAME_data));
        }
    }
    public void merge(ResValueInt resValueInt){
        if(resValueInt==null||resValueInt==this){
            return;
        }
        ValueType valueType=resValueInt.getValueType();
        if(valueType==ValueType.STRING){
            setValueAsString(resValueInt.getValueAsString());
        }else {
            setType(valueType);
            setData(resValueInt.getData());
        }
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" type=");
        ValueType vt=getValueType();
        if(vt!=null){
            builder.append(vt.name());
        }else {
            builder.append("Unknown");
        }
        String data=null;
        if(vt==ValueType.STRING){
            data=getString(getData());
        }else{
            data= ValueDecoder.decode(vt, getData());
        }
        if(data==null){
            data=String.format("0x%08x", getData());
        }
        builder.append('(');
        builder.append(String.format("0x%02x", getType()));
        builder.append("), data=");
        builder.append(data);
        return builder.toString();
    }

    private static final int OFFSET_SIZE=0;
    private static final int OFFSET_RESERVED=2;
    private static final int OFFSET_TYPE=3;
    private static final int OFFSET_DATA=4;

    private static final int BYTES_COUNT=8;
    private static final short BYTES_SIZE=0x08;

}
