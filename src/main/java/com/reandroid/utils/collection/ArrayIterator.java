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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ArrayIterator<T> implements Iterator<T> {
    private final T[] elements;
    private final Predicate<T> mTester;
    private int index;
    private T mNext;

    public ArrayIterator(T[] elements, Predicate<T> tester){
        this.elements = elements;
        this.mTester = tester;
    }
    public ArrayIterator(T[] elements){
        this(elements, null);
    }

    @Override
    public boolean hasNext() {
        return getNext() != null;
    }
    @Override
    public T next() {
        T item = getNext();
        if(item == null){
            throw new NoSuchElementException();
        }
        mNext = null;
        return item;
    }
    private T getNext(){
        T[] elements = this.elements;
        if(mNext == null && elements != null) {
            while (index < elements.length) {
                T item = elements[index];
                index ++;
                if (testAll(item)) {
                    mNext = item;
                    break;
                }
            }
        }
        return mNext;
    }
    private boolean testAll(T item){
        if(item == null){
            return false;
        }
        return mTester == null || mTester.test(item);
    }
}
