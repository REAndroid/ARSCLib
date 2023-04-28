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
package com.reandroid.arsc.value;

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.json.JSONObject;

public class ResTableMapEntry extends TableEntry<EntryHeaderMap, ResValueMapArray> {
    public ResTableMapEntry(){
        super(new EntryHeaderMap(), new ResValueMapArray());
    }
    public void refresh(){
        getHeader().setValuesCount(getValue().childesCount());
    }
    public ResValueMap[] listResValueMap(){
        return getValue().getChildes();
    }
    public int getParentId(){
        return getHeader().getParentId();
    }
    public void setParentId(int parentId){
        getHeader().setParentId(parentId);
    }
    public int getValuesCount(){
        return getHeader().getValuesCount();
    }
    public void setValuesCount(int valuesCount){
        getHeader().setValuesCount(valuesCount);
        getValue().setChildesCount(valuesCount);
    }
    @Override
    void linkTableStringsInternal(TableStringPool tableStringPool){
        for(ResValueMap resValueMap : listResValueMap()){
            resValueMap.linkTableStrings(tableStringPool);
        }
    }
    @Override
    void onHeaderLoaded(ValueHeader valueHeader){
        getValue().setChildesCount(getValuesCount());
    }

    @Override
    void onRemoved(){
        getHeader().onRemoved();
        getValue().onRemoved();
    }
    @Override
    boolean shouldMerge(TableEntry<?, ?> tableEntry){
        if(tableEntry == this || !(tableEntry instanceof ResTableMapEntry)){
            return false;
        }
        ResValueMapArray coming = ((ResTableMapEntry) tableEntry).getValue();
        if(coming.childesCount() == 0){
            return false;
        }
        return getValue().childesCount() == 0;
    }

    @Override
    public void merge(TableEntry<?, ?> tableEntry){
        if(tableEntry==null || tableEntry==this){
            return;
        }
        ResTableMapEntry coming = (ResTableMapEntry) tableEntry;
        getHeader().merge(coming.getHeader());
        getValue().merge(coming.getValue());
        refresh();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        getHeader().toJson(jsonObject);
        jsonObject.put(NAME_values, getValue().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getHeader().fromJson(json);
        getValue().fromJson(json.optJSONArray(NAME_values));
        refresh();
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getHeader());
        ResValueMap[] valueMaps = listResValueMap();
        int len = valueMaps.length;
        int max = len;
        if(max>4){
            max = 4;
        }
        for(int i=0;i<max;i++){
            builder.append("\n    ");
            builder.append(valueMaps[i]);
        }
        if(len>0){
            if(max!=len){
                builder.append("\n    ...");
            }
            builder.append("\n   ");
        }
        return builder.toString();
    }

    public static final String NAME_values = "values";
}
