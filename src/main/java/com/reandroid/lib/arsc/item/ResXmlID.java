package com.reandroid.lib.arsc.item;

public class ResXmlID extends IntegerItem {
    public ResXmlID(){
        super();
    }
    public ResXmlID(int resId){
        super(resId);
    }
    @Override
    public String toString(){
        return getIndex()+": "+String.format("0x%08x", get());
    }
}
