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
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.sections.Section;


public class DexSectionPool<T extends Block> extends KeyPool<T>{

    private final Section<T> section;

    private boolean keyItems;
    private boolean keyItemsCreate;
    private boolean keyItemsChecked;

    public DexSectionPool(Section<T> section){
        super(section.getSectionType(), section.getCount());
        this.section = section;
    }

    @Override
    public void remove(T item){
        if(isKeyItems()){
            super.remove(item);
        }
    }
    public boolean update(Key key){
        KeyItemGroup<T> group = remove(key);
        if(group == null || group.size() == 0){
            return false;
        }
        key = group.getKey();
        put(key, group);
        return true;
    }
    public T getOrCreate(Key key){
        if(key == null || !isKeyItemsCreate()){
            return null;
        }
        KeyItemGroup<T> exist = getGroup(key);
        if(exist == null) {
            T item = section.createItem();
            ((KeyItemCreate)item).setKey(key);
            exist = add(item);
        }
        return exist.matching(key);
    }
    public void load(){
        if(!isKeyItems()){
            return;
        }
        Section<T> section = this.section;
        T[] items = section.getItemArray().getChildes();
        int length = items.length;
        reInitialize(length);
        for(int i = 0; i < length; i++){
            add(items[i]);
        }
        trimToSize();
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
}
