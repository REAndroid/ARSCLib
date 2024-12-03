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

public class DalvikEnclosingMethod extends DalvikEnclosing<MethodKey> {

    private DalvikEnclosingMethod(AnnotatedProgram annotatedProgram) {
        super(annotatedProgram, TypeKey.DALVIK_EnclosingMethod);
    }

    public static DalvikEnclosingMethod of(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram.hasAnnotation(TypeKey.DALVIK_EnclosingMethod)) {
            return new DalvikEnclosingMethod(annotatedProgram);
        }
        return null;
    }
    public static DalvikEnclosingMethod getOrCreate(AnnotatedProgram annotatedProgram) {
        if (!annotatedProgram.hasAnnotation(TypeKey.DALVIK_EnclosingMethod)) {
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_EnclosingMethod,
                    AnnotationElementKey.create(Key.DALVIK_value, NullValueKey.INSTANCE)
                    )
            );
        }
        return of(annotatedProgram);
    }
}
