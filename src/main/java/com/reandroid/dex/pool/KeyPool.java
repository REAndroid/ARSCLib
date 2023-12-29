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

import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.*;

public class KeyPool<T extends SectionItem> {

    private final SectionType<T> sectionType;
    private Map<Key, Object> itemsMap;

    public KeyPool(SectionType<T> sectionType, int initialSize){
        this.sectionType = sectionType;
        this.itemsMap = new HashMap<>(initialSize);
    }

    public void trimToSize(){
        for(Object obj : itemsMap.values()){
            if(obj instanceof KeyItemGroup){
                ((KeyItemGroup<?>)obj).trimToSize();
            }
        }
    }
    public SectionType<T> getSectionType(){
        return sectionType;
    }
    @SuppressWarnings("unchecked")
    public void put(Key key, T item){
        if(key == null || item == null){
            return;
        }
        Object obj = getItem(key);
        if(obj == null){
            itemsMap.put(key, item);
            return;
        }
        KeyItemGroup<T> group;
        if(!(obj instanceof KeyItemGroup)){
            group = new KeyItemGroup<>((T) obj);
            itemsMap.remove(key);
            itemsMap.put(key, group);
        }else {
            group = (KeyItemGroup<T>) obj;
        }
        group.add(item);
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
    @SuppressWarnings("unchecked")
    void add(T item){
        if(item == null){
            return;
        }
        Key key = ((KeyItem)item).getKey();
        if(key == null){
            return;
        }
        Object obj = getItem(key);
        if(obj == null){
            itemsMap.put(key, item);
            return;
        }

        KeyItemGroup<T> group;
        if(!(obj instanceof KeyItemGroup)){
            group = new KeyItemGroup<>((T) obj);
            itemsMap.put(key, group);
        }else {
            group = (KeyItemGroup<T>) obj;
        }
        group.add(item);
    }
    @SuppressWarnings("unchecked")
    public void remove(T item){
        if(item == null){
            return;
        }
        Key key = ((KeyItem)item).getKey();
        Object obj = getItem(key);
        if(obj == null){
            return;
        }
        if(!(obj instanceof KeyItemGroup)){
            remove(key);
            return;
        }
        KeyItemGroup<T> group = (KeyItemGroup<T>) obj;
        group.remove(item);
        if(group.isEmpty()){
            remove(key);
        }
    }
    void remove(Key key){
        if(key == null){
            return;
        }
        itemsMap.remove(key);
    }
    public boolean contains(Key key){
        return itemsMap.containsKey(key);
    }
    @SuppressWarnings("unchecked")
    public T get(Key key){
        Object obj = getItem(key);
        if(!(obj instanceof KeyItemGroup)){
            return (T) obj;
        }
        KeyItemGroup<T> group = (KeyItemGroup<T>) obj;
        return group.matching(key);
    }
    @SuppressWarnings("unchecked")
    public Iterator<T> getAll(Key key){
        Object obj = getItem(key);
        if(!(obj instanceof KeyItemGroup)){
            return SingleIterator.of((T) obj);
        }
        KeyItemGroup<T> group = (KeyItemGroup<T>) obj;
        return group.iterator();
    }
    @SuppressWarnings("unchecked")
    public KeyItemGroup<T> getGroup(Key key){
        if(key == null){
            return null;
        }
        Object item = getItem(key);
        if(item == null){
            return null;
        }
        if(item instanceof KeyItemGroup){
            return (KeyItemGroup<T>) item;
        }
        return new KeyItemGroup<>((T)item);
    }
    Object getItem(Key key){
        return itemsMap.get(key);
    }

    @SuppressWarnings("unchecked")
    Iterator<KeyItemGroup<T>> groupIterator(){
        return ComputeIterator.of(itemsMap.values().iterator(), obj -> {
            if(obj instanceof KeyItemGroup){
                return (KeyItemGroup<T>) obj;
            }
            return null;
        });
    }

    @Override
    public String toString() {
        return getSectionType().getName() + "-Pool = " + size();
    }
}
