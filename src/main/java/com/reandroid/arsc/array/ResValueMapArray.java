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
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;

public class ResValueMapArray extends BlockArray<ResValueMap> implements JSONConvert<JSONArray> {
    public ResValueMapArray(){
        super();
    }

    public ResValueMap getByName(int name){
        for(ResValueMap resValueMap:listItems()){
            if(resValueMap != null &&name == resValueMap.getName()){
                return resValueMap;
            }
        }
        return null;
    }
    @Override
    public ResValueMap newInstance() {
        return new ResValueMap();
    }

    @Override
    public ResValueMap[] newInstance(int len) {
        return new ResValueMap[len];
    }

    @Override
    protected void onRefreshed() {
    }
    public void onRemoved(){
        for(ResValueMap resValueMap:listItems()){
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
        ResValueMap[] childes = getChildes();
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
    public void merge(ResValueMapArray bagItemArray){
        if(bagItemArray==null||bagItemArray==this){
            return;
        }
        clearChildes();
        int count=bagItemArray.childesCount();
        ensureSize(count);
        for(int i=0;i<count;i++){
            ResValueMap coming=bagItemArray.get(i);
            ResValueMap exist=get(i);
            exist.merge(coming);
        }
    }
}
