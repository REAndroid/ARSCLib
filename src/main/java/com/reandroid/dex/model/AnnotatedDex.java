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

import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ProgramKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.ProgramElement;

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
    default AnnotationItemKey getAnnotation(TypeKey typeKey) {
        return getProgramElement().getAnnotation(typeKey);
    }
    @Override
    default Key getAnnotationValue(TypeKey typeKey, String name) {
        return getProgramElement().getAnnotationValue(typeKey, name);
    }
    @Override
    default boolean hasAnnotations() {
        return getProgramElement().hasAnnotations();
    }
    @Override
    default void clearAnnotations() {
        getProgramElement().clearAnnotations();
    }
}
