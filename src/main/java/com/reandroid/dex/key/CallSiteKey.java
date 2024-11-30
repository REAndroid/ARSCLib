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

import java.io.IOException;

public class CallSiteKey implements Key {

    private final MethodHandleKey methodHandle;
    private final StringKey name;
    private final ProtoKey proto;
    private final ArrayKey arguments;

    public CallSiteKey(MethodHandleKey methodHandle, StringKey name, ProtoKey proto, ArrayKey arguments) {
        this.methodHandle = methodHandle;
        this.name = name;
        this.proto = proto;
        this.arguments = arguments;
    }

    public MethodHandleKey getMethodHandle() {
        return methodHandle;
    }
    public StringKey getName() {
        return name;
    }
    public ProtoKey getProto() {
        return proto;
    }
    public ArrayKey getArguments() {
        return arguments;
    }
    public ArrayKey toArrayKey() {
        ArrayKey arguments = getArguments();
        int argumentsLength = arguments.size();
        Key[] elements = new Key[3 + arguments.size()];
        elements[0] = getMethodHandle();
        elements[1] = getName();
        elements[2] = getProto();
        for (int i = 0; i < argumentsLength; i++) {
            elements[i + 3] = arguments.get(i);
        }
        return ArrayKey.create(elements);
    }
    public CallSiteKey changeMethodHandle(MethodHandleKey methodHandle) {
        if (methodHandle.equals(getMethodHandle())) {
            return this;
        }
        return new CallSiteKey(methodHandle, getName(), getProto(), getArguments());
    }
    public CallSiteKey changeName(StringKey name) {
        if (name.equals(getName())) {
            return this;
        }
        return new CallSiteKey(getMethodHandle(), name, getProto(), getArguments());
    }
    public CallSiteKey changeProto(ProtoKey proto) {
        if (proto.equals(getProto())) {
            return this;
        }
        return new CallSiteKey(getMethodHandle(), getName(), proto, getArguments());
    }
    public CallSiteKey changeArguments(ArrayValueKey arguments) {
        if (arguments.equals(getArguments())) {
            return this;
        }
        return new CallSiteKey(getMethodHandle(), getName(), getProto(), arguments);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('(');
        getName().append(writer);
        writer.append(", ");
        getProto().append(writer);
        ArrayKey arguments = getArguments();
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
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof CallSiteKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        CallSiteKey key = (CallSiteKey) obj;
        int i = CompareUtil.compare(this.getMethodHandle(), key.getMethodHandle());
        if (i == 0) {
            i = CompareUtil.compare(this.getArguments(), key.getArguments());
            if (i == 0) {
                i = CompareUtil.compare(this.getName(), key.getName());
                if (i == 0) {
                    i = CompareUtil.compare(this.getProto(), key.getProto());
                }
            }
        }
        return i;
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
        return ObjectsUtil.equals(getMethodHandle(), other.getMethodHandle()) &&
                ObjectsUtil.equals(getName(), other.getName()) &&
                ObjectsUtil.equals(getProto(), other.getProto()) &&
                ObjectsUtil.equals(getArguments(), other.getArguments());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getMethodHandle(),
                getName(), getProto(), getArguments());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(getName());
        builder.append(", ");
        builder.append(getProto());
        ArrayKey arguments = getArguments();
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

    public static CallSiteKey read(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        String label = readCallSiteLabel(reader);
        SmaliParseException.expect(reader, '(');
        StringKey name = StringKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, ',');
        ProtoKey protoKey = ProtoKey.read(reader);
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, ',');
        ArrayKey arguments = ArrayKey.read(reader, ')');
        reader.skipWhitespacesOrComment();
        SmaliParseException.expect(reader, '@');
        MethodHandleKey methodHandleKey = MethodHandleKey.read(MethodHandleType.INVOKE_STATIC, reader);
        return new CallSiteKey(methodHandleKey, name, protoKey, arguments);
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
