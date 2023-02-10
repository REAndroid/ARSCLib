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
import com.reandroid.arsc.value.BaseResValue;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResValueInt;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.util.Iterator;


public class EntryBlockArray extends OffsetBlockArray<EntryBlock> implements JSONConvert<JSONArray> {
    public EntryBlockArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
    }
    public boolean isEmpty(){
        return !iterator(true).hasNext();
    }
    public void setEntry(short entryId, EntryBlock entryBlock){
        setItem(0xffff & entryId, entryBlock);
    }
    public EntryBlock getOrCreate(short entryId){
        int id = 0xffff & entryId;
        EntryBlock entryBlock=get(id);
        if(entryBlock!=null){
            return entryBlock;
        }
        int count=id+1;
        ensureSize(count);
        refreshCount();
        return get(id);
    }
    public EntryBlock get(short entryId){
        int index = 0xffff & entryId;
        return super.get(index);
    }
    public EntryBlock getEntry(short entryId){
        return get(0xffff & entryId);
    }
    @Override
    public EntryBlock newInstance() {
        return new EntryBlock();
    }
    @Override
    public EntryBlock[] newInstance(int len) {
        return new EntryBlock[len];
    }

    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public EntryBlock searchByEntryName(String entryName){
        if(entryName==null){
            return null;
        }
        for(EntryBlock entryBlock:listItems()){
            if(entryName.equals(entryBlock.getName())){
                return entryBlock;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int index=0;
        for(EntryBlock entryBlock:listItems()){
            JSONObject childObject = entryBlock.toJson();
            if(childObject==null){
                continue;
            }
            childObject.put(NAME_id, entryBlock.getIndex());
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
            EntryBlock entryBlock=get(id);
            entryBlock.fromJson(jsonObject);
        }
        refreshCountAndStart();
    }
    public void merge(EntryBlockArray entryBlockArray){
        if(entryBlockArray==null||entryBlockArray==this||entryBlockArray.isEmpty()){
            return;
        }
        ensureSize(entryBlockArray.childesCount());
        Iterator<EntryBlock> itr=entryBlockArray.iterator(true);
        while (itr.hasNext()){
            EntryBlock comingBlock=itr.next();
            EntryBlock existingBlock=get(comingBlock.getIndex());
            existingBlock.merge(comingBlock);
        }
    }
    @Override
    public String toString(){
        return getClass().getSimpleName()+": size="+childesCount();
    }
    private static final String NAME_id="id";
}
