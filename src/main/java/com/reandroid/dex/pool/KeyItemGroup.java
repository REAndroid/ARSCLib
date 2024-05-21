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
import com.reandroid.dex.id.MethodId;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.utils.CompareUtil;

import java.util.*;

public class KeyItemGroup<T extends Block> extends ArrayCollection<T> implements ArraySupplier<T> {

    private boolean sorted;

    public KeyItemGroup() {
        super();
    }
    public KeyItemGroup(T item) {
        super(new Object[]{item});
    }

    public T matching(Key key){
        int length = size();
        if(length == 0){
            return null;
        }
        T result = null;
        for(int i = 0; i < length; i++) {
            T item = get(i);
            if(result == null){
                result = item;
            }
            Key itemKey = ((KeyItem)item).getKey();
            int compare = CompareUtil.compare(key, itemKey);
            if(compare == 0){
                return item;
            }
        }
        return result;
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
    private void sort(){
        if(sorted){
            return;
        }
        sorted = true;
        int size = size();
        if(size < 2){
            return;
        }
        super.sort((item1, item2) -> {
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
        });
        sorted = true;
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
    public void onChanged(){
        super.onChanged();
        sorted = false;
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
}
