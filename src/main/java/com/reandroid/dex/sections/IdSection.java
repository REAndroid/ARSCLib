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

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.base.BlockListArray;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class IdSection<T extends IdItem> extends Section<T> {

    public IdSection(IntegerPair countAndOffset, SectionType<T> sectionType) {
        super(sectionType, new IdSectionArray<>(countAndOffset, sectionType.getCreator()));
    }
    IdSection(SectionType<T> sectionType, IdSectionArray<T> itemArray){
        super(sectionType, itemArray);
    }

    @SuppressWarnings("unchecked")
    @Override
    boolean keyChanged(Block block, Key key, boolean immediateSort) {
        boolean changed = super.keyChanged(block, key, immediateSort);
        if(!immediateSort){
            return changed;
        }
        T item = (T) block;
        sortImmediate(item);
        return changed;
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        sort();
    }
    @Override
    public boolean remove(Key key){
        T item = getSectionItem(key);
        if(item != null && item.getParent() != null){
            item.removeSelf();
            return true;
        }
        return false;
    }
    @Override
    public Iterator<Key> removeAll(Predicate<? super Key> filter){
        Iterator<T> iterator = getItemArray().clonedIterator();
        return ComputeIterator.of(iterator, item -> {
            Key key = item.getKey();
            if(filter.test(key)){
                item.removeSelf();
                return key;
            }
            return null;
        });
    }
    @Override
    public T getSectionItem(int i){
        T result = getItemArray().get(i);
        if(i >= 0 && result == null){
            throw new NullPointerException("Null id: " + i);
        }
        return result;
    }
    @Override
    public T[] getSectionItems(int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        BlockListArray<T> itemArray = getItemArray();
        int length = indexes.length;
        T[] results = itemArray.newArrayInstance(indexes.length);
        for(int i = 0; i < length; i++){
            results[i] = itemArray.get(indexes[i]);
            if(results[i] == null){
                throw new NullPointerException("Null id: " + i);
            }
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
        int count = getCount() + 200;
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
