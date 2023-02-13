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
package com.reandroid.arsc.value.array;

import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ResValueMap;

public class ArrayBag {
    private final ArrayBagItem[] mBagItems;
    private ArrayBag(ArrayBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public ArrayBagItem[] getBagItems() {
        return mBagItems;
    }
    public String getName(){
        Entry entry =getBagItems()[0].getBagItem().getEntry();
        if(entry ==null){
            return null;
        }
        return entry.getName();
    }
    public String getTypeName(){
        Entry entry =getBagItems()[0].getBagItem().getEntry();
        if(entry ==null){
            return null;
        }
        return entry.getTypeName();
    }
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<");
        String type=getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\">");
        ArrayBagItem[] allItems = getBagItems();
        for(int i=0;i<allItems.length;i++){
            builder.append("\n    ");
            builder.append(allItems[i].toString());
        }
        builder.append("\n</");
        builder.append(type);
        builder.append(">");
        return builder.toString();
    }

    /** The result of this is not always 100% accurate,
     * in addition to this use your methods to cross check like type-name == "array"**/
    public static boolean isArray(ResTableMapEntry mapEntry){
        if(mapEntry==null){
            return false;
        }
        if(mapEntry.getParentId()!=0){
            return false;
        }
        ArrayBagItem[] arrayBagItems = ArrayBagItem.create(mapEntry.listResValueMap());
        if(arrayBagItems==null || arrayBagItems.length==0){
            return false;
        }
        for(int i=0;i< arrayBagItems.length; i++){
            ArrayBagItem arrayBagItem = arrayBagItems[i];
            ResValueMap resValueMap = arrayBagItem.getBagItem();
            int name = resValueMap.getName();
            int high = (name >> 16) & 0xffff;
            if(high!=0x0100){
                return false;
            }
            int low = name & 0xffff;
            if(low != (i+1)){
                return false;
            }
        }
        return true;
    }

    public static ArrayBag create(ResTableMapEntry mapEntry){
        if(mapEntry==null){
            return null;
        }
        ArrayBagItem[] bagItems=ArrayBagItem.create(mapEntry.listResValueMap());
        if(bagItems==null){
            return null;
        }
        return new ArrayBag(bagItems);
    }
}
