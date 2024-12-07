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

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;

public class DalvikSignatureKey implements Key {

    private final ArrayKey<ParameterisedTypeKey> elements;

    private DalvikSignatureKey(ArrayKey<ParameterisedTypeKey> elements) {
        this.elements = elements;
    }

    public Iterator<ParameterisedTypeKey> iterator() {
        return getElements().iterator();
    }
    public int size() {
        return getElements().size();
    }
    public ParameterisedTypeKey get(int i) {
        return getElements().get(i);
    }

    private ArrayKey<ParameterisedTypeKey> getElements() {
        return elements;
    }
    private DalvikSignatureKey changeElements(ArrayKey<ParameterisedTypeKey> elements) {
        if (getElements().equals(elements)) {
            return this;
        }
        return new DalvikSignatureKey(elements);
    }

    public Iterator<TypeKey> getTypes() {
        return new IterableIterator<ParameterisedTypeKey, TypeKey>(iterator()) {
            @Override
            public Iterator<TypeKey> iterator(ParameterisedTypeKey element) {
                return element.getTypes();
            }
        };
    }

    public ArrayValueKey toStringValues() {
        SignatureStringsBuilder builder = new SignatureStringsBuilder();
        ArrayKey<ParameterisedTypeKey> elements = getElements();
        int size  = elements.size();
        for (int i = 0; i < size; i++) {
            ParameterisedTypeKey key = elements.get(i);
            key.buildSignature(builder);
        }
        builder.flush();
        return builder.build();
    }

    @Override
    public DalvikSignatureKey replaceKey(Key search, Key replace) {
        if (search.equals(this)) {
            return (DalvikSignatureKey) replace;
        }
        ArrayKey<ParameterisedTypeKey> arrayKey = getElements();
        arrayKey = arrayKey.replaceKey(search, replace);
        return changeElements(arrayKey);
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return CombiningIterator.singleOne(this,
                new IterableIterator<ParameterisedTypeKey, Key>(iterator()) {
                    @Override
                    public Iterator<Key> iterator(ParameterisedTypeKey element) {
                        return ObjectsUtil.cast(element.mentionedKeys());
                    }
                });
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null) {
            return -1;
        }
        if (!(obj instanceof DalvikSignatureKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        DalvikSignatureKey key = (DalvikSignatureKey) obj;
        return CompareUtil.compare(getElements(), key.getElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DalvikSignatureKey)) {
            return false;
        }
        DalvikSignatureKey that = (DalvikSignatureKey) obj;
        return ObjectsUtil.equals(getElements(), that.getElements());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getElements());
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        ArrayKey<?> elements = getElements();
        int size = elements.size();
        for (int i = 0; i < size; i ++) {
            ((ParameterisedTypeKey) elements.get(i)).appendString(builder);
        }
        return builder.toString();
    }

    public static DalvikSignatureKey parseAnnotationValue(ArrayValueKey stringsKey) {
        if (stringsKey != null) {
            return parse(StringsUtil.join(stringsKey.stringValuesIterator(), ""));
        }
        return null;
    }
    public static DalvikSignatureKey parse(String text) {
        try {
            return read(SmaliReader.of(text));
        } catch (IOException ignored) {
            return null;
        }
    }
    public static DalvikSignatureKey read(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        Key[] elements = parseElements(reader);
        return new DalvikSignatureKey(ArrayKey.create(elements));
    }
    private static Key[] parseElements(SmaliReader reader) throws IOException {
        ArrayCollection<ParameterisedTypeKey> keyList = new ArrayCollection<>();
        while (!reader.finished() && !reader.skipWhitespaces()) {
            ParameterisedTypeKey typeKey = ParameterisedTypeKey.read(reader);
            keyList.add(typeKey);
        }
        return keyList.toArrayFill(new Key[keyList.size()]);
    }

}
