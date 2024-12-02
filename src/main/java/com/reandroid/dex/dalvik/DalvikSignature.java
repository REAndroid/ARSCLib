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

public class DalvikSignature extends DalvikAnnotation {

    private DalvikSignature(ProgramElement programElement) {
        super(programElement, TypeKey.DALVIK_Signature);
    }

    public String[] values() {
        String[] values = getArrayKey().getStringValues();
        if (values == null) {
            values = new String[0];
        }
        return values;
    }
    public void values(String[] values) {
        setArrayKey(ArrayValueKey.of(values));
    }
    public ArrayValueKey getArrayKey() {
        ArrayValueKey key = (ArrayValueKey) getKey().getValue(Key.DALVIK_value);
        if (key == null) {
            key = ArrayValueKey.EMPTY;
        }
        return key;
    }
    public void setArrayKey(ArrayValueKey arrayKey) {
        setKey(getKey().add(Key.DALVIK_value, arrayKey));
    }

    public String getString() {
        String[] values = values();
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(value);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getString();
    }

    public static DalvikSignature of(ProgramElement programElement) {
        if (programElement.hasAnnotation(TypeKey.DALVIK_Signature)) {
            return new DalvikSignature(programElement);
        }
        return null;
    }
    public static DalvikSignature getOrCreate(ProgramElement programElement) {
        if (!programElement.hasAnnotation(TypeKey.DALVIK_Signature)) {
            programElement.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_Signature,
                    AnnotationElementKey.create(Key.DALVIK_value, ArrayValueKey.EMPTY)
                    )
            );
        }
        return of(programElement);
    }
}
