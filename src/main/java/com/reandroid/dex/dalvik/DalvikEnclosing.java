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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullValueKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.utils.ObjectsUtil;

public abstract class DalvikEnclosing<T extends Key> extends DalvikAnnotation {

    public DalvikEnclosing(AnnotatedProgram annotatedProgram, TypeKey annotationType) {
        super(annotatedProgram, annotationType);
    }

    public T getEnclosing() {
        Key value = readValue(Key.DALVIK_value);
        if (value == null || (value instanceof NullValueKey)) {
            return null;
        }
        return ObjectsUtil.cast(value);
    }
    public void setEnclosing(T enclosing) {
        Key value = enclosing == null ? NullValueKey.INSTANCE : enclosing;
        writeValue(Key.DALVIK_value, value);
    }
    public TypeKey getEnclosingClass() {
        Key key = getEnclosing();
        if (key != null) {
            return key.getDeclaring();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(getEnclosing());
    }

    public static DalvikEnclosing<?> of(AnnotatedProgram annotatedProgram) {
        DalvikEnclosing<?> enclosing = DalvikEnclosingClass.of(annotatedProgram);
        if (enclosing == null) {
            enclosing = DalvikEnclosingMethod.of(annotatedProgram);
        }
        return enclosing;
    }
}
