package com.reandroid.lib.arsc.item;

import java.util.List;

public class ResXmlString extends StringItem {
    public ResXmlString(boolean utf8) {
        super(utf8);
    }
    public ResXmlString(boolean utf8, String value) {
        this(utf8);
        set(value);
    }
    @Override
    public String toString(){
        List<ReferenceItem> refList = getReferencedList();
        return "USED BY="+refList.size()+"{"+super.toString()+"}";
    }
}
