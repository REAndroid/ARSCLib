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

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class KeyPair<T1 extends Key, T2 extends Key> 
        implements SmaliFormat, Comparable<KeyPair<Key, Key>> {

    private T1 first;
    private T2 second;

    public KeyPair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    public KeyPair(T1 first) {
        this(first, null);
    }
    public KeyPair() {
        this(null, null);
    }

    public T1 getFirst() {
        return first;
    }
    public void setFirst(T1 first) {
        this.first = first;
    }
    public T2 getSecond() {
        return second;
    }
    public void setSecond(T2 second) {
        this.second = second;
    }

    public KeyPair<T2, T1> flip() {
        return new KeyPair<>(getSecond(), getFirst());
    }
    public boolean isValid() {
        T1 t1 = getFirst();
        if (t1 == null) {
            return false;
        }
        T2 t2 = getSecond();
        if (t2 == null) {
            return false;
        }
        return !t1.equals(t2);
    }
    public boolean isInstance(Class<?> instance) {
        Key first = getFirst();
        if (first != null && !instance.isInstance(first)) {
            return false;
        }
        Key second = getFirst();
        if (second != null && !instance.isInstance(second)) {
            return false;
        }
        return first != null && second != null;
    }
    public int getDeclaringInnerDepth() {
        TypeKey typeKey = getDeclaring();
        if (typeKey != null) {
            return typeKey.getInnerDepth();
        }
        return -1;
    }
    public TypeKey getDeclaring() {
        Key key = getFirst();
        if (key != null) {
            return key.getDeclaring();
        }
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        Key key = getFirst();
        if (key == null) {
            key = NullValueKey.INSTANCE;
        }
        key.append(writer);
        writer.append('=');
        key = getSecond();
        if (key == null) {
            key = NullValueKey.INSTANCE;
        }
        key.append(writer);
    }

    @Override
    public int compareTo(KeyPair<Key, Key> pair) {
        if (pair == null) {
            return -1;
        }
        if (pair == this) {
            return 0;
        }
        int i = CompareUtil.compare(this.getFirst(), pair.getFirst());
        if (i == 0) {
            i = CompareUtil.compare(this.getSecond(), pair.getSecond());
        }
        return i;
    }

    public boolean equalsBoth(KeyPair<?, ?> keyPair) {
        if (this == keyPair) {
            return true;
        }
        return ObjectsUtil.equals(getFirst(), keyPair.getFirst()) &&
                ObjectsUtil.equals(getSecond(), keyPair.getSecond());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyPair)) {
            return false;
        }
        KeyPair<?, ?> keyPair = (KeyPair<?, ?>) obj;
        return ObjectsUtil.equals(getFirst(), keyPair.getFirst());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getFirst());
    }
    @Override
    public String toString() {
        return getFirst() + "=" + getSecond();
    }

    public static KeyPair<?, ?> parse(String text) {
        if (text == null) {
            return null;
        }
        SmaliReader reader = SmaliReader.of(text);
        reader.skipWhitespaces();
        if (reader.finished()) {
            return null;
        }
        byte b = reader.get();
        if (b == '#' || b == '/') {
            return null;
        }
        if (reader.indexOfBeforeLineEnd('=') < 0) {
            return null;
        }
        try {
            return read(reader);
        } catch (IOException ignored) {
            return null;
        }
    }
    public static KeyPair<?, ?> read(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        Key first = KeyUtil.readKey(reader);
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '=');
        reader.skipWhitespaces();
        if (reader.finished()) {
            throw new SmaliParseException(
                    "Finished reading but expecting second key", reader);
        }
        Key second = KeyUtil.readKey(reader);
        if (first == NullValueKey.INSTANCE) {
            first = null;
        }
        if (second == NullValueKey.INSTANCE) {
            second = null;
        }
        return new KeyPair<>(first, second);
    }
    public static<T extends Key, R extends Key> KeyPair<T, R> read(SmaliDirective directive, SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        Key first = readKey(directive, reader);
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '=');
        reader.skipWhitespaces();
        Key second = readKey(directive, reader);
        if (first == NullValueKey.INSTANCE) {
            first = null;
        }
        if (second == NullValueKey.INSTANCE) {
            second = null;
        }
        return ObjectsUtil.cast(new KeyPair<>(first, second));
    }
    private static Key readKey(SmaliDirective directive, SmaliReader reader) throws IOException {
        if (reader.finished()) {
            throw new SmaliParseException(
                    "Finished reading", reader);
        }
        if (reader.get() == 'n') {
            return NullValueKey.read(reader);
        }
        if (directive == SmaliDirective.CLASS) {
            return TypeKey.read(reader);
        }
        if (directive == SmaliDirective.FIELD) {
            return FieldKey.read(reader);
        }
        if (directive == SmaliDirective.METHOD) {
            return MethodKey.read(reader);
        }
        throw new IllegalArgumentException("Unimplemented directive: " + directive);
    }

    public static<E extends Key> Iterator<KeyPair<E, E>> instancesOf(Class<E> instance, Iterator<KeyPair<?, ?>> iterator) {
        return ObjectsUtil.cast(FilterIterator.of(iterator, kp -> kp.isInstance(instance)));
    }
    public static<E1 extends Key, E2 extends Key> Iterator<KeyPair<E2, E1>> flip(Iterator<KeyPair<E1, E2>> iterator) {
        return ComputeIterator.of(iterator, KeyPair::flip);
    }
    public static<E1 extends Key, E2 extends Key> List<KeyPair<E2, E1>> flip(Collection<KeyPair<E1, E2>> list) {
        ArrayCollection<KeyPair<E2, E1>> results = new ArrayCollection<>(list.size());
        results.addAll(KeyPair.flip(list.iterator()));
        return results;
    }
}
