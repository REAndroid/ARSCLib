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

import com.reandroid.dex.smali.SmaliWriter;

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

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('(');
        getName().append(writer);
        writer.append(", ");
        getProto().append(writer);
        ArrayKey arguments = getArguments();
        if (!arguments.isEmpty()) {
            writer.append(", ");
            getArguments().append(writer, ", ");
        }
        writer.append(')');
        getMethodHandle().append(writer, false);
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
}
