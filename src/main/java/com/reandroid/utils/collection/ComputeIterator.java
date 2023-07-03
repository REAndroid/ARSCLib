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
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ComputeIterator<E, T> implements Iterator<T>, Function<E, T> {
    private final Iterator<? extends E> iterator;
    private T mNext;
    private final Predicate<E> mInputTester;
    private final Predicate<T> mOutputTester;

    public ComputeIterator(Iterator<? extends E> iterator, Predicate<E> inputTester, Predicate<T> outputTester){
        this.iterator = iterator;
        this.mInputTester = inputTester;
        this.mOutputTester = outputTester;
    }
    public ComputeIterator(Iterator<? extends E> iterator, Predicate<T> outputTester){
        this(iterator, null, outputTester);
    }
    public ComputeIterator(Iterator<? extends E> iterator){
        this(iterator, null, null);
    }

    @Override
    public abstract T apply(E element);

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
        if(mNext == null) {
            while (iterator.hasNext()) {
                E input = iterator.next();
                if(!testInput(input)){
                    continue;
                }
                T output = apply(input);
                if (testOutput(output)) {
                    mNext = output;
                    break;
                }
            }
        }
        return mNext;
    }
    private boolean testInput(E input){
        if(input == null){
            return false;
        }
        return mInputTester == null
                || mInputTester.test(input);
    }
    private boolean testOutput(T output){
        if(output == null){
            return false;
        }
        return mOutputTester == null
                || mOutputTester.test(output);
    }
}
