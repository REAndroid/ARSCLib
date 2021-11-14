package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.io.BlockReader;


import java.io.IOException;

public class ResValueInt extends BaseResValue {
    public ResValueInt() {
        super(BYTES_COUNT);
        setHeaderSize(BYTES_SIZE);
    }
    @Override
    void setHeaderSize(short size) {
        setShort(OFFSET_SIZE, size);
    }
    @Override
    short getHeaderSize() {
        return getShort(OFFSET_SIZE);
    }
    @Override
    void setReserved(byte reserved) {
        setByte(OFFSET_RESERVED, reserved);
    }
    @Override
    byte getReserved() {
        return getByte(OFFSET_RESERVED);
    }
    @Override
    public void setType(byte type){
        setByte(OFFSET_TYPE, type);
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
        setInt(OFFSET_DATA, data);
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
        builder.append('(');
        builder.append(String.format("0x%02x", getType()));
        builder.append("), data=");
        builder.append(String.format("0x%08x", getData()));
        return builder.toString();
    }

    private static final int OFFSET_SIZE=0;
    private static final int OFFSET_RESERVED=2;
    private static final int OFFSET_TYPE=3;
    private static final int OFFSET_DATA=4;

    private static final int BYTES_COUNT=8;

    private static final short BYTES_SIZE=0x08;
}
