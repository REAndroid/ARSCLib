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
package com.reandroid.dex.sections;

import com.reandroid.dex.base.BlockListArray;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;

import java.util.Collection;
import java.util.Iterator;

public class IdSection<T extends IdItem> extends Section<T> {

    public IdSection(IntegerPair countAndOffset, SectionType<T> sectionType) {
        super(sectionType, new IdSectionArray<>(countAndOffset, sectionType.getCreator()));
    }
    IdSection(SectionType<T> sectionType, IdSectionArray<T> itemArray){
        super(sectionType, itemArray);
    }

    @Override
    public T get(int i){
        return getItemArray().get(i);
    }
    @Override
    public T[] get(int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        BlockListArray<T> itemArray = getItemArray();
        int length = indexes.length;
        T[] results = itemArray.newInstance(indexes.length);
        for(int i = 0; i < length; i++){
            results[i] = itemArray.get(indexes[i]);
        }
        return results;
    }
    public T createItem() {
        return getItemArray().createNext();
    }
    @Override
    void onRefreshed(int position){
        position += getItemArray().countBytes();
        updateNextSection(position);
    }
    @Override
    int getDiffCount(Section<T> section){
        int count = getCount();
        if(section == this || section == null){
            return count;
        }
        for(T item : section){
            if(!contains(item.getKey())){
                count ++;
            }
        }
        return count;
    }
    public boolean canAdd(Collection<IdItem> collection){
        int count = getCount();
        int check = count + collection.size();
        if((check & 0xffff0000) == 0){
            return true;
        }
        SectionType<T> sectionType = getSectionType();
        Iterator<IdItem> iterator = collection.iterator();
        while (iterator.hasNext()){
            IdItem item = iterator.next();
            if(item.getSectionType() != sectionType){
                continue;
            }
            Key key = item.getKey();
            if(!contains(key)){
                count++;
            }
            if((count & 0xffff0000) != 0){
                return false;
            }
        }
        return (count & 0xffff0000) == 0;
    }
}
