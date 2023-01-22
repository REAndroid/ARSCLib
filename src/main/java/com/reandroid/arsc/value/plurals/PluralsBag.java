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
package com.reandroid.arsc.value.plurals;

import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResValueBag;

public class PluralsBag {
    private final PluralsBagItem[] mBagItems;
    private PluralsBag(PluralsBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public PluralsBagItem[] getBagItems() {
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
        PluralsBagItem[] allItems = getBagItems();
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
     * in addition to this use your methods to cross check like type-name == "plurals"**/
    public static boolean isPlurals(ResValueBag resValueBag){
        if(resValueBag==null){
            return false;
        }
        EntryBlock entryBlock = resValueBag.getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        return PluralsBagItem.create(resValueBag.getBagItems()) != null;
    }

    public static PluralsBag create(ResValueBag resValueBag){
        if(resValueBag==null){
            return null;
        }
        PluralsBagItem[] bagItems=PluralsBagItem.create(resValueBag.getBagItems());
        if(bagItems==null){
            return null;
        }
        return new PluralsBag(bagItems);
    }
}
