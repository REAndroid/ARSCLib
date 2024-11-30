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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class KeyList<T extends Key> implements Key, Iterable<T> {

    private final T[] elements;
    private final T[] sortedElements;
    private int mHash;

    public KeyList(T[] elements) {
        this.elements = elements;
        this.sortedElements = initializeSortedElements(elements);
    }

    public T get(int i) {
        return elements[i];
    }
    public int size() {
        return elements.length;
    }
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public Iterator<T> iterator() {
        return ArrayIterator.of(this.elements);
    }

    public int indexOf(Object key) {
        T[] elements = this.elements;
        int length = elements.length;
        for (int i = 0; i < length; i++) {
            if (ObjectsUtil.equals(elements[i], key)) {
                return i;
            }
        }
        return -1;
    }
    public KeyList<T> set(int i, T item) {
        if (ObjectsUtil.equals(item, get(i))) {
            return this;
        }
        T[] elements = this.elements.clone();
        elements[i] = item;
        return newInstance(elements);
    }
    public KeyList<T> add(T item) {
        T[] elements = this.elements;
        int length = elements.length;
        T[] result = newArray(length + 1);
        for (int i = 0; i < length; i++) {
            result[i] = elements[i];
        }
        result[length] = item;
        return newInstance(result);
    }
    public KeyList<T> remove(T itemKey) {
        return remove(indexOf(itemKey));
    }
    public KeyList<T> remove(int index) {
        if (index < 0) {
            return this;
        }
        T[] elements = this.elements;
        int length = elements.length;
        if (index >= length) {
            return this;
        }
        if (length == 1) {
            return newInstance(newArray(0));
        }
        T[] result = newArray(length - 1);
        int j = 0;
        for (int i = 0; i < length; i++) {
            if (i != index) {
                result[j] = elements[i];
                j ++;
            }
        }
        return newInstance(result);
    }
    public KeyList<T> removeIf(Predicate<? super T> predicate) {
        T[] elements = this.elements;
        int length = elements.length;
        if (length == 0) {
            return this;
        }
        T[] filtered = null;
        int j = 0;
        for (int i = 0; i < length; i++) {
            T item = elements[i];
            if (predicate.test(item)) {
                if (filtered == null) {
                    filtered = elements.clone();
                    j = i;
                }
            } else if (filtered != null) {
                filtered[j] = item;
                j ++;
            }
        }
        if (filtered == null) {
            return this;
        }
        T[] result = newArray(j);
        length = result.length;
        for (int i = 0; i < length; i++) {
            result[i] = filtered[i];
        }
        return newInstance(result);
    }

    public T getDuplicate() {
        T[] elements = getSortedElements();
        int length = elements.length;
        if (length < 2) {
            return null;
        }
        T last = elements[0];
        for (int i = 1; i < length; i++) {
            T element = elements[i];
            if (ObjectsUtil.equals(last, element)) {
                return element;
            }
            last = element;
        }
        return null;
    }
    public KeyList<T> sorted() {
        T[] elements = this.sortedElements;
        if (elements == null) {
            return this;
        }
        return newInstance(elements);
    }
    private T[] getSortedElements() {
        T[] elements = this.sortedElements;
        if (elements == null) {
            elements = this.elements;
        }
        return elements;
    }
    T[] initializeSortedElements(T[] elements) {
        return null;
    }

    T[] getElements() {
        return elements;
    }
    abstract KeyList<T> newInstance(T[] elements);
    abstract T[] newArray(int length);

    @SuppressWarnings("unchecked")
    @Override
    public KeyList<T> replaceKey(Key search, Key replace) {
        if (this.equals(search)) {
            return (KeyList<T>) replace;
        }
        return replaceElements(search, replace);
    }
    @SuppressWarnings("unchecked")
    KeyList<T> replaceElements(Key search, Key replace) {
        KeyList<T> result = this;
        int size = result.size();
        for (int i = 0; i < size; i++) {
            T item = get(i);
            if (item == null) {
                continue;
            }
            item = (T) item.replaceKey(search, replace);
            result = result.set(i, item);
        }
        return result;
    }
    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return CombiningIterator.singleTwo(
                this,
                iterator(),
                new IterableIterator<T, Key>(iterator()) {
                    @Override
                    public Iterator<Key> iterator(T element) {
                        return ObjectsUtil.cast(element.mentionedKeys());
                    }
                });
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAll(iterator());
    }


    int compareElements(KeyList<T> keyList) {
        return CompareUtil.compare(getSortedElements(), keyList.getSortedElements());
    }
    boolean equalsElements(KeyList<T> keyList) {
        return ObjectsUtil.equalsArray(this.getSortedElements(), keyList.getSortedElements());
    }
    int getHashCode() {
        int hash = this.mHash;
        if (hash == 0) {
            hash = computeHash();
            this.mHash = hash;
        }
        return hash;
    }
    int computeHash() {
        return ObjectsUtil.hashElements(this.getSortedElements());
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }
    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }

    public static<T1 extends Key> boolean equalsIgnoreEmpty(KeyList<T1> key1, KeyList<T1> key2) {
        if (key1 == key2) {
            return true;
        }
        if (key1 == null || key1.isEmpty()) {
            return key2 == null || key2.isEmpty();
        }
        if (key2 == null || key2.isEmpty()) {
            return key1.isEmpty();
        }
        return key1.equalsElements(key2);
    }
}
