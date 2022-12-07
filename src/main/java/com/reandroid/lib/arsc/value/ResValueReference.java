package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.item.ReferenceItem;

import java.util.Objects;

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
        resValueItem.onSetReference(val);
    }
    @Override
    public int get() {
        return resValueItem.getData();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResValueReference that = (ResValueReference) o;
        return Objects.equals(resValueItem, that.resValueItem);
    }
    @Override
    public int hashCode() {
        return Objects.hash(resValueItem);
    }
}
