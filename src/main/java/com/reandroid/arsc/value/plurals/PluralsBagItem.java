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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResValueBagItem;
import com.reandroid.arsc.value.ValueType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluralsBagItem {
    private final ResValueBagItem mBagItem;
    private PluralsBagItem(ResValueBagItem bagItem){
        this.mBagItem=bagItem;
    }
    public ResValueBagItem getBagItem() {
        return mBagItem;
    }
    public PluralsQuantity getQuantity(){
        ResValueBagItem item=getBagItem();
        return PluralsQuantity.valueOf(item.getIdLow());
    }
    public ValueType getValueType(){
        return getBagItem().getValueType();
    }
    private TableStringPool getStringPool(){
        EntryBlock entryBlock=getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        PackageBlock pkg = entryBlock.getPackageBlock();
        if(pkg==null){
            return null;
        }
        TableBlock tableBlock= pkg.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        return tableBlock.getTableStringPool();
    }
    public int getValue(){
        return getBagItem().getData();
    }
    public boolean hasStringValue(){
        return getValueType()==ValueType.STRING;
    }
    public boolean hasReferenceValue(){
        return getValueType()==ValueType.REFERENCE;
    }
    public String getStringValue(){
        ValueType valueType=getValueType();
        if(valueType!=ValueType.STRING){
            throw new IllegalArgumentException("Not STRING ValueType="+valueType);
        }
        TableStringPool stringPool=getStringPool();
        if(stringPool==null){
            return null;
        }
        int ref=getValue();
        TableString tableString = stringPool.get(ref);
        return tableString.getHtml();
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<item quantity=\"");
        builder.append(getQuantity());
        builder.append("\">");
        if(hasStringValue()){
            builder.append(getStringValue());
        }else {
            builder.append(String.format("@0x%08x", getValue()));
        }
        builder.append("</item>");
        return builder.toString();
    }

    public static PluralsBagItem[] create(ResValueBagItem[] resValueBagItems){
        if(resValueBagItems==null){
            return null;
        }
        int len=resValueBagItems.length;
        if(len==0){
            return null;
        }
        Set<PluralsQuantity> duplicates=new HashSet<>();
        List<PluralsBagItem> results=new ArrayList<>();
        for(int i=0;i<len;i++){
            ResValueBagItem resValueBagItem = resValueBagItems[i];
            if(resValueBagItem.getIdHigh() != 0x0100){
                return null;
            }
            PluralsBagItem item=create(resValueBagItem);
            if(item==null){
                // If it reaches here type name is obfuscated
                return null;
            }
            PluralsQuantity quantity=item.getQuantity();
            if(duplicates.contains(quantity)){
                return null;
            }
            duplicates.add(quantity);
            results.add(item);
        }
        return results.toArray(new PluralsBagItem[0]);
    }
    public static PluralsBagItem create(ResValueBagItem resValueBagItem){
        PluralsBagItem item=new PluralsBagItem(resValueBagItem);
        if(item.getQuantity()==null){
            return null;
        }
        return item;
    }
}
