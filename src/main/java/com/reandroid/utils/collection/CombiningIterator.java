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

public class CombiningIterator<T> implements Iterator<T> {
    private final Iterator<T> iterator1;
    private final Iterator<T> iterator2;
    private boolean mFirstFinished;

    public CombiningIterator(Iterator<T> iterator1, Iterator<T> iterator2){
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
    }

    @Override
    public boolean hasNext() {
        if(!mFirstFinished){
            if(iterator1 != null && iterator1.hasNext()){
                return true;
            }
            mFirstFinished = true;
        }
        Iterator<T> iterator2 = this.iterator2;
        return iterator2 != null && iterator2.hasNext();
    }
    @Override
    public T next() {
        Iterator<T> current;
        if(mFirstFinished){
            current = iterator2;
        }else {
            current = iterator1;
        }
        if(current == null){
            throw new NoSuchElementException();
        }
        return current.next();
    }
}
