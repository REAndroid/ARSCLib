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
package com.reandroid.lib.arsc.value.style;

import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;

import java.util.ArrayList;
import java.util.List;

public class StyleBagItem {
    private final ResValueBagItem mBagItem;
    public StyleBagItem(ResValueBagItem bagItem){
        this.mBagItem=bagItem;
    }
    public ResValueBagItem getBagItem() {
        return mBagItem;
    }

    public String getName(){
        EntryBlock block=getBagItem().getEntryBlock();
        if(block==null){
            return null;
        }
        char prefix=0;
        return block.buildResourceName(getNameId(), prefix, false);
    }
    public int getNameId(){
        return getBagItem().getId();
    }
    public boolean hasStringValue(){
        return getValueType()== ValueType.STRING;
    }
    public boolean hasReferenceValue(){
        return getValueType()==ValueType.REFERENCE;
    }
    public boolean hasAttributeValue(){
        return getValueType()==ValueType.REFERENCE;
    }
    public String getValueAsReference(){
        ValueType valueType=getValueType();
        if(valueType!=ValueType.REFERENCE && valueType!=ValueType.ATTRIBUTE){
            throw new IllegalArgumentException("Not REF ValueType="+valueType);
        }
        EntryBlock entryBlock=getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        char prefix='@';
        boolean includeType=true;
        if(valueType==ValueType.ATTRIBUTE){
            prefix='?';
            includeType=false;
        }
        int id=getValue();
        return entryBlock.buildResourceName(id, prefix, includeType);
    }
    public String getStringValue(){
        return mBagItem.getValueAsString();
    }
    public ValueType getValueType(){
        return getBagItem().getValueType();
    }
    public int getValue(){
        return getBagItem().getData();
    }
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<item name=\"");
        String name=getName();
        if(name==null){
            name=String.format("@0x%08x", getNameId());
        }
        builder.append(name);
        builder.append("\">");
        if(hasStringValue()){
            builder.append(getStringValue());
        }
        String val=null;
        if(hasReferenceValue()||hasAttributeValue()) {
            val=getValueAsReference();
        }
        if(val==null) {
            val=String.format("0x%08x", getValue());
        }
        builder.append(val);
        builder.append("</item>");
        return builder.toString();
    }
    public static StyleBagItem[] create(ResValueBagItem[] resValueBagItems){
        if(resValueBagItems==null){
            return null;
        }
        int len=resValueBagItems.length;
        if(len==0){
            return null;
        }
        List<StyleBagItem> results=new ArrayList<>();
        for(int i=0;i<len;i++){
            StyleBagItem item=create(resValueBagItems[i]);
            if(item==null){
                return null;
            }
            results.add(item);
        }
        return results.toArray(new StyleBagItem[0]);
    }
    public static StyleBagItem create(ResValueBagItem resValueBagItem){
        if(resValueBagItem==null){
            return null;
        }
        return new StyleBagItem(resValueBagItem);
    }
}
