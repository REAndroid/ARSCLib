package com.reandroid.lib.arsc.item;


public class ByteItem extends BlockItem {
    public ByteItem() {
        super(1);
    }
    public boolean getBit(int index){
        return ((get()>>index) & 0x1) == 1;
    }
    public void putBit(int index, boolean bit){
        int val=get();
        int left=val>>index;
        if(bit){
            left=left|0x1;
        }else {
            left=left & 0xFE;
        }
        left=left<<index;
        index=8-index;
        int right=(0xFF>>index) & val;
        val=left|right;
        set((byte) val);
    }
    public void set(byte b){
        getBytesInternal()[0]=b;
    }
    public byte get(){
        return getBytesInternal()[0];
    }
    @Override
    public void onBytesChanged() {
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
