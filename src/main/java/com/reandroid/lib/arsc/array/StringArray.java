package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.StringItem;
import com.reandroid.lib.json.JsonItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class StringArray<T extends StringItem> extends OffsetBlockArray<T> implements JsonItem<JSONArray> {
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
    @Override
    public JSONArray toJson() {
        return toJson(true);
    }
    public JSONArray toJson(boolean styledOnly) {
        if(childesCount()==0){
            return null;
        }
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(T item:listItems()){
            if(item.isNull()){
                continue;
            }
            if(styledOnly && !item.hasStyle()){
                continue;
            }
            JSONObject jsonObject= item.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        if(i==0){
            return null;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clearChildes();
        if(json==null){
            return;
        }
        int length = json.length();
        ensureSize(length);
        for(int i=0; i<length;i++){
            T item=get(i);
            JSONObject jsonObject = json.getJSONObject(i);
            item.fromJson(jsonObject);
        }
    }

}
