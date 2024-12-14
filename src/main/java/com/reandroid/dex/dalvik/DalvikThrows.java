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

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.*;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.program.MethodProgram;
import com.reandroid.utils.StringsUtil;

public class DalvikThrows extends DalvikAnnotation {

    private DalvikThrows(AnnotatedProgram annotatedProgram) {
        super(annotatedProgram, TypeKey.DALVIK_Throws);
    }

    public void add(TypeKey typeKey) {
        ArrayKey<TypeKey> arrayKey = getThrows();
        if (!arrayKey.contains(typeKey)) {
            arrayKey = arrayKey.add(typeKey);
            setThrows(arrayKey);
        }
    }
    public boolean contains(TypeKey typeKey) {
        return getThrows().contains(typeKey);
    }
    public boolean remove(TypeKey typeKey) {
        ArrayKey<TypeKey> arrayKey = getThrows();
        if (arrayKey.contains(typeKey)) {
            arrayKey = arrayKey.remove(typeKey);
            setThrows(arrayKey);
            return true;
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public ArrayKey<TypeKey> getThrows() {
        Key key = readValue(Key.DALVIK_value);
        if (!(key instanceof ArrayKey)) {
            return ArrayKey.empty();
        }
        return (ArrayKey<TypeKey>) key;
    }
    public void setThrows(ArrayKey<TypeKey> typeKeys) {
        writeValue(Key.DALVIK_value, ArrayValueKey.create(typeKeys));
    }

    @Override
    public MethodProgram getAnnotatedProgram() {
        return (MethodProgram) super.getAnnotatedProgram();
    }

    @Override
    public String toString() {
        return StringsUtil.join(getThrows().iterator(), ", ");
    }

    public static DalvikThrows of(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram.hasAnnotation(TypeKey.DALVIK_Throws)) {
            return new DalvikThrows(annotatedProgram);
        }
        return null;
    }
    public static DalvikThrows getOrCreate(AnnotatedProgram annotatedProgram) {
        if (!annotatedProgram.hasAnnotation(TypeKey.DALVIK_Throws)) {
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_Throws,
                    AnnotationElementKey.create(Key.DALVIK_value, ArrayValueKey.empty())
                    )
            );
        }
        return of(annotatedProgram);
    }
}
