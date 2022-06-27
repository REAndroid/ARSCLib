package com.reandroid.lib.arsc.item;


public class IntegerItem extends BlockItem implements ReferenceItem{
    private int mCache;
    public IntegerItem(){
        super(4);
    }
    public IntegerItem(int val){
        this();
        set(val);
    }
    @Override
    public void set(int val){
        if(val==mCache){
            return;
        }
        mCache=val;
        byte[] bts = getBytesInternal();
        bts[3]= (byte) (val >>> 24 & 0xff);
        bts[2]= (byte) (val >>> 16 & 0xff);
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    @Override
    public int get(){
        return mCache;
    }



    @Override
    public void onBytesChanged() {
        mCache=readIntBytes();
    }
    private int readIntBytes(){
        byte[] bts = getBytesInternal();
        return bts[0] & 0xff |
                (bts[1] & 0xff) << 8 |
                (bts[2] & 0xff) << 16 |
                (bts[3] & 0xff) << 24;
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }

}
