package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.item.ReferenceItem;

public class ResValueReference implements ReferenceItem {
    private final BaseResValueItem resValueItem;
    public ResValueReference(BaseResValueItem resValueItem){
        this.resValueItem=resValueItem;
    }
    public EntryBlock getEntryBlock(){
        return resValueItem.getEntryBlock();
    }
    @Override
    public void set(int val) {
        resValueItem.setData(val);
    }
    @Override
    public int get() {
        return resValueItem.getData();
    }
}
