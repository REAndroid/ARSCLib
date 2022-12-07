package com.reandroid.lib.arsc.item;

public class SpecString extends StringItem {
    public SpecString(boolean utf8) {
        super(utf8);
    }
    @Override
    public StyleItem getStyle(){
        // Spec (resource name) don't have style unless to obfuscate/confuse other decompilers
        return null;
    }
}
