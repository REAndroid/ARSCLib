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
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.ArraySort;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationItemKey implements Key, Iterable<AnnotationElementKey> {

    private static final AnnotationElementKey[] EMPTY = new AnnotationElementKey[0];

    private final AnnotationVisibility visibility;
    private final TypeKey type;
    private final AnnotationElementKey[] elements;
    private final AnnotationElementKey[] sortedElements;

    public AnnotationItemKey(AnnotationVisibility visibility, TypeKey type, AnnotationElementKey[] elements) {
        if (elements == null || elements.length == 0) {
            elements = EMPTY;
        }
        this.visibility = visibility;
        this.type = type;
        this.elements = elements;
        this.sortedElements = sortElements(elements);
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
    public AnnotationElementKey get(String name) {
        AnnotationElementKey[] elements = this.elements;
        int length = elements.length;
        for (int i = 0; i < length; i++) {
            AnnotationElementKey elementKey = elements[i];
            if (elementKey != null && ObjectsUtil.equals(name, elementKey.getName())) {
                return elementKey;
            }
        }
        return null;
    }
    public AnnotationElementKey get(int i) {
        return elements[i];
    }
    public boolean isEmpty() {
        return elements.length == 0;
    }
    public int size() {
        return elements.length;
    }

    private AnnotationElementKey[] getSortedElements() {
        AnnotationElementKey[] elementKeys = this.sortedElements;
        if (elementKeys == null) {
            elementKeys = this.elements;
        }
        return elementKeys;
    }

    @Override
    public Iterator<AnnotationElementKey> iterator() {
        return ArrayIterator.of(this.elements);
    }

    public SmaliDirective getSmaliDirective() {
        if(hasVisibility()) {
            return SmaliDirective.ANNOTATION;
        }
        return SmaliDirective.SUB_ANNOTATION;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnnotationItemKey)) {
            return false;
        }
        AnnotationItemKey other = (AnnotationItemKey) obj;
        return ObjectsUtil.equals(this.getVisibility(), other.getVisibility()) &&
                ObjectsUtil.equals(this.getType(), other.getType()) &&
                ObjectsUtil.equalsArray(this.getSortedElements(), other.getSortedElements());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getVisibility(), getType()) * 31 +
                ObjectsUtil.hashElements(this.getSortedElements());
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }

    private static AnnotationElementKey[] sortElements(AnnotationElementKey[] elements) {
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
}
