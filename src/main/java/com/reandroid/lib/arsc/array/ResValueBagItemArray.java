package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;

public class ResValueBagItemArray extends BlockArray<ResValueBagItem> implements JSONConvert<JSONArray> {
    public ResValueBagItemArray(){
        super();
    }
    @Override
    public ResValueBagItem newInstance() {
        return new ResValueBagItem();
    }

    @Override
    public ResValueBagItem[] newInstance(int len) {
        return new ResValueBagItem[len];
    }

    @Override
    protected void onRefreshed() {

    }
    @Override
    public void clearChildes(){
        for(ResValueBagItem bagItem:listItems()){
            bagItem.onRemoved();
        }
        super.clearChildes();
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        if(isNull()){
            return jsonArray;
        }
        ResValueBagItem[] childes = getChildes();
        for(int i=0;i<childes.length;i++){
            jsonArray.put(i, childes[i].toJson());
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json){
        clearChildes();
        if(json==null){
            return;
        }
        int count=json.length();
        ensureSize(count);
        for(int i=0;i<count;i++){
            get(i).fromJson(json.getJSONObject(i));
        }
    }
    public void merge(ResValueBagItemArray bagItemArray){
        if(bagItemArray==null||bagItemArray==this){
            return;
        }
        clearChildes();
        int count=bagItemArray.childesCount();
        ensureSize(count);
        for(int i=0;i<count;i++){
            ResValueBagItem coming=bagItemArray.get(i);
            ResValueBagItem exist=get(i);
            exist.merge(coming);
        }
    }
}
