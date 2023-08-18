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
package com.reandroid.arsc.array;

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.util.*;

public abstract class StringArray<T extends StringItem> extends OffsetBlockArray<T> implements JSONConvert<JSONArray> {
    private boolean mUtf8;

    public StringArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        super(offsets, itemCount, itemStart);
        this.mUtf8=is_utf8;
    }
    @Override
    protected void onPreShifting(){
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null){
            stringPool.ensureStringLinkUnlockedInternal();
        }
    }
    @Override
    protected void onPostShift(int index){
        StringPool<?> stringPool = getParentInstance(StringPool.class);
        if(stringPool != null){
            stringPool.getStyleArray().onStringShifted(index);
        }
    }
    @Override
    protected void onPreRefresh(){
        if(isFlexible()){
            trimNullBlocks();
        }
        super.onPreRefresh();
    }

    public List<String> toStringList(){
        return new AbstractList<String>() {
            @Override
            public String get(int i) {
                T item=StringArray.this.get(i);
                if(item==null){
                    return null;
                }
                return item.getHtml();
            }
            @Override
            public int size() {
                return getChildesCount();
            }
        };
    }
    public List<T> removeUnusedStrings(){
        List<T> unusedList = listUnusedStringsToRemove();
        remove(unusedList);
        return unusedList;
    }
    @Override
    protected int remove(Collection<T> blockList, Collection<T> removedList){
        List<T> copyList = new ArrayList<>();
        int count = super.remove(blockList, copyList);
        for(T item : copyList){
            item.onRemoved();
        }
        if(removedList != null){
            removedList.addAll(copyList);
        }
        return count;
    }
    @Override
    public void onPreRemove(T block){
        block.onPreRemoveInternal();
    }
    @Override
    protected boolean remove(T block, boolean trim){
        if(block == null){
            return false;
        }
        boolean removed = super.remove(block, trim);
        if(removed){
            block.onRemoved();
        }
        return removed;
    }
    List<T> listUnusedStringsToRemove(){
        return listUnusedStrings();
    }
    public List<T> listUnusedStrings(){
        List<T> results=new ArrayList<>();
        T[] childes = getChildes();
        for(int i = 0; i < childes.length; i++){
            T item = childes[i];
            if(item != null && !item.hasReference()){
                results.add(item);
            }
        }
        return results;
    }
    public void setUtf8(boolean is_utf8){
        if(mUtf8 == is_utf8){
            return;
        }
        mUtf8 = is_utf8;
        T[] childes = getChildes();
        int length = childes.length;
        for(int i = 0; i < length; i++){
            T item = childes[i];
            if(item != null){
                item.setUtf8(is_utf8);
            }
        }
    }
    public boolean isUtf8() {
        return mUtf8;
    }

    @Override
    protected void refreshChildes(){
        // Not required
    }
    // Only styled strings
    @Override
    public JSONArray toJson() {
        return toJson(true);
    }
    public JSONArray toJson(boolean styledOnly) {
        if(getChildesCount()==0){
            return null;
        }
        JSONArray jsonArray=new JSONArray();
        int i=0;
        Iterator<T> itr = iterator(true);
        while (itr.hasNext()){
            T item = itr.next();
            if(styledOnly && !item.hasStyle()){
                continue;
            }
            JSONObject jsonObject= item.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        if(i==0){
            return null;
        }
        return jsonArray;
    }
    // Only styled strings
    @Override
    public void fromJson(JSONArray json) {
        throw new IllegalArgumentException(getClass().getSimpleName()+".fromJson() NOT implemented");
    }

    public static final Comparator<StringItem> COMPARATOR = (stringItem1, stringItem2) -> {
        if(stringItem1 == stringItem2){
            return 0;
        }
        if(stringItem1 == null){
            return 1;
        }
        return stringItem1.compareTo(stringItem2);
    };
}
