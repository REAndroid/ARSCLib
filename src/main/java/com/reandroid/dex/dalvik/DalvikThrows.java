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
import com.reandroid.dex.program.MethodProgram;

public class DalvikThrows extends DalvikAnnotation {

    private DalvikThrows(MethodProgram methodProgram) {
        super(methodProgram, TypeKey.DALVIK_Throws);
    }
    public TypeKey getThrow() {
        Key key = readValue(Key.DALVIK_value);
        if (key == null || (key instanceof NullValueKey)) {
            return null;
        }
        return (TypeKey) key;
    }
    public void setThrow(TypeKey typeKey) {
        writeValue(Key.DALVIK_value, typeKey);
    }

    @Override
    public MethodProgram getProgramElement() {
        return (MethodProgram) super.getProgramElement();
    }

    @Override
    public String toString() {
        return String.valueOf(getThrow());
    }

    public static DalvikThrows of(MethodProgram methodProgram) {
        if (methodProgram.hasAnnotation(TypeKey.DALVIK_MemberClass)) {
            return new DalvikThrows(methodProgram);
        }
        return null;
    }
    public static DalvikThrows getOrCreate(MethodProgram methodProgram) {
        if (!methodProgram.hasAnnotation(TypeKey.DALVIK_Throws)) {
            methodProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_Throws,
                    AnnotationElementKey.create(Key.DALVIK_value, TypeKey.EXCEPTION)
                    )
            );
        }
        return of(methodProgram);
    }
}
