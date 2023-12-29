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
import com.reandroid.dex.sections.DataSection;
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
