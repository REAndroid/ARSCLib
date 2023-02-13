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
package com.reandroid.arsc.value.style;

import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;

public class StyleBag {
    private final StyleBagItem[] mBagItems;
    private StyleBag(StyleBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public StyleBagItem[] getBagItems() {
        return mBagItems;
    }
    public String getName(){
        Entry entry = getEntry();
        if(entry ==null){
            return null;
        }
        SpecString spec = entry.getSpecString();
        if(spec==null){
            return null;
        }
        return spec.get();
    }
    public String getParentResourceName(){
        int id=getParentId();
        if(id==0){
            return null;
        }
        Entry entry = getEntry();
        if(entry ==null){
            return null;
        }
        return entry.buildResourceName(id, '@', true);
    }
    public int getParentId(){
        ResTableMapEntry mapEntry = getBagItems()[0].getBagItem().getParentMapEntry();
        if(mapEntry==null){
            return 0;
        }
        return mapEntry.getParentId();
    }
    public int getResourceId(){
        Entry entry = getEntry();
        if(entry ==null){
            return 0;
        }
        return entry.getResourceId();
    }
    public String getTypeName(){
        Entry entry =getBagItems()[0].getBagItem().getEntry();
        if(entry ==null){
            return null;
        }
        return entry.getTypeName();
    }
    private Entry getEntry(){
        return getBagItems()[0].getBagItem().getEntry();
    }
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<");
        String type=getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\"");
        String parent=getParentResourceName();
        if(parent!=null){
            builder.append(" parent=\"");
            builder.append(parent);
            builder.append("\"");
        }
        builder.append("\">");
        StyleBagItem[] allItems = getBagItems();
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
    public static boolean isStyle(ResTableMapEntry mapEntry){
        if(mapEntry==null){
            return false;
        }
        return StyleBag.create(mapEntry) != null;
    }

    public static StyleBag create(ResTableMapEntry mapEntry){
        if(mapEntry==null){
            return null;
        }
        StyleBagItem[] bagItems=StyleBagItem.create(mapEntry.getValue().getChildes());
        if(bagItems==null){
            return null;
        }
        return new StyleBag(bagItems);
    }
}
