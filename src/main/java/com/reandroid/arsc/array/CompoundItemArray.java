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
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.arsc.value.*;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;

import java.util.Comparator;

public abstract class CompoundItemArray<T extends ResValueMap>
        extends BlockArray<T> implements JSONConvert<JSONArray>, Comparator<ResValueMap> {
    public CompoundItemArray(){
        super();
    }

    public void sort(){
        super.sort(this);
        updateCountToHeader();
    }
    @Override
    public T createNext(){
        T resValueMap = super.createNext();
        updateCountToHeader();
        return resValueMap;
    }
    private void updateCountToHeader(){
        EntryHeaderMap headerMap = getEntryHeaderMap();
        headerMap.setValuesCount(size());
    }
    private EntryHeaderMap getEntryHeaderMap(){
        ResTableMapEntry mapEntry = getParent(ResTableMapEntry.class);
        assert mapEntry != null;
        return mapEntry.getHeader();
    }
    public AttributeDataFormat[] getFormats(){
        ResValueMap formatsMap = getByType(AttributeType.FORMATS);
        if(formatsMap != null){
            return AttributeDataFormat.decodeValueTypes(
                    formatsMap.getData() & 0xff);
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
    public T getOrCreateType(AttributeType attributeType){
        if(attributeType == null){
            return null;
        }
        T valueMap = getByType(attributeType);
        if(valueMap == null){
            valueMap = createNext();
            valueMap.setAttributeType(attributeType);
        }
        return valueMap;
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
            if(resValueMap != null && name == resValueMap.getNameId()){
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
    public void clear(){
        this.onRemoved();
        super.clear();
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
        clear();
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
        clear();
        int count = mapArray.size();
        ensureSize(count);
        for(int i=0;i<count;i++){
            ResValueMap coming = mapArray.get(i);
            ResValueMap exist = get(i);
            assert exist != null;
            exist.merge(coming);
        }
    }
    public void mergeWithName(ResourceMergeOption mergeOption, CompoundItemArray<?> mapArray){
        if(mapArray == null || mapArray == this){
            return;
        }
        clear();
        int count = mapArray.size();
        ensureSize(count);
        for(int i=0;i<count;i++){
            ResValueMap coming = mapArray.get(i);
            ResValueMap exist = get(i);
            assert exist != null;
            exist.mergeWithName(mergeOption, coming);
        }
    }
    @Override
    public int compare(ResValueMap valueMap1, ResValueMap valueMap2){
        if(valueMap1 == valueMap2){
            return 0;
        }
        if(valueMap1 == null){
            return 1;
        }
        return valueMap1.compareTo(valueMap2);
    }
}
