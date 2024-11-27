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

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;

public class TypeListKey extends KeyList<TypeKey> {

    public static final TypeListKey EMPTY;
    private static final TypeKey[] EMPTY_ARRAY;

    static {
        TypeKey[] emptyArray = new TypeKey[0];
        EMPTY_ARRAY = emptyArray;
        EMPTY = new TypeListKey(emptyArray);
    }

    private TypeListKey(TypeKey[] keys) {
        super(keys);
    }

    @Override
    public TypeKey get(int i) {
        if (i >= 0 && i < size()) {
            return super.get(i);
        }
        return null;
    }

    @Override
    public TypeListKey remove(int index) {
        return (TypeListKey) super.remove(index);
    }

    @Override
    TypeListKey newInstance(TypeKey[] elements) {
        return TypeListKey.create(elements);
    }
    @Override
    TypeKey[] newArray(int length) {
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        return new TypeKey[length];
    }

    @Override
    public TypeListKey add(TypeKey typeKey) {
        return (TypeListKey) super.add(typeKey);
    }
    @Override
    public TypeListKey remove(TypeKey itemKey) {
        return (TypeListKey) super.remove(itemKey);
    }
    @Override
    public TypeListKey set(int i, TypeKey item) {
        return (TypeListKey) super.set(i, item);
    }

    @Override
    public TypeListKey replaceKey(Key search, Key replace) {
        return (TypeListKey) super.replaceKey(search, replace);
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (obj == null) {
            return -1;
        }
        return compareElements((TypeListKey) obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TypeListKey) {
            return equalsElements((TypeListKey) obj);
        }
        return false;
    }

    @Override
    public String toString() {
        return '(' + StringsUtil.join(iterator(), null) + ')';
    }

    public static TypeListKey create(TypeKey ... keys) {
        keys = removeNulls(keys);
        if (keys == EMPTY_ARRAY) {
            return EMPTY;
        }
        return new TypeListKey(keys);
    }


    public static TypeListKey readParameters(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '(');
        reader.skipWhitespacesOrComment();
        ArrayCollection<TypeKey> keys = null;
        while (!reader.finished() && reader.get() != ')') {
            if (keys == null) {
                keys = new ArrayCollection<>();
            }
            keys.add(TypeKey.read(reader));
            reader.skipWhitespacesOrComment();
        }
        SmaliParseException.expect(reader, ')');
        if (keys == null) {
            return EMPTY;
        }
        return create(keys.toArrayFill(new TypeKey[keys.size()]));
    }

    public static TypeListKey parseParameters(String parameters) {
        return parseParameters(parameters, 0, parameters.length());
    }
    public static TypeListKey parseParameters(String text, int start, int end) {
        if (end == start) {
            return EMPTY;
        }
        if (start > end) {
            return null;
        }
        ArrayCollection<TypeKey> results = new ArrayCollection<>();
        while (start < end) {
            TypeKey typeKey = TypeKey.parseBinaryType(text, start, end);
            if (typeKey == null) {
                return null;
            }
            results.add(typeKey);
            start = start + typeKey.getTypeName().length();
        }
        return create(results.toArrayFill(new TypeKey[results.size()]));
    }
    private static TypeKey[] removeNulls(TypeKey[] elements) {
        if (elements == null || elements.length == 0) {
            return EMPTY_ARRAY;
        }
        int length = elements.length;
        int size = 0;
        for (int i = 0; i < length; i ++) {
            TypeKey key = elements[i];
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
        TypeKey[] results = new TypeKey[size];
        int j = 0;
        for (int i = 0; i < length; i ++) {
            TypeKey key = elements[i];
            if (key != null) {
                results[j] = key;
                j ++;
            }
        }
        return results;
    }
}
