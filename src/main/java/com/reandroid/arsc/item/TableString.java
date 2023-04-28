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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueItem;

import java.util.ArrayList;
import java.util.List;

public class TableString extends StringItem {
    public TableString(boolean utf8) {
        super(utf8);
    }
    public List<Entry> listReferencedResValueEntries(){
        List<Entry> results=new ArrayList<>();
        for(ReferenceItem ref:getReferencedList()){
            if(!(ref instanceof ReferenceBlock)){
                continue;
            }
            Block block = ((ReferenceBlock<?>)ref).getBlock();
            if(block ==null){
                continue;
            }
            if(!(block instanceof ResValue)){
                continue;
            }
            ResValue resValue = (ResValue) block;
            results.add(resValue.getEntry());
        }
        return results;
    }
}
