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
import com.reandroid.common.ArraySupplier;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.utils.CompareUtil;

import java.util.*;

public class KeyItemGroup<T extends Block> extends ArrayCollection<T> implements ArraySupplier<T>, Comparator<T> {

    private boolean sorted;

    public KeyItemGroup() {
        super();
    }

    public T matching(Key key){
        sort();
        int length = size();
        if(length == 0){
            return null;
        }
        T best = get(0);
        for(int i = 1; i < length; i++) {
            T item = get(i);
            Key itemKey = ((KeyItem)item).getKey();
            int compare = CompareUtil.compare(key, itemKey);
            if(compare == 0){
                return item;
            }
            if(compare < 0){
                best = item;
            }
        }
        return best;
    }
    public Key getKey(){
        int size = size();
        if(size == 0){
            return null;
        }
        T first = get(0);
        if(first instanceof KeyItem){
            return ((KeyItem) first).getKey();
        }
        return null;
    }
    @Override
    public boolean add(T block) {
        boolean result = super.add(block);
        sorted = false;
        return result;
    }
    private void sort(){
        if(sorted){
            return;
        }
        sorted = true;
        int size = size();
        if(size < 2){
            return;
        }
        super.sort(this);
    }

    @Override
    public int compare(T item1, T item2) {
        if(item1 == item2){
            return 0;
        }
        if(item1 == null){
            return -1;
        }
        if(item2 == null){
            return 1;
        }
        int i = CompareUtil.compare(((KeyItem)item1).getKey(), ((KeyItem)item2).getKey());
        if(i != 0){
            return i;
        }
        return Integer.compare(item1.getIndex(), item2.getIndex());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof KeyItemGroup)){
            return false;
        }
        return Objects.equals(getKey(), ((KeyItemGroup<?>) obj).getKey());
    }

    @Override
    public int hashCode() {
        Key key = getKey();
        if(key != null){
            return 31 * key.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        Key key = getKey();
        return size() + " {" + key + "}";
    }

    public static String EMPTY = "";
}
