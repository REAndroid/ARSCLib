package com.reandroid.lib.arsc.item;

import java.util.List;

public class TableString extends StringItem {
    public TableString(boolean utf8) {
        super(utf8);
    }
    @Override
    public String toString(){
        List<ReferenceItem> refList = getReferencedList();
        return "USED BY="+refList.size()+"{"+super.toString()+"}";
    }
}
