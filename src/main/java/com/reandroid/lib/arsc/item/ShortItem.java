package com.reandroid.lib.arsc.item;

public class ShortItem extends BlockItem {
    private short mCache;

    public ShortItem(){
        super(2);
    }
    public ShortItem(short val){
        this();
        set(val);
    }
    public void set(short val){
        if(val==mCache){
            return;
        }
        mCache=val;
        byte[] bts = getBytesInternal();
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    public short get(){
        return mCache;
    }
    @Override
    public void onBytesChanged() {
        mCache=readShortBytes();
    }
    private short readShortBytes(){
        byte[] bts = getBytesInternal();
        return (short) (bts[0] & 0xff | (bts[1] & 0xff) << 8);
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
