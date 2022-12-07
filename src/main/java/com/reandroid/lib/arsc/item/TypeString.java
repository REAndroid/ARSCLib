package com.reandroid.lib.arsc.item;


public class TypeString extends StringItem {
    public TypeString(boolean utf8) {
        super(utf8);
    }
    public byte getId(){
        return (byte) (getIndex()+1);
    }
    @Override
    public StyleItem getStyle(){
        // Type don't have style unless to obfuscate/confuse other decompilers
        return null;
    }
}
