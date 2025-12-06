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
import com.reandroid.dex.key.AnnotationElementKey;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.NullValueKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.program.ClassProgram;

public class DalvikEnclosingClass extends DalvikEnclosing<TypeKey> {

    private DalvikEnclosingClass(AnnotatedProgram annotatedProgram) {
        super(annotatedProgram, TypeKey.DALVIK_EnclosingClass);
    }

    public static DalvikEnclosingClass of(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram.hasAnnotation(TypeKey.DALVIK_EnclosingClass)) {
            return new DalvikEnclosingClass(annotatedProgram);
        }
        return null;
    }
    public static DalvikEnclosingClass getOrCreate(AnnotatedProgram annotatedProgram) {
        if (!annotatedProgram.hasAnnotation(TypeKey.DALVIK_EnclosingClass)) {
            Key key = null;
            if (annotatedProgram instanceof ClassProgram) {
                key = ((ClassProgram) annotatedProgram).getKey().getEnclosingClass();
            }
            if (key == null) {
                key = NullValueKey.INSTANCE;
            }
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_EnclosingClass,
                    AnnotationElementKey.create(Key.DALVIK_value, key)
                    )
            );
        }
        return of(annotatedProgram);
    }
}
