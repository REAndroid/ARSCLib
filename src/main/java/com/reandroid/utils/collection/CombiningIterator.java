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

public class CombiningIterator<T, E extends T> implements Iterator<T> {
    private Iterator<? extends T> iterator1;
    private Iterator<? extends T> iterator2;
    private Iterator<Iterator<E>> iteratorIterator;
    private boolean mFirstFinished;
    private boolean mSecondFinished;
    private Iterator<? extends T> mSecond;
    private Iterator<? extends T> mCurrent;

    public CombiningIterator(Iterator<? extends T> iterator1, Iterator<? extends T> iterator2){
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
        this.iteratorIterator = null;
    }

    @Override
    public boolean hasNext() {
        return getCurrent() != null;
    }
    @Override
    public T next() {
        Iterator<? extends T> current = getCurrent();
        if(current == null){
            throw new NoSuchElementException();
        }
        mCurrent = null;
        return current.next();
    }
    private Iterator<? extends T> getCurrent() {
        Iterator<? extends T> current = mCurrent;
        if(current == null){
            current = loadCurrent();
            mCurrent = current;
        }
        return current;
    }
    private Iterator<? extends T> loadCurrent() {
        Iterator<? extends T> current = getFirst();
        if(current == null){
            current = getSecond();
        }
        return current;
    }

    private Iterator<? extends T> getSecond() {
        if(mSecondFinished){
            return null;
        }
        Iterator<? extends T> second = mSecond;
        if(second == null && iterator2 != null){
            second = iterator2;
            if(!second.hasNext()){
                iterator2 = null;
                mSecondFinished = true;
                return null;
            }
            return second;
        }
        second = iterator2;
        if(second != null){
            if(second.hasNext()){
                return second;
            }
            iterator2 = null;
            mSecondFinished = true;
            return null;
        }
        Iterator<Iterator<E>> iteratorIterator = this.iteratorIterator;
        if(iteratorIterator == null){
            mSecondFinished = true;
            return null;
        }
        second = mSecond;
        while (second == null || !second.hasNext()){
            if(!iteratorIterator.hasNext()){
                this.iteratorIterator = null;
                mSecond = null;
                mSecondFinished = true;
                return null;
            }
            second = iteratorIterator.next();
        }
        mSecond = second;
        return second;
    }
    private Iterator<? extends T> getFirst() {
        if(mFirstFinished){
            return null;
        }
        Iterator<? extends T> first = iterator1;
        if(first == null || !first.hasNext()){
            iterator1 = null;
            mFirstFinished = true;
            return null;
        }
        return first;
    }

    public static<T1, E1 extends T1> Iterator<T1> of(Iterator<T1> iterator1, Iterator<Iterator<E1>> iteratorIterator){
        if(!iteratorIterator.hasNext()){
            return iterator1;
        }
        CombiningIterator<T1, E1> iterator = new CombiningIterator<>(iterator1, null);
        iterator.iteratorIterator = iteratorIterator;
        return iterator;
    }
}
