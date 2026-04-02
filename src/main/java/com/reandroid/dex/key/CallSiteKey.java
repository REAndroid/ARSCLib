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

import com.reandroid.dex.common.MethodHandleType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.Iterator;

public class CallSiteKey implements Key {

    private final NamedTypeKey nameAndType;
    private final BootstrapMethodKey bootstrap;

    private CallSiteKey(NamedTypeKey nameAndType, BootstrapMethodKey bootstrap) {
        this.nameAndType = nameAndType;
        this.bootstrap = bootstrap;
    }

    public NamedTypeKey getNameAndType() {
        return nameAndType;
    }
    public BootstrapMethodKey getBootstrap() {
        return bootstrap;
    }
    public MethodHandleKey getMethodHandle() {
        return getBootstrap().getMethodHandle();
    }
    public String getName() {
        return getNameAndType().getName();
    }
    public Key getType() {
        return getNameAndType().getType();
    }
    public ArrayKey<?> getArguments() {
        return getBootstrap().getArguments();
    }
    public ArrayKey<?> toArrayKey() {
        ArrayKey<?> arguments = getArguments();
        int argumentsLength = arguments.size();
        Key[] elements = new Key[3 + argumentsLength];
        elements[0] = getMethodHandle();
        NamedTypeKey namedTypeKey = getNameAndType();
        elements[1] = namedTypeKey.getNameKey();
        elements[2] = namedTypeKey.getType();
        for (int i = 0; i < argumentsLength; i++) {
            elements[i + 3] = arguments.get(i);
        }
        return ArrayKey.create(elements);
    }
    public CallSiteKey changeNamedKey(NamedTypeKey namedTypeKey) {
        if (namedTypeKey.equals(getNameAndType())) {
            return this;
        }
        return create(namedTypeKey, getBootstrap());
    }
    public CallSiteKey changeBootstrap(BootstrapMethodKey bootstrap) {
        if (bootstrap.equals(getBootstrap())) {
            return this;
        }
        return create(getNameAndType(), bootstrap);
    }
    public CallSiteKey changeMethodHandle(MethodHandleKey methodHandle) {
        return changeBootstrap(getBootstrap().changeMethodHandle(methodHandle));
    }
    public CallSiteKey changeName(StringKey name) {
        return changeNamedKey(getNameAndType().changeName(name));
    }
    public CallSiteKey changeType(TypeDescriptorKey type) {
        return changeNamedKey(getNameAndType().changeType(type));
    }
    public CallSiteKey changeArguments(ArrayKey<?> arguments) {
        return changeBootstrap(getBootstrap().changeArguments(arguments));
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('(');
        getNameAndType().getNameKey().append(writer);
        writer.append(", ");
        getType().append(writer);
        ArrayKey<?> arguments = getArguments();
        int size = arguments.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                writer.append(", ");
            }
            arguments.get(i).append(writer);
        }
        writer.append(')');
        getMethodHandle().append(writer, false);
    }

    @Override
    public Iterator<? extends Key> contents() {
        return CombiningIterator.singleTwo(this,
                getNameAndType().contents(),
                getBootstrap().contents());
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof CallSiteKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        CallSiteKey key = (CallSiteKey) obj;
        int i = CompareUtil.compare(this.getBootstrap(), key.getBootstrap());
        if (i == 0) {
            i = CompareUtil.compare(this.getNameAndType(), key.getNameAndType());
        }
        return i;
    }

    public boolean equals(NamedTypeKey nameAndType, BootstrapMethodKey bootstrap) {
        return getNameAndType().equals(nameAndType) &&
                getBootstrap().equals(bootstrap);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CallSiteKey)) {
            return false;
        }
        CallSiteKey other = (CallSiteKey) obj;
        return ObjectsUtil.equals(getBootstrap(), other.getBootstrap()) &&
                ObjectsUtil.equals(getNameAndType(), other.getNameAndType());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getBootstrap(), getNameAndType());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(getNameAndType().getNameKey());
        builder.append(", ");
        builder.append(getType());
        ArrayKey<?> arguments = getArguments();
        if (!arguments.isEmpty()) {
            builder.append(", ");
            builder.append(arguments.toString(", "));
        }
        builder.append(')');
        builder.append('@');
        MethodHandleKey handle = getMethodHandle();
        if (handle != null) {
            builder.append(handle.getMember());
        } else {
            builder.append("null");
        }
        return builder.toString();
    }

    public static CallSiteKey create(NamedTypeKey nameAndType, BootstrapMethodKey bootstrap) {
        if (nameAndType == null || bootstrap == null) {
            return null;
        }
        return new CallSiteKey(nameAndType, bootstrap);
    }
    public static CallSiteKey create(StringKey name, TypeDescriptorKey type, BootstrapMethodKey bootstrap) {
        if (name == null || type == null || bootstrap == null) {
            return null;
        }
        return create(NamedTypeKey.create(name, type), bootstrap);
    }
    public static CallSiteKey create(NamedTypeKey nameAndType, ArrayKey<?> arguments, MethodHandleKey methodHandle) {
        if (nameAndType == null || methodHandle == null) {
            return null;
        }
        return create(nameAndType, BootstrapMethodKey.create(arguments, methodHandle));
    }
    public static CallSiteKey create(StringKey name, TypeDescriptorKey type, ArrayKey<?> arguments, MethodHandleKey methodHandle) {
        if (name == null || type == null || methodHandle == null) {
            return null;
        }
        return create(name, type, BootstrapMethodKey.create(arguments, methodHandle));
    }
    public static CallSiteKey read(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        String label = readCallSiteLabel(reader);
        SmaliParseException.expect(reader, '(');
        NamedTypeKey nameAndType = NamedTypeKey.readCall(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, ',');
        ArrayKey<?> arguments = ArrayKey.read(reader, ')');
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '@');
        MethodHandleType handleType;
        if (nameAndType.getType() instanceof TypeKey) {
            handleType = MethodHandleType.STATIC_GET;
        } else {
            handleType = MethodHandleType.INVOKE_STATIC;
        }
        MethodHandleKey methodHandleKey = MethodHandleKey.read(handleType, reader);
        return create(nameAndType, arguments, methodHandleKey);
    }

    public static CallSiteKey parse(String text) {
        //FIXME
        throw new RuntimeException("CallSiteKey.parse not implemented");
    }
    private static String readCallSiteLabel(SmaliReader reader) throws IOException {
        int position = reader.position();
        if (reader.getASCII(position) == '(') {
            return null;
        }
        int i = reader.indexOfBeforeLineEnd('(');
        if (i < 0) {
            throw new SmaliParseException("Expecting call site", reader);
        }
        return reader.readString(i - position);
    }
}
