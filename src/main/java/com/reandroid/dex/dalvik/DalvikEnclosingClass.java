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
import com.reandroid.dex.program.ProgramElement;

public class DalvikEnclosingClass extends DalvikEnclosing<TypeKey> {

    private DalvikEnclosingClass(ProgramElement programElement) {
        super(programElement, TypeKey.DALVIK_EnclosingClass);
    }

    public static DalvikEnclosingClass of(ProgramElement programElement) {
        if (programElement.hasAnnotation(TypeKey.DALVIK_EnclosingClass)) {
            return new DalvikEnclosingClass(programElement);
        }
        return null;
    }
    public static DalvikEnclosingClass getOrCreate(ProgramElement programElement) {
        if (!programElement.hasAnnotation(TypeKey.DALVIK_EnclosingClass)) {
            TypeKey typeKey = (TypeKey) programElement.getKey();
            typeKey = typeKey.getEnclosingClass();
            programElement.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_EnclosingClass,
                    AnnotationElementKey.create(Key.DALVIK_value, typeKey)
                    )
            );
        }
        return of(programElement);
    }
}
