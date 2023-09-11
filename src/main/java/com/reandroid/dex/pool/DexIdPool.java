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

import com.reandroid.dex.index.ItemId;
import com.reandroid.dex.sections.Section;

import java.util.HashMap;
import java.util.Map;

public class DexIdPool<T extends ItemId> {
    private final Section<T> section;
    private final Map<String, T> itemsMap;

    public DexIdPool(Section<T> section){
        this.section = section;
        this.itemsMap = new HashMap<>(section.getCount());
    }

    public void keyChanged(String old, T item){
        itemsMap.remove(old);
        itemsMap.put(item.getKey(), item);
    }
    public T getOrCreate(String key){
        T exist = itemsMap.get(key);
        if(exist == null){
            exist = section.getItemArray().createNext();
            exist.setKey(key);
            itemsMap.put(key, exist);
        }
        return exist;
    }
    public T get(String key){
        return itemsMap.get(key);
    }
    public void add(T item){
        String key = item.getKey();
        if(key != null){
            itemsMap.put(key, item);
        }
    }
    public int size(){
        return itemsMap.size();
    }
    public void load(){
        Section<T> section = this.section;
        Map<String, T> itemsMap = this.itemsMap;
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
            itemsMap.put(key, item);
        }
    }

    @Override
    public String toString() {
        return section.getSectionType().getName() + "-Pool = " + size();
    }
}
