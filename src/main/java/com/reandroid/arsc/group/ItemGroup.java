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
package com.reandroid.arsc.group;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockArrayCreator;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ItemGroup<T extends Block> {
    private final BlockArrayCreator<T> mBlockArrayCreator;
    private final String name;
    private T[] items;
    public ItemGroup(BlockArrayCreator<T> blockArrayCreator, String name, T firstItem){
        this.mBlockArrayCreator = blockArrayCreator;
        this.name = name;
        T[] items;
        if(firstItem != null){
            items = blockArrayCreator.newInstance(1);
            items[0] = firstItem;
        }else {
            items = blockArrayCreator.newInstance(0);
        }
        this.items = items;
    }
    public ItemGroup(BlockArrayCreator<T> blockArrayCreator, String name){
        this(blockArrayCreator, name, null);
    }

    public Iterator<T> iterator(){
        return iterator(false);
    }
    public Iterator<T> iterator(boolean skipNullBlock){
        if(size() == 0){
            return EmptyIterator.of();
        }
        if(!skipNullBlock){
            return new ArrayIterator<>(this.items);
        }
        return new ArrayIterator<>(this.items, new Predicate<T>() {
            @Override
            public boolean test(T item) {
                return !item.isNull();
            }
        });
    }
    public Iterator<T> iterator(Predicate<T> tester){
        if(size() == 0){
            return EmptyIterator.of();
        }
        return new ArrayIterator<>(this.items, tester);
    }
    public List<T> listItems(){
        return new AbstractList<T>() {
            private final int mSize = ItemGroup.this.size();
            @Override
            public T get(int i) {
                return ItemGroup.this.get(i);
            }

            @Override
            public int size() {
                return mSize;
            }
        };
    }
    public T first(){
        T[] items = getItems();
        if(items != null && items.length > 0){
            return items[0];
        }
        return null;
    }
    public T get(int i){
        T[] items = getItems();
        if(items != null && i >= 0 && i < items.length){
            return items[0];
        }
        return null;
    }
    public int size(){
        if(items == null){
            return 0;
        }
        return items.length;
    }
    public boolean contains(T block){
        T[] items = this.items;
        if(block == null || items == null){
            return false;
        }
        int length = items.length;
        for(int i = 0; i < length; i++){
            if(block == items[i]){
                return true;
            }
        }
        return false;
    }
    public void remove(T block){
        T[] items = this.items;
        if(block == null || items == null){
            return;
        }
        boolean found = false;
        int length = items.length;
        for(int i = 0; i < length; i++){
            if(block == items[i]){
                items[i] = null;
                found = true;
            }
        }
        if(found){
            trimToSize();
        }
    }
    public void add(T block){
        if(block == null){
            return;
        }
        T[] items = this.items;
        int index;
        if(items != null){
            index = items.length;
        }else {
            index = 0;
        }
        T[] update = createNew(index + 1);
        if(index != 0){
            System.arraycopy(items, 0, update, 0, index);
        }
        update[index] = block;
        this.items = update;
    }
    public T[] getItems(){
        return items;
    }
    private void trimToSize(){
        T[] items = this.items;
        int count = countNonNull();
        int length = items.length;
        if(count == length){
            return;
        }
        T[] update = createNew(count);
        int index = 0;
        for(int i = 0; i < length; i++){
            T block = items[i];
            if(block != null){
                update[index] = block;
                index++;
            }
        }
        this.items = update;
    }
    private int countNonNull(){
        int result = 0;
        for(T t:items){
            if(t!=null){
                result++;
            }
        }
        return result;
    }
    private T[] createNew(int len){
        return mBlockArrayCreator.newInstance(len);
    }
    @Override
    public int hashCode(){
        return 31 * name.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        if(obj instanceof StringGroup){
            return hashCode()==obj.hashCode();
        }
        return false;
    }
    @Override
    public String toString(){
        return size()+"{"+name+"}";
    }
}
