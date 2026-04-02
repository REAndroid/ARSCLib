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
package com.reandroid.dex.dalvik;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.AnnotationElementKey;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.ArrayKey;
import com.reandroid.dex.key.ArrayValueKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullValueKey;
import com.reandroid.dex.key.PrimitiveKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.AccessibleItem;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.program.MethodParameter;
import com.reandroid.dex.program.MethodProgram;
import com.reandroid.utils.NumbersUtil;

import java.lang.annotation.ElementType;

public class DalvikMethodParameters extends DalvikAnnotation {

    private static final int ACCESS_FLAGS_MASK = AccessFlag.FINAL.getValue()
            | AccessFlag.MANDATED.getValue() | AccessFlag.SYNTHETIC.getValue();

    private DalvikMethodParameters(MethodProgram methodProgram) {
        super(methodProgram, TypeKey.DALVIK_MethodParameters);
    }

    @Override
    public MethodProgram getAnnotatedProgram() {
        return (MethodProgram) super.getAnnotatedProgram();
    }

    public Data get(int i) {
        return get(getAnnotatedProgram().getParameter(i));
    }
    public Data get(MethodParameter parameter) {
        if (parameter == null) {
            return null;
        }
        int i = parameter.getDefinitionIndex();
        if (i < 0 || i >= size()) {
            return null;
        }
        return new Data(this, parameter);
    }
    public int getAccessFlags(int i) {
        ArrayValueKey value = getAccessFlags();
        if (value != null) {
            return value.getInt(i);
        }
        return 0;
    }
    public void setAccessFlags(int i, int flags) {
        flags = ACCESS_FLAGS_MASK & flags;
        if (flags == 0) {
            flags = AccessFlag.SYNTHETIC.getValue();
        }
        ensureSize(i + 1);
        writeValue(Key.DALVIK_accessFlags, getAccessFlags()
                .set(i, PrimitiveKey.of(flags)));
    }
    public String getName(int i) {
        ArrayValueKey value = getNames();
        if (value != null) {
            return value.getString(i);
        }
        return null;
    }
    public void setName(int i, String name) {
        ensureSize(i + 1);
        Key key;
        if (name == null) {
            key = NullValueKey.INSTANCE;
        } else {
            key = StringKey.create(name);
        }
        writeValue(Key.DALVIK_names, getNames().set(i, key));
    }
    public int size() {
        int size;
        Key value = readValue(Key.DALVIK_accessFlags);
        if (value instanceof ArrayKey) {
            size = ((ArrayKey<?>) value).size();
        } else {
            size = 0;
        }
        value = readValue(Key.DALVIK_names);
        if (value instanceof ArrayKey) {
            size = NumbersUtil.min(size, ((ArrayKey<?>) value).size());
        } else {
            size = 0;
        }
        return size;
    }
    public void setSize(int size) {
        if (size < 0) {
            size = 0;
        }
        ArrayKey<?> arrayKey;
        Key value = readValue(Key.DALVIK_accessFlags);
        if (value instanceof ArrayKey) {
            arrayKey = (ArrayKey<?>) value;
        } else {
            arrayKey = ArrayValueKey.empty();
        }
        arrayKey = arrayKey.setSize(size, PrimitiveKey.of(AccessFlag.SYNTHETIC.getValue()));
        writeValue(Key.DALVIK_accessFlags, arrayKey);

        value = readValue(Key.DALVIK_names);
        if (value instanceof ArrayKey) {
            arrayKey = (ArrayKey<?>) value;
        } else {
            arrayKey = ArrayValueKey.empty();
        }
        arrayKey = arrayKey.setSize(size, StringKey.EMPTY);
        writeValue(Key.DALVIK_names, arrayKey);
    }
    public void ensureSize(int size) {
        if (size > size()) {
            setSize(size);
        }
    }
    public ArrayValueKey getAccessFlags() {
        return ArrayValueKey.create((ArrayKey<?>) readValue(Key.DALVIK_accessFlags));
    }
    public ArrayValueKey getNames() {
        return ArrayValueKey.create((ArrayKey<?>) readValue(Key.DALVIK_names));
    }

    public static DalvikMethodParameters of(AnnotatedProgram methodProgram) {
        if (methodProgram instanceof MethodProgram &&
                methodProgram.hasAnnotation(TypeKey.DALVIK_MethodParameters)) {
            return new DalvikMethodParameters((MethodProgram) methodProgram);
        }
        return null;
    }
    public static DalvikMethodParameters getOrCreate(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram instanceof MethodProgram && !annotatedProgram.hasAnnotation(TypeKey.DALVIK_MethodParameters)) {
            int size = ((MethodProgram) annotatedProgram).getParametersCount();
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_MethodParameters,
                    AnnotationElementKey.create(Key.DALVIK_accessFlags,
                            ArrayValueKey.empty().setSize(size, PrimitiveKey.of(AccessFlag.SYNTHETIC.getValue()))),
                    AnnotationElementKey.create(Key.DALVIK_names,
                            ArrayValueKey.empty().setSize(size, StringKey.EMPTY))
                    )
            );
        }
        return of(annotatedProgram);
    }

    public static class Data implements AccessibleItem {

        private final DalvikMethodParameters dalvikMethodParameters;
        private final MethodParameter methodParameter;

        Data(DalvikMethodParameters dalvikMethodParameters, MethodParameter methodParameter) {
            this.dalvikMethodParameters = dalvikMethodParameters;
            this.methodParameter = methodParameter;
        }

        @Override
        public int getAccessFlagsValue() {
            return dalvikMethodParameters.getAccessFlags(
                    methodParameter.getDefinitionIndex());
        }
        @Override
        public void setAccessFlagsValue(int value) {
            dalvikMethodParameters.setAccessFlags(
                    methodParameter.getDefinitionIndex(), value);
        }
        public String getName() {
            return dalvikMethodParameters.getName(
                    methodParameter.getDefinitionIndex());
        }
        public void setName(String name) {
            dalvikMethodParameters.setName(
                    methodParameter.getDefinitionIndex(), name);
        }

        @Override
        public ElementType getElementType() {
            return ElementType.PARAMETER;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(Modifier.toString(getModifiers()));
            TypeKey typeKey = methodParameter.getKey();
            if (typeKey != null) {
                builder.append(typeKey.getSimpleName());
                builder.append(' ');
            }
            builder.append(getName());
            return builder.toString();
        }
    }
}
