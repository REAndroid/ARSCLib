package com.reandroid.lib.arsc.value.attribute;

public enum AttributeItemType {
    FORMAT((short)0x0000),
    MIN((short)0x0001),
    MAX((short)0x0002),
    L10N((short)0x0003);
    private final short mValue;
    AttributeItemType(short value){
        this.mValue=value;
    }
    public short getValue() {
        return mValue;
    }
    @Override
    public String toString(){
        return name().toLowerCase();
    }
    public static AttributeItemType valueOf(short value){
        AttributeItemType[] all=values();
        for(AttributeItemType bagType:all){
            if(bagType.mValue==value){
                return bagType;
            }
        }
        return null;
    }
    public static AttributeItemType fromName(String name){
        if(name==null){
            return null;
        }
        name=name.toUpperCase();
        AttributeItemType[] all=values();
        for(AttributeItemType bagType:all){
            if(name.equals(bagType.name())){
                return bagType;
            }
        }
        return null;
    }
}
