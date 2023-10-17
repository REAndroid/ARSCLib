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
package com.reandroid.dex.pool;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.*;

public class KeyPool<T extends Block> {

    private final SectionType<T> sectionType;
    private Map<Key, KeyItemGroup<T>> itemsMap;

    public KeyPool(SectionType<T> sectionType, int initialSize){
        this.sectionType = sectionType;
        this.itemsMap = new HashMap<>(initialSize);
    }

    public void trimToSize(){
        for(KeyItemGroup<T> group : itemsMap.values()){
            group.trimToSize();
        }
    }
    public SectionType<T> getSectionType(){
        return sectionType;
    }
    public Iterable<T> put(Key key, T item){
        if(key == null || item == null){
            return null;
        }
        KeyItemGroup<T> group = itemsMap.get(key);
        if(group == null){
            group = new KeyItemGroup<>();
            group.add(item);
            itemsMap.put(key, group);
        }else {
            group.add(item);
        }
        return group;
    }
    public void put(Key key, KeyItemGroup<T> group){
        if(key != null && group != null){
            itemsMap.put(key, group);
        }
    }
    public int size(){
        return itemsMap.size();
    }
    public void clear(){
        itemsMap.clear();
    }
    public void reInitialize(int capacity){
        itemsMap.clear();
        itemsMap = new HashMap<>(capacity);
    }
    KeyItemGroup<T> add(T item){
        if(item == null){
            return null;
        }
        Key key = ((KeyItem)item).getKey();
        if(key == null){
            return null;
        }
        KeyItemGroup<T> group = itemsMap.get(key);
        if(group == null){
            group = new KeyItemGroup<>();
            group.add(item);
            itemsMap.put(key, group);
        }else {
            group.add(item);
        }
        return group;
    }
    public void remove(T item){
        if(item == null){
            return;
        }
        Key key = ((KeyItem)item).getKey();
        KeyItemGroup<T> group = getGroup(key);
        if(group == null){
            return;
        }
        group.remove(item);
        if(group.isEmpty()){
            remove(key);
        }
    }
    KeyItemGroup<T> remove(Key key){
        if(key == null){
            return null;
        }
        return itemsMap.remove(key);
    }
    public boolean contains(Key key){
        return itemsMap.containsKey(key);
    }
    public T get(Key key){
        KeyItemGroup<T> group = getGroup(key);
        if(group != null){
            return group.matching(key);
        }
        return null;
    }
    public Iterator<T> getAll(Key key){
        KeyItemGroup<T> group = getGroup(key);
        if(group != null){
            return group.iterator();
        }
        return EmptyIterator.of();
    }
    public KeyItemGroup<T> getGroup(Key key){
        if(key == null){
            return null;
        }
        return itemsMap.get(key);
    }
    public Iterator<T> getIterator(Key key){
        KeyItemGroup<T> group = getGroup(key);
        if(group == null){
            return EmptyIterator.of();
        }
        return group.iterator();
    }

    @Override
    public String toString() {
        return getSectionType().getName() + "-Pool = " + size();
    }
}
