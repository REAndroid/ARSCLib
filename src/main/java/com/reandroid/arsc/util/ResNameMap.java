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
package com.reandroid.arsc.util;

import java.util.HashMap;
import java.util.Map;

public class ResNameMap<VALUE> {
    private final Object mLock = new Object();
    private final Map<String, Map<String, VALUE>> mainMap;
    public ResNameMap(){
        this.mainMap = new HashMap<>();
    }

    public VALUE get(String type, String name){
        synchronized (mLock){
            if(type==null || name==null){
                return null;
            }
            Map<String, VALUE> valueMap = mainMap.get(type);
            if(valueMap!=null){
                return valueMap.get(name);
            }
            return null;
        }
    }
    public void add(String type, String name, VALUE value){
        synchronized (mLock){
            if(type==null || name==null || value==null){
                return;
            }
            Map<String, VALUE> valueMap = mainMap.get(type);
            if(valueMap==null){
                valueMap=new HashMap<>();
                mainMap.put(type, valueMap);
            }
            valueMap.putIfAbsent(name, value);
        }
    }
    public void clear(){
        synchronized (mLock){
            for(Map<String, VALUE> valueMap:mainMap.values()){
                valueMap.clear();
            }
            mainMap.clear();
        }
    }

    private static final String TYPE_ATTR = "attr";
    private static final String TYPE_ID = "id";

}
