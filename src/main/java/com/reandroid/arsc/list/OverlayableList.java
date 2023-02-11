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
package com.reandroid.arsc.list;

import com.reandroid.arsc.chunk.Overlayable;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

public class OverlayableList extends BlockList<Overlayable> implements JSONConvert<JSONArray> {
    public OverlayableList(){
        super();
    }
    public Overlayable get(String name){
        for(Overlayable overlayable:getChildes()){
            if(name.equals(overlayable.getName())){
                return overlayable;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        if(size()==0){
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        for(Overlayable overlayable:getChildes()){
            JSONObject jsonOverlayble = overlayable.toJson();
            jsonArray.put(jsonOverlayble);
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json==null){
            return;
        }
        int length = json.length();
        for(int i=0;i<length;i++){
            Overlayable overlayable = new Overlayable();
            add(overlayable);
            overlayable.fromJson(json.getJSONObject(i));
        }
    }
    public void merge(OverlayableList overlayableList){
        if(overlayableList==null || overlayableList==this){
            return;
        }
        for(Overlayable overlayable: overlayableList.getChildes()){
            Overlayable exist=get(overlayable.getName());
            if(exist==null){
                exist = new Overlayable();
                add(exist);
            }
            exist.merge(overlayable);
        }
    }
}
