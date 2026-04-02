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
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class NamedTypeKey implements Key {

    private final TypeKey declaring;
    private final StringKey nameKey;
    private final TypeDescriptorKey type;

    NamedTypeKey(TypeKey declaring, StringKey nameKey, TypeDescriptorKey type) {
        this.declaring = declaring;
        this.nameKey = nameKey;
        this.type = type;
    }

    @Override
    public TypeKey getDeclaring() {
        return declaring;
    }
    public String getName() {
        return nameKey.getString();
    }
    public StringKey getNameKey() {
        return nameKey;
    }
    public TypeDescriptorKey getType() {
        return type;
    }
    public NamedTypeKey changeDeclaring(TypeKey declaring) {
        if (ObjectsUtil.equals(getDeclaring(), declaring)) {
            return this;
        }
        return newInstance(declaring, getNameKey(), getType());
    }
    public NamedTypeKey changeName(String name) {
        return changeName(StringKey.create(name));
    }
    public NamedTypeKey changeName(StringKey name) {
        if (getNameKey().equals(name)) {
            return this;
        }
        return newInstance(getDeclaring(), name, getType());
    }
    public NamedTypeKey changeType(TypeDescriptorKey type) {
        if (getType().equals(type)) {
            return this;
        }
        return newInstance(getDeclaring(), getNameKey(), type);
    }

    protected NamedTypeKey newInstance(TypeKey declaring, StringKey nameKey, TypeDescriptorKey type) {
        return NamedTypeKey.create(declaring, nameKey, type);
    }
    public NamedTypeKey asNamedTypeKey() {
        if (getDeclaring() != null) {
            return NamedTypeKey.create(getName(), getType());
        }
        return this;
    }
    @Override
    public Iterator<? extends Key> contents() {
        return CombiningIterator.singleThree(this,
                SingleIterator.of(getDeclaring()),
                getNameKey().contents(),
                getType().contents());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getNameKey().append(writer);
        writer.append(", ");
        getType().append(writer);
    }
    public void appendDefinition(SmaliWriter writer) throws IOException {
        getNameKey().appendSimpleName(writer);
        TypeDescriptorKey type = getType();
        if (type instanceof TypeKey) {
            writer.append(':');
        }
        type.append(writer);
    }

    @Override
    public NamedTypeKey replaceKey(Key search, Key replace) {
        NamedTypeKey result = this;
        if (search.equals(result)) {
            return (NamedTypeKey) replace;
        }
        result = result.changeName((StringKey) getNameKey().replaceKey(search, replace));
        result = result.changeType((TypeDescriptorKey) getType().replaceKey(search, replace));
        return result;
    }
    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof NamedTypeKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        NamedTypeKey namedTypeKey = (NamedTypeKey) obj;
        int i = getNameKey().compareTo(namedTypeKey.getNameKey());
        if (i == 0) {
            i = getType().compareTo(namedTypeKey.getType());
        }
        return i;
    }
    public boolean equalsIgnoreName(NamedTypeKey namedTypeKey) {
        if (namedTypeKey == null) {
            return false;
        }
        if (namedTypeKey == this) {
            return true;
        }
        return ObjectsUtil.equals(getDeclaring(), namedTypeKey.getDeclaring()) &&
                getType().equals(namedTypeKey.getType());
    }
    public boolean equalsIgnoreDeclaring(NamedTypeKey namedTypeKey) {
        if (namedTypeKey == null) {
            return false;
        }
        if (namedTypeKey == this) {
            return true;
        }
        return getNameKey().equals(namedTypeKey.getNameKey()) &&
                getType().equals(namedTypeKey.getType());
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NamedTypeKey)) {
            return false;
        }
        NamedTypeKey namedTypeKey = (NamedTypeKey) obj;
        return getNameKey().equals(namedTypeKey.getNameKey()) &&
                getType().equals(namedTypeKey.getType());
    }
    @Override
    public int hashCode() {
        return getNameKey().hashCode() * 31 + getType().hashCode();
    }

    @Override
    public String toString() {
        return getNameKey() + ", " + getType();
    }
    public String toStringDefinition() {
        StringBuilder builder = new StringBuilder();
        builder.append(getNameKey().getAsSimpleName());
        Key type = getType();
        if (type instanceof TypeKey) {
            builder.append(':');
        }
        builder.append(type);
        return builder.toString();
    }

    public static NamedTypeKey create(StringKey name, TypeDescriptorKey type) {
        return create(null, name, type);
    }
    public static NamedTypeKey create(TypeKey declaring, StringKey name, TypeDescriptorKey type) {
        if (name == null || type == null || StringKey.EMPTY.equals(name)) {
            return null;
        }
        return new NamedTypeKey(declaring, name, type);
    }
    public static NamedTypeKey create(String name, TypeDescriptorKey type) {
        return create(null, name, type);
    }
    public static NamedTypeKey create(TypeKey declaring, String name, TypeDescriptorKey type) {
        if (StringsUtil.isEmpty(name) || type == null) {
            return null;
        }
        return create(declaring, StringKey.create(name), type);
    }
    public static NamedTypeKey readDefinition(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        StringKey name = StringKey.readSimpleName(reader);
        reader.skipWhitespacesOrComment();
        TypeDescriptorKey type;
        byte c = reader.get();
        if (c == ':') {
            reader.skip(1);
            type = TypeKey.read(reader);
        } else if (c == '(') {
            type = ProtoKey.read(reader);
        } else {
            throw new SmaliParseException("Unexpected character after: " + name, reader);
        }
        return create(name, type);
    }

    public static NamedTypeKey readCall(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        StringKey name = StringKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, ',');
        reader.skipWhitespacesOrComment();
        TypeDescriptorKey type;
        if (reader.get() == '(') {
            type = ProtoKey.read(reader);
        } else {
            type = TypeKey.read(reader);
        }
        return create(name, type);
    }
}
