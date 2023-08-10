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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.dex.base.DexItemArray;
import com.reandroid.dex.header.CountAndOffset;

import java.util.Iterator;

public class DexSection<T extends Block>  extends ExpandableBlockContainer implements Iterable<T>{
    private final DexItemArray<T> itemArray;
    public DexSection(CountAndOffset countAndOffset, Creator<T> creator){
        super(1);
        this.itemArray = new DexItemArray<>(countAndOffset, creator);
        addChild(itemArray);
    }
    public T get(int index){
        return itemArray.get(index);
    }
    public int size(){
        return itemArray.childrenCount();
    }

    public T[] toArray(int[] indexes){
        if(indexes == null){
            return null;
        }
        int size = indexes.length;
        if(size == 0){
            return null;
        }
        DexItemArray<T> itemArray = this.itemArray;
        T[] results = itemArray.newInstance(size);
        for(int i = 0; i < size; i++){
            results[i] = itemArray.get(indexes[i]);
        }
        return results;
    }
    @Override
    public Iterator<T> iterator() {
        return itemArray.iterator();
    }
    @Override
    public String toString(){
        return "size=" + size();
    }
}
