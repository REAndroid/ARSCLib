package com.reandroid.lib.arsc.value;

public interface ResValueItem {
    void setHeaderSize(short size);
    short getHeaderSize();

    void setReserved(byte reserved);
    byte getReserved();

    void setType(byte type);
    void setType(ValueType valueType);
    byte getType();

    int getData();
    void setData(int data);
}
