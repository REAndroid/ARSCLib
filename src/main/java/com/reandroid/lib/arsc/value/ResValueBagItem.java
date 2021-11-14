package com.reandroid.lib.arsc.value;

public class ResValueBagItem extends BaseResValue {

    public ResValueBagItem() {
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


    public void setId(int id){
        setInt(OFFSET_ID, id);
    }
    public int getId(){
        return getInt(OFFSET_ID);
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
