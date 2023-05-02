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
package com.reandroid.arsc.value.attribute;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.common.EntryStore;


public class AttributeBagItem {
    private final ResValueMap mBagItem;
    public AttributeBagItem(ResValueMap bagItem){
        this.mBagItem=bagItem;
    }
    public int getData(){
        return getBagItem().getData();
    }
    public String getNameOrHex(){
        return getNameOrHex(null);
    }
    public String getNameOrHex(EntryStore entryStore){
        String name=getName(entryStore);
        if(name==null){
            name=String.format("@0x%08x", getBagItem().getName());
        }
        return name;
    }
    public String getName(){
        return getName(null);
    }
    public String getName(EntryStore entryStore){
        if(isType()){
            return null;
        }
        ResValueMap item=getBagItem();
        int id=item.getName();
        Entry parentEntry=item.getEntry();
        if(parentEntry!=null){
            PackageBlock packageBlock=parentEntry.getPackageBlock();
            if(packageBlock!=null){
                EntryGroup entryGroup=packageBlock.getEntryGroup(id);
                if(entryGroup!=null){
                    String name=entryGroup.getSpecName();
                    if(name!=null){
                        return name;
                    }
                }
            }
        }
        if(entryStore!=null){
            EntryGroup entryGroup=entryStore.getEntryGroup(id);
            if(entryGroup!=null){
                return entryGroup.getSpecName();
            }
        }
        return null;
    }
    public ResValueMap getBagItem() {
        return mBagItem;
    }
    public AttributeItemType getItemType(){
        if(!isType()){
            return null;
        }
        ResValueMap item=getBagItem();
        int low = item.getName() & 0xffff;
        return AttributeItemType.valueOf((short) low);
    }
    public boolean isType(){
        ResValueMap item=getBagItem();
        return ((item.getName()>>16) & 0xffff)==0x0100;
    }
    public boolean contains(AttributeValueType valueType){
        if(valueType == null || getItemType()!=AttributeItemType.FORMAT){
            return false;
        }
        int value = 0xff & valueType.sumValues();
        int dataLow = 0xffff & getBagItem().getData();
        return (dataLow & value) == value;
    }
    public boolean isEqualType(AttributeValueType valueType){
        if(valueType == null || getItemType()!=AttributeItemType.FORMAT){
            return false;
        }
        int value = 0xff & valueType.sumValues();
        int dataLow = 0xffff & getBagItem().getData();
        return (dataLow  == value);
    }
    public AttributeValueType[] getValueTypes(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return null;
        }
        ResValueMap item=getBagItem();
        short low = (short) (item.getData() & 0xffff);
        return AttributeValueType.valuesOf(low);
    }
    public Integer getBound(){
        AttributeItemType type=getItemType();
        if(type==null || type==AttributeItemType.FORMAT){
            return null;
        }
        ResValueMap item=getBagItem();
        return item.getData();
    }
    public boolean isEnum(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return false;
        }
        ResValueMap item=getBagItem();
        int high = (item.getData() >> 16) & 0xffff;
        return high==AttributeBag.TYPE_ENUM;
    }
    public boolean isFlag(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return false;
        }
        ResValueMap item=getBagItem();
        int high = (item.getData() >> 16) & 0xffff;
        return high==AttributeBag.TYPE_FLAG;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        AttributeItemType type=getItemType();
        if(type!=null){
            builder.append(type.toString());
            if(type==AttributeItemType.FORMAT){
                if(isEnum()){
                    builder.append("(enum)");
                }else if(isFlag()){
                    builder.append("(flag)");
                }
                String value=AttributeValueType.toString(getValueTypes());
                if(value!=null){
                    builder.append("=");
                    builder.append(value);
                }
                return builder.toString();
            }
            Integer bound=getBound();
            builder.append("=");
            builder.append(bound);
            return builder.toString();
        }
        ResValueMap item=getBagItem();
        builder.append(getNameOrHex());
        builder.append("=").append(String.format("0x%x", item.getData()));
        return builder.toString();
    }

    public static String toString(EntryStore entryStore, AttributeBagItem[] bagItems)  {
        if(bagItems==null){
            return null;
        }
        int len=bagItems.length;
        if(len==0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce=false;
        for (int i = 0; i < len; i++) {
            AttributeBagItem item=bagItems[i];
            if(item==null){
                continue;
            }
            if(appendOnce){
                builder.append("|");
            }
            String name=item.getNameOrHex(entryStore);
            builder.append(name);
            appendOnce=true;
        }
        if(appendOnce){
            return builder.toString();
        }
        return null;
    }
    public static AttributeBagItem[] create(ResValueMap[] resValueMaps){
        if(resValueMaps ==null){
            return null;
        }
        AttributeBagItem format=null;
        int len= resValueMaps.length;
        AttributeBagItem[] bagItems=new AttributeBagItem[len];
        for(int i=0;i<len;i++){
            AttributeBagItem item=new AttributeBagItem(resValueMaps[i]);
            bagItems[i]=item;
            if(format==null){
                if(AttributeItemType.FORMAT==item.getItemType()){
                    format=item;
                }
            }
        }
        if(format!=null){
            return bagItems;
        }
        return null;
    }


}
