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
package com.reandroid.dex.model;

import com.reandroid.dex.key.ProgramKey;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.ProgramElement;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Iterator;

public interface AnnotatedDex extends ProgramElement {

    ProgramElement getProgramElement();

    @Override
    default ProgramKey getKey() {
        return getProgramElement().getKey();
    }
    @Override
    default AnnotationSetKey getAnnotation() {
        return getProgramElement().getAnnotation();
    }
    @Override
    default void setAnnotation(AnnotationSetKey annotationSet) {
        getProgramElement().setAnnotation(annotationSet);
    }

    @Override
    default boolean hasAnnotations() {
        return getProgramElement().hasAnnotations();
    }
    @Override
    default void clearAnnotations() {
        getProgramElement().clearAnnotations();
    }

    default Iterator<DexAnnotation> getDexAnnotations() {
        AnnotationSetKey annotation = getProgramElement().getAnnotation();
        return ComputeIterator.of(annotation.getTypes(), this::getDexAnnotation);
    }
    default DexAnnotation getDexAnnotation(TypeKey typeKey) {
        AnnotatedProgram annotatedProgram = getProgramElement();
        if (annotatedProgram.hasAnnotation(typeKey)) {
            return new DexAnnotation((Dex) this, annotatedProgram, typeKey);
        }
        return null;
    }
    default DexAnnotation getOrCreateDexAnnotation(TypeKey typeKey) {
        AnnotatedProgram annotatedProgram = getProgramElement();
        AnnotationSetKey annotationSetKey = annotatedProgram.getAnnotation();
        if (!annotationSetKey.contains(typeKey)) {
            annotationSetKey = annotationSetKey.getOrCreate(typeKey);
            annotatedProgram.setAnnotation(annotationSetKey);
        }
        return new DexAnnotation((Dex) this, annotatedProgram, typeKey);
    }

    default DexAnnotationElement getDexAnnotationElement(TypeKey typeKey, String name){
        DexAnnotation dexAnnotation = getDexAnnotation(typeKey);
        if(dexAnnotation != null) {
            return dexAnnotation.get(name);
        }
        return null;
    }
    default DexAnnotationElement getOrCreateDexAnnotationElement(TypeKey typeKey, String name) {
        DexAnnotation annotation = getOrCreateDexAnnotation(typeKey);
        return annotation.getOrCreate(name);
    }
}
