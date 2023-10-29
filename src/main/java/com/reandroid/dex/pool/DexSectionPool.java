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
import com.reandroid.dex.base.BlockListArray;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.sections.Section;


public class DexSectionPool<T extends Block> extends KeyPool<T>{

    private final Section<T> section;

    private boolean keyItems;
    private boolean keyItemsCreate;
    private boolean keyItemsChecked;

    DexSectionPool(Section<T> section, int initialCapacity){
        super(section.getSectionType(), initialCapacity);
        this.section = section;
    }
    public DexSectionPool(Section<T> section){
        this(section, section.getCount());
    }

    @Override
    public void remove(T item){
        if(isKeyItems()){
            super.remove(item);
        }
    }
    @SuppressWarnings("unchecked")
    public boolean update(Key key){
        Object obj = getItem(key);
        if(obj == null){
            return false;
        }
        remove(key);
        if(!(obj instanceof KeyItemGroup)){
            T item = (T)obj;
            key = ((KeyItem) item).getKey();
            put(key, item);
            return true;
        }
        KeyItemGroup<T> group = (KeyItemGroup<T>) obj;
        if(group.size() == 0){
            return false;
        }
        key = group.getKey();
        put(key, group);
        return true;
    }
    @SuppressWarnings("unchecked")
    public T getOrCreate(Key key){
        if(key == null || !isKeyItemsCreate()){
            return null;
        }
        Object obj = getItem(key);
        if(obj != null){
            if(obj instanceof KeyItemGroup){
                return  ((KeyItemGroup<T>)obj).matching(key);
            }else {
                return (T) obj;
            }
        }
        T item = createNext(key);
        add(item);
        return item;
    }
    public void load(){
        if(!isKeyItems()){
            return;
        }
        Section<T> section = this.getSection();
        BlockListArray<T> itemArray = section.getItemArray();
        int length = itemArray.size();
        reInitialize(length);
        for(int i = 0; i < length; i++){
            add(itemArray.get(i));
        }
        trimToSize();
    }
    T createNext(Key key){
        T item = getSection().createItem();
        ((KeyItemCreate) item).setKey(key);
        return item;
    }
    Section<T> getSection(){
        return this.section;
    }
    boolean isKeyItemsCreate(){
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
