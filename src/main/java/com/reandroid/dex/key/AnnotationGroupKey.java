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

import com.reandroid.dex.program.MethodProgram;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.utils.collection.ArrayUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class AnnotationGroupKey extends AnnotationsKey<AnnotationSetKey> {

    private static final AnnotationGroupKey EMPTY = new AnnotationGroupKey(EMPTY_ARRAY);

    private AnnotationGroupKey(Key[] elements) {
        super(elements, false);
    }

    @Override
    public boolean isBlank() {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (!get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    public AnnotationGroupKey setParametersCount(int count) {
        if (count == 0 || isBlank()) {
            return empty();
        }
        return setSize(count);
    }
    public AnnotationGroupKey setSize(int size) {
        int current = size();
        if (size == current) {
            return this;
        }
        if (size == 0) {
            return empty();
        }
        Key[] elements = new Key[size];
        int min = NumbersUtil.min(current, size);
        for (int i = 0; i < min; i ++) {
            elements[i] = get(i);
        }
        AnnotationSetKey emptySetKey = null;
        for (int i = min; i < size; i++) {
            if (emptySetKey == null) {
                emptySetKey = AnnotationSetKey.empty();
            }
            elements[i] = emptySetKey;
        }
        return createNonNull(elements);
    }

    @Override
    public AnnotationGroupKey merge(AnnotationsKey<? extends AnnotationSetKey> item) {
        if (item == null || item == this || item.isBlank()) {
            return this;
        }
        if (this.isBlank()) {
            return (AnnotationGroupKey) item;
        }
        AnnotationGroupKey result = this;
        int size = item.size();
        for (int i = 0; i < size; i++) {
            result = result.merge(i, item.get(i));
        }
        return result;
    }
    @Override
    public AnnotationGroupKey merge(int i, AnnotationSetKey item) {
        return set(i, this.getOrEmpty(i).merge(item));
    }

    public AnnotationSetKey getOrEmpty(int i) {
        AnnotationSetKey key = get(i);
        if (key == null) {
            key = AnnotationSetKey.empty();
        }
        return key;
    }

    @Override
    public AnnotationSetKey get(int i) {
        return super.get(i);
    }
    @Override
    public AnnotationGroupKey add(AnnotationSetKey key) {
        return (AnnotationGroupKey) super.add(key);
    }
    @Override
    public AnnotationGroupKey remove(AnnotationSetKey key) {
        return (AnnotationGroupKey) super.remove(key);
    }
    @Override
    public AnnotationGroupKey remove(int index) {
        return (AnnotationGroupKey) super.remove(index);
    }
    public AnnotationGroupKey clearAt(int i) {
        if (i >= size()) {
            return this;
        }
        return set(i, AnnotationSetKey.empty());
    }
    @Override
    public AnnotationGroupKey set(int i, AnnotationSetKey item) {
        if (item == null) {
            item = AnnotationSetKey.empty();
        }
        int size = size();
        if (i < size) {
            return checkBlank((AnnotationGroupKey) super.set(i, item));
        }
        if (item.isEmpty() && this.isBlank()) {
            return empty();
        }
        int length = i + 1;
        Key[] elements = new Key[length];
        for (int j = 0; j < size; j++) {
            elements[j] = this.get(j);
        }
        AnnotationSetKey emptySetKey = AnnotationSetKey.empty();
        for (int j = size; j < length; j++) {
            elements[j] = emptySetKey;
        }
        elements[i] = item;
        return createNonNull(elements);
    }
    @Override
    public AnnotationGroupKey sorted() {
        return this;
    }
    @Override
    public AnnotationGroupKey clearDuplicates() {
        Key[] elements = getElements();
        Key[] results = null;
        int length = elements.length;
        for (int i = 0; i < length; i++) {
            AnnotationSetKey key = (AnnotationSetKey) elements[i];
            AnnotationSetKey cleared = key.clearDuplicates();
            if (key != cleared) {
                if (results == null) {
                    results = elements.clone();
                }
                results[i] = cleared;
            }
        }
        if (results == null) {
            return checkBlank(this);
        }
        return checkBlank(createNonNull(results));
    }
    @Override
    public AnnotationGroupKey clearDuplicates(Comparator<? super AnnotationSetKey> comparator) {
        return this;
    }

    @Override
    AnnotationGroupKey newInstance(Key[] elements) {
        return createKey(elements);
    }
    @Override
    public AnnotationGroupKey replaceKey(Key search, Key replace) {
        return (AnnotationGroupKey) super.replaceKey(search, replace);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        if (size == 0) {
            return;
        }
        TypeKey[] fakeParams = new TypeKey[size];
        ArrayUtil.fill(fakeParams, TypeKey.TYPE_I);
        append(false, ProtoKey.create(TypeKey.TYPE_V, fakeParams), writer);
    }
    public void append(MethodProgram methodProgram, SmaliWriter writer) throws IOException {
        append(methodProgram.isStatic(), methodProgram.getKey().getProto(), writer);
    }
    public void append(boolean is_static, ProtoKey protoKey, SmaliWriter writer) throws IOException {
        if (isBlank()) {
            return;
        }
        int size = NumbersUtil.min(this.size(), protoKey.getParametersCount());
        int instance = is_static? 0 : 1;
        for (int i = 0; i < size; i++) {
            AnnotationSetKey item = get(i);
            if (item.isEmpty()) {
                continue;
            }
            writer.newLine();
            SmaliDirective.PARAM.append(writer);
            writer.append('p');
            writer.appendInteger(instance + protoKey.getRegister(i));
            writer.appendComment(protoKey.getParameter(i).getTypeName());
            writer.indentPlus();
            item.append(writer);
            writer.indentMinus();
            SmaliDirective.PARAM.appendEnd(writer);
        }
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this, false);
    }

    public static AnnotationGroupKey empty() {
        return EMPTY;
    }
    public static AnnotationGroupKey empty(int length) {
        if (length == 0) {
            return EMPTY;
        }
        return createKey(new Key[length]);
    }
    public static AnnotationGroupKey of(AnnotationSetKey ... elements) {
        return createKey(elements);
    }
    public static AnnotationGroupKey combine(Iterator<AnnotationGroupKey> iterator) {
        AnnotationGroupKey result = empty();
        while (iterator.hasNext()) {
            result = result.merge(iterator.next());
        }
        return result;
    }

    private static AnnotationGroupKey checkBlank(AnnotationGroupKey groupKey) {
        if (groupKey == null || groupKey.isBlank()) {
            groupKey = empty();
        }
        return groupKey;
    }
    private static AnnotationGroupKey createKey(Key[]  elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        int length = elements.length;
        AnnotationSetKey emptySetKey = null;
        for (int i = 0; i < length; i++) {
            Key key = elements[i];
            if (key == null) {
                if (emptySetKey == null) {
                    emptySetKey = AnnotationSetKey.empty();
                }
                elements[i] = emptySetKey;
            }
        }
        return createNonNull(elements);
    }
    private static AnnotationGroupKey createNonNull(Key[]  elements) {
        return new AnnotationGroupKey(elements);
    }
}
