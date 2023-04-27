/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.array;

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public abstract class StringArray<T extends StringItem> extends OffsetBlockArray<T> implements JSONConvert<JSONArray> {
    private boolean mUtf8;

    public StringArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart);
        this.mUtf8=is_utf8;
        setEndBytes((byte)0x00);
    }
    public List<String> toStringList(){
        return new AbstractList<String>() {
            @Override
            public String get(int i) {
                T item=StringArray.this.get(i);
                if(item==null){
                    return null;
                }
                return item.getHtml();
            }
            @Override
            public int size() {
                return childesCount();
            }
        };
    }
    public List<T> removeUnusedStrings(){
        List<T> unusedList = listUnusedStringsToRemove();
        remove(unusedList);
        for(T item:unusedList){
            item.onRemoved();
        }
        return unusedList;
    }
    List<T> listUnusedStringsToRemove(){
        return listUnusedStrings();
    }
    public List<T> listUnusedStrings(){
        List<T> results=new ArrayList<>();
        for(T item:listItems()){
            if(!item.hasReference()){
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
    // Only styled strings
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
    // Only styled strings
    @Override
    public void fromJson(JSONArray json) {
        throw new IllegalArgumentException(getClass().getSimpleName()+".fromJson() NOT implemented");
    }
}
