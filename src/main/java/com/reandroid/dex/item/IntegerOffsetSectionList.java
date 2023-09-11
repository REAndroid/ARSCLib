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
package com.reandroid.dex.item;

import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ArrayIterator;

import java.util.Iterator;

public class IntegerOffsetSectionList<T extends DexItem> extends IntegerList implements Iterable<T>{
    private final SectionType<T> sectionType;
    private T[] items;

    public IntegerOffsetSectionList(SectionType<T> sectionType) {
        super();
        this.sectionType = sectionType;
    }

    @Override
    public Iterator<T> iterator() {
        return ArrayIterator.of(items);
    }
    public T getItem(int i){
        if(i < 0){
            return null;
        }
        T[] items = this.items;
        if(items == null || i >= items.length){
            return null;
        }
        return items[i];
    }
    public T[] getItems() {
        return items;
    }

    @Override
    void onChanged() {
        super.onChanged();
        updateItems();
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        refreshItems();
    }

    private void refreshItems(){
        T[] items = this.items;
        if(isEmpty(items)){
            this.items = null;
            setSize(0);
            return;
        }
        int length = items.length;
        setSize(length, false);
        for(int i = 0; i < length; i++){
            T item = items[i];
            put(i, getData(item));
        }
    }
    private int getData(T item){
        if(item == null){
            return 0;
        }
        return item.getOffsetReference().get();
    }
    private void updateItems(){
        items = getAt(sectionType, toArray());
    }
    private boolean isEmpty(T[] items){
        if(items == null || items.length == 0){
            return true;
        }
        for(int i = 0; i < items.length; i++){
            if(items[i] != null){
                return false;
            }
        }
        return true;
    }
}
