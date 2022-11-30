package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueReference;

import java.util.ArrayList;
import java.util.List;

public class TableString extends StringItem {
    public TableString(boolean utf8) {
        super(utf8);
    }
    public List<EntryBlock> listReferencedEntries(){
        List<EntryBlock> results=new ArrayList<>();
        for(ReferenceItem ref:getReferencedList()){
            if(!(ref instanceof ResValueReference)){
                continue;
            }
            EntryBlock entryBlock=((ResValueReference)ref).getEntryBlock();
            if(entryBlock==null){
                continue;
            }
            results.add(entryBlock);
        }
        return results;
    }
    @Override
    public String toString(){
        List<ReferenceItem> refList = getReferencedList();
        return "USED BY="+refList.size()+"{"+super.toString()+"}";
    }
}
