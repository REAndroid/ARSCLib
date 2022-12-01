package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;


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
    @Override
    public String toString(){
        return toJson().toString(4);
    }
    private static final String NAME_id="id";
}
