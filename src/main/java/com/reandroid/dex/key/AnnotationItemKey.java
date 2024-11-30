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
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class AnnotationItemKey extends KeyList<AnnotationElementKey> implements Key, Iterable<AnnotationElementKey> {

    private static final AnnotationElementKey[] EMPTY = new AnnotationElementKey[0];

    private final AnnotationVisibility visibility;
    private final TypeKey type;

    public AnnotationItemKey(AnnotationVisibility visibility, TypeKey type, AnnotationElementKey[] elements) {
        super(removeNulls(elements));
        this.visibility = visibility;
        this.type = type;
    }

    public boolean hasVisibility() {
        return getVisibility() != null;
    }
    public AnnotationVisibility getVisibility() {
        return visibility;
    }
    public TypeKey getType() {
        return type;
    }
    public AnnotationItemKey changeType(TypeKey typeKey) {
        if (typeKey.equals(getType())) {
            return this;
        }
        return new AnnotationItemKey(getVisibility(), getType(), getElements());
    }
    public AnnotationElementKey get(String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            AnnotationElementKey elementKey = get(i);
            if (elementKey != null && ObjectsUtil.equals(name, elementKey.getName())) {
                return elementKey;
            }
        }
        return null;
    }

    @Override
    public AnnotationItemKey add(AnnotationElementKey item) {
        return (AnnotationItemKey) super.add(item);
    }
    @Override
    public AnnotationItemKey remove(AnnotationElementKey itemKey) {
        return (AnnotationItemKey) super.remove(itemKey);
    }
    @Override
    public AnnotationItemKey remove(int index) {
        return (AnnotationItemKey) super.remove(index);
    }
    @Override
    public AnnotationItemKey removeIf(Predicate<? super AnnotationElementKey> predicate) {
        return (AnnotationItemKey) super.removeIf(predicate);
    }
    @Override
    public AnnotationItemKey set(int i, AnnotationElementKey item) {
        return (AnnotationItemKey) super.set(i, item);
    }

    @Override
    AnnotationItemKey newInstance(AnnotationElementKey[] elements) {
        return new AnnotationItemKey(getVisibility(), getType(), elements);
    }
    @Override
    AnnotationElementKey[] newArray(int length) {
        if (length == 0) {
            return EMPTY;
        }
        return new AnnotationElementKey[length];
    }
    @Override
    AnnotationElementKey[] initializeSortedElements(AnnotationElementKey[] elements) {
        if (elements == null || elements.length < 2) {
            return null;
        }
        boolean needsSort = false;
        int length = elements.length;
        AnnotationElementKey previous  = elements[0];
        for (int i = 1; i < length; i ++) {
            AnnotationElementKey next = elements[i];
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

    public SmaliDirective getSmaliDirective() {
        if(hasVisibility()) {
            return SmaliDirective.ANNOTATION;
        }
        return SmaliDirective.SUB_ANNOTATION;
    }

    @Override
    public AnnotationItemKey replaceKey(Key search, Key replace) {
        if (this.equals(search)) {
            return (AnnotationItemKey) replace;
        }
        AnnotationItemKey result = this;
        if(search.equals(getType())) {
            result = result.changeType((TypeKey) replace);
        }
        return (AnnotationItemKey) result.replaceElements(search, replace);
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return CombiningIterator.singleOne(
                getType(),
                super.mentionedKeys());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendOptional(getVisibility());
        getType().append(writer);
        writer.indentPlus();
        writer.appendAllWithDoubleNewLine(iterator());
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof AnnotationItemKey)) {
            return -1;
        }
        AnnotationItemKey itemKey = (AnnotationItemKey) obj;
        return CompareUtil.compare(getType(), itemKey.getType());
    }

    @Override
    int computeHash() {
        return ObjectsUtil.hash(getVisibility(), getType()) * 31 + super.computeHash();
    }

    public boolean equalsType(TypeKey typeKey) {
        return ObjectsUtil.equals(getType(), typeKey);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnnotationItemKey)) {
            return false;
        }
        AnnotationItemKey other = (AnnotationItemKey) obj;
        return this.hashCode() == other.hashCode() &&
                ObjectsUtil.equals(this.getVisibility(), other.getVisibility()) &&
                ObjectsUtil.equals(this.getType(), other.getType()) &&
                equalsElements(other);
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }


    public static AnnotationItemKey read(SmaliReader reader) throws IOException {
        //FIXME
        throw new RuntimeException("AnnotationItemKey.read not implemented");
    }
    public static AnnotationItemKey parse(String text) {
        //FIXME
        throw new RuntimeException("AnnotationItemKey.parse not implemented");
    }

    private static AnnotationElementKey[] removeNulls(AnnotationElementKey[] elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY;
        }
        int length = elements.length;
        int size = 0;
        for (int i = 0; i < length; i ++) {
            AnnotationElementKey key = elements[i];
            if (key != null) {
                size ++;
            }
        }
        if (size == length) {
            return elements;
        }
        if (size == 0) {
            return EMPTY;
        }
        AnnotationElementKey[] results = new AnnotationElementKey[size];
        int j = 0;
        for (int i = 0; i < length; i ++) {
            AnnotationElementKey key = elements[i];
            if (key != null) {
                results[j] = key;
                j ++;
            }
        }
        return results;
    }
}
