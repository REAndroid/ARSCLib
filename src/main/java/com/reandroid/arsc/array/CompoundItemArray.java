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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.value.AttributeType;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;

public abstract class CompoundItemArray<T extends ResValueMap> extends BlockArray<T> implements JSONConvert<JSONArray> {
    public CompoundItemArray(){
        super();
    }
    public AttributeDataFormat[] getFormats(){
        ResValueMap formatsMap = getByType(AttributeType.FORMATS);
        if(formatsMap != null){
            return AttributeDataFormat.decodeValueTypes(formatsMap.getData());
        }
        return null;
    }
    public boolean containsType(AttributeType attributeType){
        for(T valueMap : getChildes()){
            if(attributeType == valueMap.getAttributeType()){
                return true;
            }
        }
        return false;
    }
    public T getByType(AttributeType attributeType){
        if(attributeType == null){
            return null;
        }
        for(T valueMap : getChildes()){
            if(attributeType == valueMap.getAttributeType()){
                return valueMap;
            }
        }
        return null;
    }
    public T getByName(int name){
        for(T resValueMap : getChildes()){
            if(resValueMap != null && name == resValueMap.getName()){
                return resValueMap;
            }
        }
        return null;
    }
    @Override
    protected void onRefreshed() {
    }
    public void onRemoved(){
        for(T resValueMap : getChildes()){
            resValueMap.onRemoved();
        }
    }
    @Override
    public void clearChildes(){
        this.onRemoved();
        super.clearChildes();
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        if(isNull()){
            return jsonArray;
        }
        T[] childes = getChildes();
        for(int i = 0; i < childes.length; i++){
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
    public void merge(CompoundItemArray<?> mapArray){
        if(mapArray == null || mapArray == this){
            return;
        }
        clearChildes();
        int count = mapArray.childesCount();
        ensureSize(count);
        for(int i=0;i<count;i++){
            ResValueMap coming = mapArray.get(i);
            ResValueMap exist = get(i);
            exist.merge(coming);
        }
    }
}
