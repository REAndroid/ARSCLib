package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ReferenceItem;


import java.io.IOException;

public class ResValueInt extends BaseResValueItem  {
    public ResValueInt() {
        super(BYTES_COUNT);
        setHeaderSize(BYTES_SIZE);
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
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
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
