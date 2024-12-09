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
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;

public class TypeListKey extends KeyList<TypeKey> {

    private static final TypeListKey EMPTY = new TypeListKey(EMPTY_ARRAY);

    private TypeListKey(Key[] keys) {
        super(keys);
    }

    @Override
    public TypeListKey remove(int index) {
        return (TypeListKey) super.remove(index);
    }

    @Override
    TypeListKey newInstance(Key[] elements) {
        return TypeListKey.create(elements);
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
    public boolean contains(TypeKey typeKey) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (ObjectsUtil.equals(typeKey, get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeListKey replaceKey(Key search, Key replace) {
        return (TypeListKey) super.replaceKey(search, replace);
    }

    public void appendInterfaces(SmaliWriter writer) throws IOException {
        int size = size();
        if (size == 0) {
            return;
        }
        writer.newLine();
        writer.newLine();
        writer.appendComment("interfaces");
        SmaliDirective directive = SmaliDirective.IMPLEMENTS;
        for (int i = 0; i < size; i ++) {
            writer.newLine();
            directive.append(writer);
            get(i).append(writer);
        }
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof TypeListKey)) {
            return StringsUtil.compareToString(this, obj);
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

    public static TypeListKey empty() {
        return EMPTY;
    }
    public static TypeListKey create(Key ... keys) {
        keys = removeNulls(keys);
        if (keys == EMPTY_ARRAY) {
            return empty();
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
            return empty();
        }
        return create(keys.toArrayFill(new TypeKey[keys.size()]));
    }

    public static TypeListKey parseParameters(String parameters) {
        return parseParameters(parameters, 0, parameters.length());
    }
    public static TypeListKey parseParameters(String text, int start, int end) {
        if (end == start) {
            return empty();
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
}
