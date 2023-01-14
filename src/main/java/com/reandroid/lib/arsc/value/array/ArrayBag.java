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
package com.reandroid.lib.arsc.value.array;

import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;

public class ArrayBag {
    private final ArrayBagItem[] mBagItems;
    private ArrayBag(ArrayBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public ArrayBagItem[] getBagItems() {
        return mBagItems;
    }
    public String getName(){
        EntryBlock entryBlock=getBagItems()[0].getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        return entryBlock.getName();
    }
    public String getTypeName(){
        EntryBlock entryBlock=getBagItems()[0].getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        return entryBlock.getTypeName();
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
    public static boolean isArray(ResValueBag resValueBag){
        if(resValueBag==null){
            return false;
        }
        EntryBlock entryBlock = resValueBag.getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        if(resValueBag.getParentId()!=0){
            return false;
        }
        ArrayBagItem[] arrayBagItems = ArrayBagItem.create(resValueBag.getBagItems());
        if(arrayBagItems==null || arrayBagItems.length==0){
            return false;
        }
        for(int i=0;i< arrayBagItems.length; i++){
            ArrayBagItem arrayBagItem = arrayBagItems[i];
            ResValueBagItem resValueBagItem = arrayBagItem.getBagItem();
            if(resValueBagItem.getIdHigh()!=0x0100){
                return false;
            }
            if(resValueBagItem.getIdLow() != (i+1)){
                return false;
            }
        }
        return true;
    }

    public static ArrayBag create(ResValueBag resValueBag){
        if(resValueBag==null){
            return null;
        }
        ArrayBagItem[] bagItems=ArrayBagItem.create(resValueBag.getBagItems());
        if(bagItems==null){
            return null;
        }
        return new ArrayBag(bagItems);
    }
}
