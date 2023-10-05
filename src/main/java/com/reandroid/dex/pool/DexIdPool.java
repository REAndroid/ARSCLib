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
import com.reandroid.arsc.base.Creator;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;

import java.util.HashMap;
import java.util.Map;

public class DexIdPool<T extends Block> {
    private final Section<T> section;
    private final Map<Key, KeyItemGroup<T>> itemsMap;
    private boolean keyItems;
    private boolean keyItemsCreate;
    private boolean keyItemsChecked;

    public DexIdPool(Section<T> section){
        this.section = section;
        this.itemsMap = new HashMap<>(section.getCount());
    }
    DexIdPool(){
        this.section = null;
        this.itemsMap = null;
    }

    public void onRemoving(T item){
        if(item instanceof KeyItem){
            Key key = ((KeyItem) item).getKey();
            KeyItemGroup<T> exist = itemsMap.get(key);
            if(exist != null && exist.contains(item)){
                itemsMap.remove(key);
                exist.remove(item);
                key = exist.getKey();
                if(key != null){
                    itemsMap.put(key, exist);
                }
            }
        }
    }
    public void keyChanged(Key old){
        if(old == null || !isKeyItems()){
            return;
        }
        KeyItemGroup<T> exist = itemsMap.remove(old);
        if(exist != null){
            T item = exist.first();
            KeyItem keyItem = (KeyItem) item;
            Key key = keyItem.getKey();
            itemsMap.put(key, exist);
        }
    }
    public T get(Key key){
        KeyItemGroup<T> group = getGroup(key);
        if(group != null){
            return group.matching(key);
        }
        return null;
    }
    public T getOrCreate(Key key){
        if(key == null || !isKeyItemsCreate()){
            return null;
        }
        KeyItemGroup<T>  exist = getGroup(key);
        if(exist == null) {
            T item = section.getItemArray().createNext();
            ((KeyItemCreate)item).setKey(key);
            exist = new KeyItemGroup<>(getSectionType().getCreator(), item);
            key = ((KeyItemCreate) item).getKey();
            itemsMap.put(key, exist);
        }
        return exist.matching(key);
    }
    public KeyItemGroup<T> getGroup(Key key){
        return itemsMap.get(key);
    }
    public void add(T item){
        if(!(item instanceof KeyItem) || item.getParent() == null){
            return;
        }
        Key key = ((KeyItem)item).getKey();
        if(key != null){
            KeyItemGroup<T> group = itemsMap.get(key);
            if(group == null){
                group = new KeyItemGroup<>(getSectionType().getCreator(), item);
                itemsMap.put(key, group);
            }else {
                group.add(item);
            }
        }
    }
    SectionType<T> getSectionType(){
        return section.getSectionType();
    }
    public int size(){
        return itemsMap.size();
    }
    public void load(){
        if(!isKeyItems()){
            return;
        }
        Section<T> section = this.section;
        Map<Key, KeyItemGroup<T>> itemsMap = this.itemsMap;
        itemsMap.clear();
        Creator<T> creator = section.getSectionType().getCreator();
        T[] items = section.getItemArray().getChildes();
        int length = items.length;
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(item == null){
                continue;
            }

            Key key = ((KeyItem)item).getKey();
            if(key == null){
                continue;
            }
            KeyItemGroup<T> group = itemsMap.get(key);
            if(group == null){
                group = new KeyItemGroup<>(creator, item);
                itemsMap.put(key, group);
            }else {
                group.add(item);
            }
        }
    }
    private boolean isKeyItemsCreate(){
        isKeyItems();
        return keyItemsCreate;
    }
    private boolean isKeyItems(){
        if(keyItemsChecked){
            return keyItems;
        }
        T sample = getSectionType().getCreator().newInstance();
        keyItemsChecked = true;
        keyItems = sample instanceof KeyItem;
        keyItemsCreate = sample instanceof KeyItemCreate;
        return keyItems;
    }

    @Override
    public String toString() {
        return getSectionType().getName() + "-Pool = " + size();
    }
}
