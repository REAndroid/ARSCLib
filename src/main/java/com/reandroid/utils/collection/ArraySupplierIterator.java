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
package com.reandroid.utils.collection;

import com.reandroid.common.ArraySupplier;

import java.util.Iterator;

public class ArraySupplierIterator<T> implements Iterator<T> {

    private final ArraySupplier<? extends T> supplier;
    private int index;

    public ArraySupplierIterator(ArraySupplier<? extends T> supplier){
        this.supplier = supplier;
    }
    @Override
    public boolean hasNext() {
        return index < supplier.getCount();
    }
    @Override
    public T next() {
        return this.supplier.get(index++);
    }

    public static<T1> Iterator<T1> of(ArraySupplier<? extends T1> supplier){
        if(supplier == null || supplier.getCount() == 0){
            return EmptyIterator.of();
        }
        return new ArraySupplierIterator<>(supplier);
    }
}
