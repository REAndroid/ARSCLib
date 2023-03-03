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

import com.reandroid.arsc.item.IntegerArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.value.Entry;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.util.Iterator;


public class EntryArray extends OffsetBlockArray<Entry> implements JSONConvert<JSONArray> {
    public EntryArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
    }
    public boolean hasComplexEntry(){
        Entry first = iterator(true).next();
        if(first==null){
            return false;
        }
        return first.isComplex();
    }
    public boolean isEmpty(){
        return !iterator(true).hasNext();
    }
    public void setEntry(short entryId, Entry entry){
        setItem(0xffff & entryId, entry);
    }
    public Entry getOrCreate(short entryId){
        int id = 0xffff & entryId;
        Entry entry =get(id);
        if(entry !=null){
            return entry;
        }
        int count=id+1;
        ensureSize(count);
        refreshCount();
        return get(id);
    }
    public Entry get(short entryId){
        int index = 0xffff & entryId;
        return super.get(index);
    }
    public Entry getEntry(short entryId){
        return get(0xffff & entryId);
    }
    @Override
    public Entry newInstance() {
        return new Entry();
    }
    @Override
    public Entry[] newInstance(int len) {
        return new Entry[len];
    }

    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public Entry searchByEntryName(String entryName){
        if(entryName==null){
            return null;
        }
        for(Entry entry:listItems()){
            if(entryName.equals(entry.getName())){
                return entry;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int index=0;
        for(Entry entry :listItems()){
            JSONObject childObject = entry.toJson();
            if(childObject==null){
                continue;
            }
            childObject.put(NAME_id, entry.getIndex());
            jsonArray.put(index, childObject);
            index++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clearChildes();
        int length=json.length();
        ensureSize(length);
        for(int i=0;i<length;i++){
            JSONObject jsonObject= json.getJSONObject(i);
            if(jsonObject==null){
                continue;
            }
            int id=jsonObject.getInt(NAME_id);
            ensureSize(id+1);
            Entry entry =get(id);
            entry.fromJson(jsonObject);
        }
        refreshCountAndStart();
    }
    public void merge(EntryArray entryArray){
        if(entryArray ==null|| entryArray ==this|| entryArray.isEmpty()){
            return;
        }
        ensureSize(entryArray.childesCount());
        Iterator<Entry> itr = entryArray.iterator(true);
        while (itr.hasNext()){
            Entry comingBlock = itr.next();
            Entry existingBlock = get(comingBlock.getIndex());
            existingBlock.merge(comingBlock);
        }
    }
    @Override
    public String toString(){
        return getClass().getSimpleName()+": size="+childesCount();
    }
    private static final String NAME_id="id";
}
