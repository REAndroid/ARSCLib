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
