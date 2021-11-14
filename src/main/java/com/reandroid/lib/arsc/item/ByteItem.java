package com.reandroid.lib.arsc.item;


public class ByteItem extends BlockItem {
    public ByteItem() {
        super(1);
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
