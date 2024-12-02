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

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ArraySort;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class AnnotationSetKey extends KeyList<AnnotationItemKey> implements Key {

    private static final AnnotationItemKey[] EMPTY_ARRAY;
    public static final AnnotationSetKey EMPTY;

    static {
        AnnotationItemKey[] emptyArray = new AnnotationItemKey[0];
        EMPTY_ARRAY = emptyArray;
        EMPTY = new AnnotationSetKey(emptyArray);
    }

    private AnnotationSetKey(AnnotationItemKey[] elements) {
        super(elements);
    }

    public Iterator<TypeKey> getTypes() {
        return ComputeIterator.of(iterator(), AnnotationItemKey::getType);
    }
    public AnnotationSetKey removeElementIf(TypeKey typeKey, Predicate<? super AnnotationElementKey> predicate) {
        AnnotationSetKey result = this;
        AnnotationItemKey itemKey = result.get(typeKey);
        if (itemKey == null) {
            return result;
        }
        int i = result.indexOf(itemKey);
        itemKey = itemKey.removeIf(predicate);
        return result.set(i, itemKey);
    }
    public AnnotationSetKey removeElement(TypeKey typeKey, String name) {
        AnnotationSetKey result = this;
        AnnotationItemKey itemKey = result.get(typeKey);
        if (itemKey == null) {
            return result;
        }
        int i = result.indexOf(itemKey);
        itemKey = itemKey.remove(name);
        return result.set(i, itemKey);
    }
    public AnnotationSetKey changeType(TypeKey typeKey, TypeKey newType) {
        AnnotationSetKey result = this;
        AnnotationItemKey itemKey = result.get(typeKey);
        if (itemKey == null) {
            return result;
        }
        int i = result.indexOf(itemKey);
        itemKey = itemKey.changeType(newType);
        return result.set(i, itemKey).sorted();
    }
    public AnnotationSetKey renameElement(TypeKey typeKey, String name, String newName) {
        AnnotationSetKey result = this;
        AnnotationItemKey itemKey = result.get(typeKey);
        if (itemKey == null) {
            return result;
        }
        int i = result.indexOf(itemKey);
        itemKey = itemKey.rename(name, newName);
        return result.set(i, itemKey);
    }
    public AnnotationSetKey setAnnotation(TypeKey typeKey, String name, Key value) {
        AnnotationSetKey result = this;
        result = result.getOrCreate(typeKey);
        AnnotationItemKey itemKey = result.get(typeKey);
        int i = result.indexOf(itemKey);
        itemKey = itemKey.setValue(name, value);
        return result.set(i, itemKey);
    }
    public AnnotationSetKey getOrCreate(TypeKey typeKey, String name) {
        AnnotationSetKey result = this;
        result = result.getOrCreate(typeKey);
        AnnotationItemKey itemKey = result.get(typeKey);
        int i = result.indexOf(itemKey);
        itemKey = itemKey.getOrCreate(name);
        return result.set(i, itemKey);
    }
    public AnnotationSetKey setVisibility(TypeKey typeKey, AnnotationVisibility visibility) {
        AnnotationItemKey itemKey = get(typeKey);
        if (itemKey == null) {
            return this;
        }
        int i = indexOf(itemKey);
        itemKey = itemKey.changeVisibility(visibility);
        return set(i, itemKey);
    }
    public AnnotationSetKey getOrCreate(TypeKey typeKey) {
        AnnotationItemKey itemKey = get(typeKey);
        if (itemKey != null) {
            return this;
        }
        itemKey = AnnotationItemKey.create(AnnotationVisibility.BUILD, typeKey);
        return this.add(itemKey).sorted();
    }
    public Key getAnnotationValue(TypeKey typeKey, String name) {
        AnnotationItemKey itemKey = get(typeKey);
        if (itemKey != null) {
            return itemKey.get(name);
        }
        return null;
    }
    public boolean containsElement(TypeKey typeKey, String name) {
        AnnotationItemKey itemKey = get(typeKey);
        if (itemKey != null) {
            return itemKey.containsElement(name);
        }
        return false;
    }
    public boolean contains(TypeKey typeKey) {
        int length = size();
        for (int i = 0; i < length; i++) {
            AnnotationItemKey itemKey = get(i);
            if (itemKey != null && ObjectsUtil.equals(typeKey, itemKey.getType())) {
                return true;
            }
        }
        return false;
    }
    public AnnotationItemKey get(TypeKey typeKey) {
        int length = size();
        for (int i = 0; i < length; i++) {
            AnnotationItemKey itemKey = get(i);
            if (itemKey != null && ObjectsUtil.equals(typeKey, itemKey.getType())) {
                return itemKey;
            }
        }
        return null;
    }

    @Override
    public AnnotationSetKey add(AnnotationItemKey item) {
        if (item == null) {
            return this;
        }
        return this.remove(item.getType())
                .addUnchecked(item)
                .sorted();
    }
    private AnnotationSetKey addUnchecked(AnnotationItemKey item) {
        return (AnnotationSetKey) super.add(item);
    }
    @Override
    public AnnotationSetKey remove(AnnotationItemKey itemKey) {
        return (AnnotationSetKey) super.remove(itemKey);
    }
    @Override
    public AnnotationSetKey remove(int index) {
        return (AnnotationSetKey) super.remove(index);
    }
    @Override
    public AnnotationSetKey removeIf(Predicate<? super AnnotationItemKey> predicate) {
        return (AnnotationSetKey) super.removeIf(predicate);
    }
    public AnnotationSetKey remove(TypeKey typeKey) {
        return removeIf(item -> item.equalsType(typeKey));
    }
    @Override
    public AnnotationSetKey set(int i, AnnotationItemKey item) {
        return (AnnotationSetKey) super.set(i, item);
    }
    @Override
    public AnnotationSetKey sorted() {
        return (AnnotationSetKey) super.sorted();
    }

    @Override
    AnnotationSetKey newInstance(AnnotationItemKey[] elements) {
        return create(elements);
    }
    @Override
    AnnotationItemKey[] newArray(int length) {
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        return new AnnotationItemKey[length];
    }
    @Override
    AnnotationItemKey[] initializeSortedElements(AnnotationItemKey[] elements) {
        if (elements == null || elements.length < 2) {
            return null;
        }
        boolean needsSort = false;
        int length = elements.length;
        AnnotationItemKey previous  = elements[0];
        for (int i = 1; i < length; i ++) {
            AnnotationItemKey next = elements[i];
            if (CompareUtil.compare(previous, next) > 0) {
                needsSort = true;
                break;
            }
        }
        if (!needsSort) {
            return null;
        }
        elements = elements.clone();
        ArraySort.sort(elements, CompareUtil.getComparableComparator());
        return elements;
    }

    @Override
    public AnnotationSetKey replaceKey(Key search, Key replace) {
        return (AnnotationSetKey) super.replaceKey(search, replace);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.indentPlus();
        super.append(writer);
        writer.indentMinus();
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof AnnotationSetKey)) {
            return 0;
        }
        return compareElements((AnnotationSetKey) obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnnotationSetKey)) {
            return false;
        }
        return equalsElements((AnnotationSetKey) obj);
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }

    public static AnnotationSetKey create(AnnotationItemKey[] elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY;
        }
        elements = removeNulls(elements);
        if (elements.length == 0) {
            return EMPTY;
        }
        return new AnnotationSetKey(elements);
    }
    public static AnnotationSetKey create(Iterator<AnnotationItemKey> iterator) {
        ArrayCollection<AnnotationItemKey> elements = null;
        while (iterator.hasNext()) {
            AnnotationItemKey key = iterator.next();
            if (elements == null) {
                elements = new ArrayCollection<>();
            }
            elements.add(key);
        }
        if (elements == null) {
            return create((AnnotationItemKey[]) null);
        }
        return create(elements.toArrayFill(new AnnotationItemKey[elements.size()]));
    }
    public static AnnotationSetKey combined(Iterator<AnnotationSetKey> iterator) {
        ArrayCollection<AnnotationItemKey> elements = null;
        while (iterator.hasNext()) {
            AnnotationSetKey key = iterator.next();
            if (!key.isEmpty()) {
                if (elements == null) {
                    elements = new ArrayCollection<>();
                }
                elements.addAll(key.iterator());
            }
        }
        if (elements == null) {
            return create((AnnotationItemKey[]) null);
        }
        return create(elements.toArrayFill(new AnnotationItemKey[elements.size()]));
    }
    private static AnnotationItemKey[] removeNulls(AnnotationItemKey[] elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY_ARRAY;
        }
        int length = elements.length;
        int size = 0;
        for (int i = 0; i < length; i ++) {
            AnnotationItemKey key = elements[i];
            if (key != null) {
                size ++;
            }
        }
        if (size == length) {
            return elements;
        }
        if (size == 0) {
            return EMPTY_ARRAY;
        }
        AnnotationItemKey[] results = new AnnotationItemKey[size];
        int j = 0;
        for (int i = 0; i < length; i ++) {
            AnnotationItemKey key = elements[i];
            if (key != null) {
                results[j] = key;
                j ++;
            }
        }
        return results;
    }
}
