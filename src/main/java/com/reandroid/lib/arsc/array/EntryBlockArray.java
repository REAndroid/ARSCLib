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
package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.BaseResValue;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueInt;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.util.Iterator;


public class EntryBlockArray extends OffsetBlockArray<EntryBlock> implements JSONConvert<JSONArray> {
    public EntryBlockArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super(offsets, itemCount, itemStart);
    }
    public boolean isEmpty(){
        return !iterator(true).hasNext();
    }
    public void setEntry(short entryId, EntryBlock entryBlock){
        setItem(entryId, entryBlock);
    }
    public EntryBlock getOrCreate(short entryId){
        EntryBlock entryBlock=get(entryId);
        if(entryBlock!=null){
            return entryBlock;
        }
        int count=entryId+1;
        ensureSize(count);
        refreshCount();
        return get(entryId);
    }
    public EntryBlock getEntry(short entryId){
        return get(entryId);
    }
    @Override
    public EntryBlock newInstance() {
        return new EntryBlock();
    }

    @Override
    public EntryBlock[] newInstance(int len) {
        return new EntryBlock[len];
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
            if(shouldMerge(existingBlock, comingBlock)){
                existingBlock.merge(comingBlock);
            }
        }
    }
    private boolean shouldMerge(EntryBlock exist, EntryBlock coming){
        if(exist.isNull()){
            return true;
        }
        if(coming.isNull()){
            return false;
        }
        BaseResValue resVal = coming.getResValue();
        if(resVal instanceof ResValueInt){
            ValueType valueType=((ResValueInt)resVal).getValueType();
            return valueType!=ValueType.INT_BOOLEAN;
        }
        return true;
    }
    @Override
    public String toString(){
        return getClass().getSimpleName()+": size="+childesCount();
    }
    private static final String NAME_id="id";
}
