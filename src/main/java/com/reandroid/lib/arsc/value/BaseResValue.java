package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.item.BlockItem;

public abstract class BaseResValue extends BlockItem {
    BaseResValue(int bytesLength){
        super(bytesLength);
    }

    public ValueType getValueType(){
        return ValueType.valueOf(getType());
    }
    public EntryBlock getEntryBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof EntryBlock){
                return (EntryBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }

    public void setType(ValueType valueType){
        byte type=0;
        if(valueType!=null){
            type=valueType.getByte();
        }
        setType(type);
    }

    abstract void setHeaderSize(short size);
    abstract short getHeaderSize();

    abstract void setReserved(byte reserved);
    abstract byte getReserved();

    public abstract void setType(byte type);
    public abstract byte getType();
    public abstract int getData();
    public abstract void setData(int data);

    @Override
    public void onBytesChanged() {

    }


    int getInt(int offset){
        byte[] bts = getBytesInternal();
        return bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 |
                (bts[offset+2] & 0xff) << 16 |
                (bts[offset+3] & 0xff) << 24;
    }
    void setInt(int offset, int val){
        if(val==getInt(offset)){
            return;
        }
        byte[] bts = getBytesInternal();
        bts[offset+3]= (byte) (val >>> 24 & 0xff);
        bts[offset+2]= (byte) (val >>> 16 & 0xff);
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
        onBytesChanged();
    }

    void setShort(int offset, short val){
        if(val==getShort(offset)){
            return;
        }
        byte[] bts = getBytesInternal();
        bts[offset+1]= (byte) (val >>> 8 & 255);
        bts[offset]= (byte) (val & 255);
        onBytesChanged();
    }
    short getShort(int offset){
        byte[] bts=getBytesInternal();
        int i= bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 ;
        return (short)i;
    }
    void setByte(int offset, byte b){
        byte[] bts=getBytesInternal();
        if(b==bts[offset]){
            return;
        }
        bts[offset]=b;
        onBytesChanged();
    }
    byte getByte(int offset){
        return getBytesInternal()[offset];
    }
}
