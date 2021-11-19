package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.StringItem;

import java.util.ArrayList;
import java.util.List;

public abstract class StringArray<T extends StringItem> extends OffsetBlockArray<T>{
    private boolean mUtf8;
    public StringArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart);
        this.mUtf8=is_utf8;
        setEndBytes((byte)0x00);
    }
    public List<T> removeUnusedStrings(){
        List<T> allUnused=listUnusedStrings();
        remove(allUnused);
        return allUnused;
    }
    public List<T> listUnusedStrings(){
        List<T> results=new ArrayList<>();
        for(T item:listItems()){
            if(item.getReferencedList().size()==0){
                results.add(item);
            }
        }
        return results;
    }
    public void setUtf8(boolean is_utf8){
        if(mUtf8==is_utf8){
            return;
        }
        mUtf8=is_utf8;
        T[] childes=getChildes();
        if(childes!=null){
            int max=childes.length;
            for(int i=0;i<max;i++){
                childes[i].setUtf8(is_utf8);
            }
        }
    }
    public boolean isUtf8() {
        return mUtf8;
    }

    @Override
    protected void refreshChildes(){
        // Not required
    }

}
