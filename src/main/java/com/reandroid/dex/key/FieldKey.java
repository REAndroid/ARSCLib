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
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;

public class FieldKey implements Key {

    private final TypeKey declaring;
    private final StringKey name;
    private final TypeKey type;

    private FieldKey(TypeKey declaring, StringKey name, TypeKey type) {
        this.declaring = declaring;
        this.name = name;
        this.type = type;
    }

    @Override
    public TypeKey getDeclaring() {
        return declaring;
    }
    public StringKey getNameKey() {
        return name;
    }
    public String getName() {
        return getNameKey().getString();
    }
    public TypeKey getType() {
        return type;
    }

    public FieldKey changeDeclaring(TypeKey typeKey) {
        if (typeKey.equals(getDeclaring())) {
            return this;
        }
        return create(typeKey, getNameKey(), getType());
    }
    public FieldKey changeName(String name) {
        if (name.equals(getName())) {
            return this;
        }
        return create(getDeclaring(), name, getType());
    }
    public FieldKey changeName(StringKey name) {
        if (name.equals(getNameKey())) {
            return this;
        }
        return create(getDeclaring(), name, getType());
    }
    public FieldKey changeType(TypeKey typeKey) {
        if (typeKey.equals(getType())) {
            return this;
        }
        return create(getDeclaring(), getNameKey(), typeKey);
    }

    @Override
    public Iterator<Key> mentionedKeys() {
        return CombiningIterator.singleThree(
                FieldKey.this,
                SingleIterator.of(getDeclaring()),
                SingleIterator.of(getNameKey()),
                SingleIterator.of(getType()));
    }

    @Override
    public FieldKey replaceKey(Key search, Key replace) {
        FieldKey result = this;
        if(search.equals(result)) {
            return (FieldKey) replace;
        }
        if(search.equals(result.getDeclaring())){
            result = result.changeDeclaring((TypeKey) replace);
        }
        if (replace instanceof StringKey && search.equals(result.getNameKey())) {
            result = result.changeName((StringKey) replace);
        }
        if (replace instanceof TypeKey && search.equals(result.getType())) {
            result = result.changeType((TypeKey) replace);
        }
        return result;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getDeclaring().append(writer);
        writer.append('-');
        writer.append('>');
        getNameKey().appendSimpleName(writer);
        writer.append(':');
        getType().append(writer);
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null) {
            return -1;
        }
        FieldKey key = (FieldKey) obj;
        int i = CompareUtil.compare(getDeclaring(), key.getDeclaring());
        if(i != 0) {
            return i;
        }
        i = CompareUtil.compare(getNameKey(), key.getNameKey());
        if (i != 0) {
            return i;
        }
        return CompareUtil.compare(getType(), key.getType());
    }

    public boolean equalsDeclaring(TypeKey declaring) {
        if(declaring == null){
            return false;
        }
        return getDeclaring().equals(declaring);
    }
    public boolean equalsDeclaring(FieldKey other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getDeclaring().equals(other.getDeclaring());
    }
    public boolean equalsIgnoreDeclaring(FieldKey other) {
        if( other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getName().equals(other.getName()) &&
                getType().equals(other.getType());
    }
    public boolean equalsIgnoreName(FieldKey other){
        if( other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getDeclaring().equals(other.getDeclaring()) &&
                getType().equals(other.getType());
    }
    public boolean equalsName(FieldKey other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getNameKey().equals(other.getNameKey());
    }
    public boolean equalsName(String name) {
        return ObjectsUtil.equals(getName(), name);
    }
    public boolean equalsType(TypeKey typeKey) {
        if (typeKey == null) {
            return false;
        }
        return getType().equals(typeKey);
    }
    public boolean equalsType(FieldKey other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return getType().equals(other.getType());
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FieldKey)) {
            return false;
        }
        FieldKey other = (FieldKey) obj;
        return getDeclaring().equals(other.getDeclaring()) &&
                getNameKey().equals(other.getNameKey()) &&
                getType().equals(other.getType());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getDeclaring(), getNameKey(), getType());
    }

    @Override
    public String toString() {
        return getDeclaring() + "->" + getNameKey().getAsSimpleName() + ':' + getType();
    }


    public static FieldKey create(TypeKey declaring, StringKey name, TypeKey type) {
        if (declaring == null || name == null || type == null) {
            return null;
        }
        return new FieldKey(declaring, name, type);
    }
    public static FieldKey create(TypeKey declaring, String name, TypeKey type) {
        return create(declaring, StringKey.create(name), type);
    }

    public static FieldKey parse(String text) {
        return parse(text, 0);
    }

    public static FieldKey parse(String text, int start) {
        if (start + 6 >= text.length() || text.charAt(start) != 'L') {
            return null;
        }

        int i = text.indexOf("->", start);
        if (i < 0) {
            return null;
        }

        TypeKey declaring = TypeKey.parseBinaryType(text, start, i);
        if (declaring == null) {
            return null;
        }
        start = start + declaring.getTypeName().length() + 2;
        i = text.indexOf(':', start);

        if (i < 0 || i == start) {
            return null;
        }

        String name = text.substring(start, i);
        start = start + name.length() + 1;
        i = text.length() - start;

        TypeKey type = TypeKey.parseBinaryType(text, start, i);

        return create(declaring, name, type);
    }
    public static FieldKey convert(Field field) {
        TypeKey declaring = TypeKey.convert(field.getDeclaringClass());
        TypeKey type = TypeKey.convert(field.getType());
        return create(declaring, field.getName(), type);
    }

    public static FieldKey read(SmaliReader reader) throws IOException {
        TypeKey declaring = TypeKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '-');
        SmaliParseException.expect(reader, '>');
        StringKey name = StringKey.readSimpleName(reader, ':');
        reader.skip(1);
        TypeKey type = TypeKey.read(reader);
        return create(declaring, name, type);
    }
}
