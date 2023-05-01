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
package com.reandroid.arsc.group;

import com.reandroid.arsc.base.BlockArrayCreator;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;

import java.util.Iterator;

public class EntryGroup extends ItemGroup<Entry> {
    private final int resourceId;
    public EntryGroup(int resId) {
        super(ARRAY_CREATOR, String.valueOf(resId));
        this.resourceId=resId;
    }
    public Entry getEntry(ResConfig resConfig){
        Entry[] items = getItems();
        if(items == null || resConfig == null){
            return null;
        }
        int length = items.length;
        for(int i=0; i<length; i++){
            Entry entry = items[i];
            if(entry == null || entry.isNull()){
                continue;
            }
            if(resConfig.equals(entry.getResConfig())){
                return entry;
            }
        }
        return null;
    }
    public int getResourceId(){
        return resourceId;
    }
    public boolean renameSpec(String name){
        Entry[] items=getItems();
        if(items==null || name==null){
            return false;
        }
        SpecStringPool specStringPool=getSpecStringPool();
        if(specStringPool==null){
            return false;
        }
        if(isAllSameSpec()){
            String oldName=getSpecName();
            if(name.equals(oldName)){
                return false;
            }
        }
        SpecString specString=specStringPool.getOrCreate(name);
        return renameSpec(specString.getIndex());
    }
    public short getEntryId(){
        return (short) (getResourceId() & 0xffff);
    }
    private boolean isAllSameSpec(){
        Entry first=null;
        for(Entry entry :listItems()){
            if(first==null){
                first= entry;
                continue;
            }
            if(first.getSpecReference()!= entry.getSpecReference()){
                return false;
            }
        }
        return true;
    }
    public boolean renameSpec(int specReference){
        Entry[] items=getItems();
        if(items==null){
            return false;
        }
        boolean renameOk=false;
        for(Entry block:items){
            if(block==null){
                continue;
            }
            if(block.getSpecReference()==specReference){
                continue;
            }
            block.setSpecReference(specReference);
            renameOk=true;
        }
        return renameOk;
    }
    public Entry pickOne(){
        Entry[] items=getItems();
        if(items==null){
            return null;
        }
        Entry result = null;
        for(Entry entry :items){
            if(entry == null){
                continue;
            }
            if(entry.isDefault() && !entry.isNull()){
                return entry;
            }
            if(result==null || result.isNull()){
                result = entry;
                continue;
            }
            result = choose(result, entry);
        }
        return result;
    }
    private Entry choose(Entry entry1, Entry entry2){
        if(entry1 == null || entry1 == entry2){
            return entry2;
        }
        if(entry2 == null){
            return entry1;
        }
        if(entry1.isNull() && !entry2.isNull()){
            return entry2;
        }
        if(!entry1.isNull() && entry2.isNull()){
            return entry1;
        }
        if(entry1.isDefault()){
            return entry1;
        }
        if(entry2.isDefault()){
            return entry2;
        }
        ResConfig config1 = entry1.getResConfig();
        ResConfig config2 = entry2.getResConfig();
        if(config1 == null && config2==null){
            return entry1;
        }
        if(config1 == null){
            return entry2;
        }
        if(config2 == null){
            return entry1;
        }
        boolean lang1 = isDefaultLanguage(config1);
        boolean lang2 = isDefaultLanguage(config2);
        if((lang1 && !lang2) || (!lang1 && !lang2)){
            return entry1;
        }
        if(!lang1){
            return entry2;
        }
        String region1 = config1.getRegion();
        String region2 = config2.getRegion();
        if((region1!=null && region2==null) || (region1==null && region2==null)){
            return entry1;
        }
        if(region1==null){
            return entry2;
        }
        return entry1;
    }
    private boolean isDefaultLanguage(ResConfig resConfig){
        String lang = resConfig.getLanguage();
        if(lang == null){
            return true;
        }
        return "en".equals(lang);
    }
    public Entry getDefault(){
        return getDefault(true);
    }
    public Entry getDefault(boolean skipNull){
        Iterator<Entry> itr=iterator(skipNull);
        while (itr.hasNext()){
            Entry entry =itr.next();
            if(entry.isDefault()){
                return entry;
            }
        }
        return null;
    }
    public TypeString getTypeString(){
        Entry entry =pickOne();
        if(entry !=null){
            return entry.getTypeString();
        }
        return null;
    }
    public SpecString getSpecString(){
        Entry entry =pickOne();
        if(entry !=null){
            return entry.getSpecString();
        }
        return null;
    }
    public String getTypeName(){
        TypeString typeString=getTypeString();
        if(typeString==null){
            return null;
        }
        return typeString.get();
    }
    public String getSpecName(){
        SpecString specString=getSpecString();
        if(specString==null){
            return null;
        }
        return specString.get();
    }
    private SpecStringPool getSpecStringPool(){
        Entry entry =get(0);
        if(entry ==null){
            return null;
        }
        TypeBlock typeBlock= entry.getTypeBlock();
        if(typeBlock==null){
            return null;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        return packageBlock.getSpecStringPool();
    }
    @Override
    public int hashCode(){
        return resourceId;
    }
    @Override
    public String toString(){
        Entry entry =pickOne();
        if(entry ==null){
            return super.toString();
        }
        return super.toString()+"{"+ entry.toString()+"}";
    }

    private static final BlockArrayCreator<Entry> ARRAY_CREATOR = new BlockArrayCreator<Entry>(){
        @Override
        public Entry newInstance() {
            return new Entry();
        }

        @Override
        public Entry[] newInstance(int len) {
            return new Entry[len];
        }
    };

}
