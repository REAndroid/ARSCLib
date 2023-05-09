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

public class ResTableMapEntry extends CompoundEntry<ResValueMap, ResValueMapArray> {
    public ResTableMapEntry(){
        super(new ResValueMapArray());
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
}
