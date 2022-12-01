package com.reandroid.lib.arsc.item;

public class ResXmlString extends StringItem {
    public ResXmlString(boolean utf8) {
        super(utf8);
    }
    public ResXmlString(boolean utf8, String value) {
        this(utf8);
        set(value);
    }
}
