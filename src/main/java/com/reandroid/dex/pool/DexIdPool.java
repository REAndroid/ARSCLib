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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.index.ItemId;
import com.reandroid.dex.sections.Section;

import java.util.HashMap;
import java.util.Map;

public class DexIdPool<T extends ItemId> {
    private final Section<T> section;
    private final Map<String, ItemGroup<T>> itemsMap;

    public DexIdPool(Section<T> section){
        this.section = section;
        this.itemsMap = new HashMap<>(section.getCount());
    }

    public void keyChanged(String old){
        ItemGroup<T> exist = itemsMap.remove(old);
        if(exist != null){
            T item = exist.first();
            String key = item.getKey();
            itemsMap.put(key, exist);
        }
    }
    public T get(String key){
        ItemGroup<T> group = itemsMap.get(key);
        if(group != null){
            return group.first();
        }
        return null;
    }
    public T getOrCreate(String key){
        ItemGroup<T>  exist = itemsMap.get(key);
        if(exist == null) {
            T item = section.getItemArray().createNext();
            item.setKey(key);
            exist = new ItemGroup<>(section.getSectionType().getCreator(), key, item);
            itemsMap.put(key, exist);
        }
        return exist.first();
    }
    public ItemGroup<T> getGroup(String key){
        return itemsMap.get(key);
    }
    public void add(T item){
        String key = item.getKey();
        if(key != null){
            ItemGroup<T> group = itemsMap.get(key);
            if(group == null){
                group = new ItemGroup<>(section.getSectionType().getCreator(), key, item);
                itemsMap.put(key, group);
            }else {
                group.add(item);
            }
        }
    }
    public int size(){
        return itemsMap.size();
    }
    public void load(){
        Section<T> section = this.section;
        Map<String, ItemGroup<T>> itemsMap = this.itemsMap;
        Creator<T> creator = section.getSectionType().getCreator();
        T[] items = section.getItemArray().getChildes();
        int length = items.length;
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(item == null){
                continue;
            }
            String key = item.getKey();
            if(key == null){
                continue;
            }
            ItemGroup<T> group = itemsMap.get(key);
            if(group == null){
                group = new ItemGroup<>(creator, key, item);
                itemsMap.put(key, group);
            }else {
                group.add(item);
            }
        }
    }

    @Override
    public String toString() {
        return section.getSectionType().getName() + "-Pool = " + size();
    }
}
