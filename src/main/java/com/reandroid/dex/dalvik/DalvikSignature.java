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
import com.reandroid.utils.StringsUtil;

import java.util.Iterator;

public class DalvikSignature extends DalvikAnnotation {

    private DalvikSignature(AnnotatedProgram annotatedProgram) {
        super(annotatedProgram, TypeKey.DALVIK_Signature);
    }

    public Iterator<String> values() {
        return getArrayKey().stringValuesIterator();
    }

    public ArrayValueKey getArrayKey() {
        ArrayValueKey key = (ArrayValueKey) getKey().getValue(Key.DALVIK_value);
        if (key == null) {
            key = ArrayValueKey.empty();
        }
        return key;
    }
    public void setArrayKey(ArrayValueKey arrayKey) {
        setKey(getKey().add(Key.DALVIK_value, arrayKey));
    }

    public String getString() {
        return StringsUtil.join(values(), "");
    }
    public DalvikSignatureKey getSignature() {
        return DalvikSignatureKey.parse(getString());
    }
    public void setSignature(DalvikSignatureKey key) {
        if (key != null) {
            setArrayKey(key.toStringValues());
        }
    }

    public void replace(TypeKey search, TypeKey replace) {
        DalvikSignatureKey signatureKey = getSignature();
        if (signatureKey != null) {
            setSignature(signatureKey.replaceKey(search, replace));
        }
    }

    @Override
    public String toString() {
        return getString();
    }

    public static DalvikSignature of(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram.hasAnnotation(TypeKey.DALVIK_Signature)) {
            return new DalvikSignature(annotatedProgram);
        }
        return null;
    }
    public static DalvikSignature getOrCreate(AnnotatedProgram annotatedProgram) {
        if (!annotatedProgram.hasAnnotation(TypeKey.DALVIK_Signature)) {
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_Signature,
                    AnnotationElementKey.create(Key.DALVIK_value, ArrayValueKey.empty())
                    )
            );
        }
        return of(annotatedProgram);
    }
    public static DalvikSignatureKey getSignature(AnnotatedProgram annotatedProgram) {
        DalvikSignature dalvikSignature = DalvikSignature.of(annotatedProgram);
        if (dalvikSignature != null) {
            return dalvikSignature.getSignature();
        }
        return null;
    }
}
