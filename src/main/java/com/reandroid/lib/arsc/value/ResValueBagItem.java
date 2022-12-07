package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.json.JSONObject;

public class ResValueBagItem extends BaseResValueItem{

    public ResValueBagItem() {
        super(BYTES_COUNT);
        setHeaderSize(BYTES_SIZE);
    }
    public String getValueAsString(){
        return getTableString(getData()).getHtml();
    }
    public void setValueAsString(String str){
        setType(ValueType.STRING);
        TableString tableString=getTableStringPool().getOrCreate(str);
        setData(tableString.getIndex());
    }
    public boolean getValueAsBoolean(){
        return getData()!=0;
    }
    public void setValueAsBoolean(boolean val){
        setType(ValueType.INT_BOOLEAN);
        int data=val?0xffffffff:0;
        setData(data);
    }

    public ResValueBag getParentBag(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof ResValueBag){
                return (ResValueBag) parent;
            }
            parent=parent.getParent();
        }
        return null;
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

    public void setId(int id){
        setInt(OFFSET_ID, id);
    }
    public int getId(){
        return getInt(OFFSET_ID);
    }

    @Override
    public void setType(ValueType valueType){
        byte type=0;
        if(valueType!=null){
            type=valueType.getByte();
        }
        setType(type);
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
            ReferenceItem ref=getTableStringReference();
            removeTableReference(ref);
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
        beforeDataValueChanged();
        setInt(OFFSET_DATA, data);
        afterDataValueChanged();
    }
    @Override
    public void onSetReference(int data){
        setInt(OFFSET_DATA, data);
    }
    private void beforeDataValueChanged(){
        if(getValueType()==ValueType.STRING){
            removeTableReference();
        }
    }
    private void afterDataValueChanged(){
        refreshTableReference();
    }
    void refreshTableReference(){
        if(getValueType()==ValueType.STRING){
            addTableReference(getTableStringReference());
        }else {
            removeTableReference();
        }
    }
    public short getIdHigh(){
        return getShort(OFFSET_ID+2);
    }
    public short getIdLow(){
        return getShort(OFFSET_ID);
    }
    public void setIdHigh(short val){
        setShort(OFFSET_ID+2, val);
    }
    public void setIdLow(short val){
        setShort(OFFSET_ID, val);
    }
    public short getDataHigh(){
        return getShort(OFFSET_DATA+2);
    }
    public short getDataLow(){
        return getShort(OFFSET_DATA);
    }
    public void setDataHigh(short val){
        if(val==getDataHigh()){
            return;
        }
        beforeDataValueChanged();
        setShort(OFFSET_DATA+2, val);
        afterDataValueChanged();
    }
    public void setDataLow(short val){
        if(val==getDataLow()){
            return;
        }
        beforeDataValueChanged();
        setShort(OFFSET_DATA+2, val);
        afterDataValueChanged();
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        ValueType valueType=getValueType();
        jsonObject.put(NAME_value_type, valueType.name());
        jsonObject.put(NAME_id, getId());
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
        setType(valueType);
        setId(json.getInt(NAME_id));
        if(valueType==ValueType.STRING){
            setValueAsString(json.getString(NAME_data));
        }else if(valueType==ValueType.INT_BOOLEAN){
            setValueAsBoolean(json.getBoolean(NAME_data));
        }else {
            setData(json.getInt(NAME_data));
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
        builder.append('(');
        builder.append(String.format("0x%02x", getType()));
        builder.append(") id=");
        builder.append(String.format("0x%08x", getId()));
        builder.append(", data=");
        builder.append(String.format("0x%08x", getData()));
        if(vt==ValueType.STRING){
            TableString ts=getTableString(getData());
            if(ts==null){
                builder.append(" Null table string");
            }else {
                builder.append(" \"").append(ts.getHtml()).append("\"");
            }
        }
        return builder.toString();
    }
    private static final int OFFSET_ID=0;
    private static final int OFFSET_SIZE=4;
    private static final int OFFSET_RESERVED=6;
    private static final int OFFSET_TYPE=7;
    private static final int OFFSET_DATA=8;

    private static final int BYTES_COUNT=12;

    private static final short BYTES_SIZE=0x08;

}
