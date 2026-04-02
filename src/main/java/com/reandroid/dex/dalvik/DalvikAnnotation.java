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
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;

public class DalvikAnnotation {

    private final AnnotatedProgram annotatedProgram;
    private final TypeKey annotationType;

    protected DalvikAnnotation(AnnotatedProgram annotatedProgram, TypeKey annotationType) {
        this.annotatedProgram = annotatedProgram;
        this.annotationType = annotationType;
    }

    public AnnotationItemKey getKey() {
        return getAnnotatedProgram().getAnnotation(getAnnotationType());
    }
    public void setKey(AnnotationItemKey key) {
        if (key == null) {
            getAnnotatedProgram().removeAnnotation(getAnnotationType());
        } else {
            if (!getAnnotationType().equals(key.getType())) {
                throw new IllegalArgumentException("Different annotation type: "
                        + getAnnotationType() + ", " + key.getType());
            }
            getAnnotatedProgram().addAnnotation(key);
        }
    }
    public AnnotationItemKey getOrCreateKey() {
        AnnotationItemKey key = getKey();
        if (key == null) {
            key = AnnotationItemKey.create(AnnotationVisibility.SYSTEM, getAnnotationType());
            setKey(key);
        }
        return key;
    }
    public TypeKey getAnnotationType() {
        return annotationType;
    }
    public AnnotatedProgram getAnnotatedProgram() {
        return annotatedProgram;
    }
    public void removeSelf() {
        getAnnotatedProgram().removeAnnotation(getAnnotationType());
    }
    public boolean isRemoved() {
        return !getAnnotatedProgram().hasAnnotation(getAnnotationType());
    }

    Key readValue(String name) {
        AnnotationItemKey key = getKey();
        if (key != null) {
            return key.getValue(name);
        }
        return null;
    }
    void writeValue(String name, Key value) {
        setKey(getOrCreateKey().add(name, value));
    }

    @Override
    public String toString() {
        AnnotationItemKey key = getKey();
        if (key == null) {
            return "# REMOVED";
        }
        return key.toString();
    }

    public static DalvikAnnotation of(TypeKey dalvikAnnotationType, AnnotatedProgram annotatedProgram) {
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_EnclosingClass)) {
            return DalvikEnclosingClass.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_EnclosingMethod)) {
            return DalvikEnclosingMethod.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_InnerClass)) {
            return DalvikInnerClass.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_MemberClass)) {
            return DalvikMemberClass.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_MethodParameters)) {
            return DalvikMethodParameters.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_Signature)) {
            return DalvikSignature.of(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_Throws)) {
            return DalvikThrows.of(annotatedProgram);
        }
        return null;
    }
    public static DalvikAnnotation getOrCreate(TypeKey dalvikAnnotationType, AnnotatedProgram annotatedProgram) {
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_AnnotationDefault)) {
            return DalvikAnnotationDefault.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_EnclosingClass)) {
            return DalvikEnclosingClass.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_EnclosingMethod)) {
            return DalvikEnclosingMethod.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_InnerClass)) {
            return DalvikInnerClass.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_MemberClass)) {
            return DalvikMemberClass.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_MethodParameters)) {
            return DalvikMethodParameters.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_Signature)) {
            return DalvikSignature.getOrCreate(annotatedProgram);
        }
        if (dalvikAnnotationType.equals(TypeKey.DALVIK_Throws)) {
            return DalvikThrows.getOrCreate(annotatedProgram);
        }
        return null;
    }
}
