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

import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.ValueType;

import java.util.ArrayList;
import java.util.List;

public class StyleBagItem {
    private final ResValueMap mBagItem;
    public StyleBagItem(ResValueMap bagItem){
        this.mBagItem=bagItem;
    }
    public ResValueMap getBagItem() {
        return mBagItem;
    }

    public String getName(){
        Entry block=getBagItem().getEntry();
        if(block==null){
            return null;
        }
        char prefix=0;
        return block.buildResourceName(getNameId(), prefix, false);
    }
    public int getNameId(){
        return getBagItem().getName();
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
        Entry entry =getBagItem().getEntry();
        if(entry ==null){
            return null;
        }
        char prefix='@';
        boolean includeType=true;
        if(valueType==ValueType.ATTRIBUTE){
            prefix='?';
            includeType=false;
        }
        int id=getValue();
        return entry.buildResourceName(id, prefix, includeType);
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
    public static StyleBagItem[] create(ResValueMap[] resValueMaps){
        if(resValueMaps ==null){
            return null;
        }
        int len= resValueMaps.length;
        if(len==0){
            return null;
        }
        List<StyleBagItem> results=new ArrayList<>();
        for(int i=0;i<len;i++){
            StyleBagItem item=create(resValueMaps[i]);
            if(item==null){
                return null;
            }
            results.add(item);
        }
        return results.toArray(new StyleBagItem[0]);
    }
    public static StyleBagItem create(ResValueMap resValueMap){
        if(resValueMap ==null){
            return null;
        }
        return new StyleBagItem(resValueMap);
    }
}
