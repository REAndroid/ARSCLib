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

import com.reandroid.dex.data.DataItem;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.sections.DataSection;
import com.reandroid.dex.sections.Section;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class DataSectionPool<T extends DataItem> extends DexSectionPool<T>{

    public DataSectionPool(DataSection<T> section) {
        super(section);
    }
    DataSectionPool(DataSection<T> section, int initialCapacity) {
        super(section, initialCapacity);
    }

    @Override
    DataSection<T> getSection() {
        return (DataSection<T>) super.getSection();
    }
    public int clearDuplicates(){
        ArrayCollection<T> result = new ArrayCollection<>(size() / 10);
        Iterator<KeyItemGroup<T>> iterator = groupIterator();
        while (iterator.hasNext()){
            replaceDuplicates(result, iterator.next());
        }
        int size = result.size();
        for(T item : result){
            item.removeSelf();
        }
        result.clear();
        return size;
    }
    private void replaceDuplicates(ArrayCollection<T> result, KeyItemGroup<T> group){
        int size = group.size();
        if(size < 2){
            return;
        }
        T first = group.getFirst();
        Iterator<T> iterator = group.iterator(1);
        while (iterator.hasNext()){
            T item = iterator.next();
            if(item == first){
                continue;
            }
            item.setReplace(first);
            result.add(item);
        }
    }

    public Iterator<T> findDuplicates(){
        Iterator<KeyItemGroup<T>> iterator = groupIterator();
        return new IterableIterator<KeyItemGroup<T>, T>(iterator) {
            @Override
            public Iterator<T> iterator(KeyItemGroup<T> element) {
                return element.iterator(1);
            }
        };
    }

}
