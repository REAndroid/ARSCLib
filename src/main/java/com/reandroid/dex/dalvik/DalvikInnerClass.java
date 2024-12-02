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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.*;
import com.reandroid.dex.program.ProgramElement;

import java.util.Iterator;

public class DalvikInnerClass extends DalvikAnnotation {

    private DalvikInnerClass(ProgramElement programElement) {
        super(programElement, TypeKey.DALVIK_InnerClass);
    }

    public Iterator<AccessFlag> getAccessFlags() {
        return AccessFlag.valuesOfClass(getAccessFlagsValue());
    }
    public int getAccessFlagsValue() {
        PrimitiveKey key = (PrimitiveKey) readValue(Key.DALVIK_accessFlags);
        return (int) key.getValueAsLong();
    }
    public void setAccessFlags(int flags) {
        writeValue(Key.DALVIK_accessFlags, PrimitiveKey.of(flags));
    }
    public String getName() {
        Key key = getKey().getValue(Key.DALVIK_name);
        if (key instanceof StringKey) {
            return ((StringKey) key).getString();
        }
        return null;
    }
    public void setName(String name) {
        Key key = name == null? NullValueKey.INSTANCE : StringKey.create(name);
        writeValue(Key.DALVIK_name, key);
    }

    @Override
    public String toString() {
        return AccessFlag.toString(getAccessFlags()) + getName();
    }

    public static DalvikInnerClass of(ProgramElement programElement) {
        if (programElement.hasAnnotation(TypeKey.DALVIK_InnerClass)) {
            return new DalvikInnerClass(programElement);
        }
        return null;
    }
    public static DalvikInnerClass getOrCreate(ProgramElement programElement) {
        if (!programElement.hasAnnotation(TypeKey.DALVIK_InnerClass)) {
            programElement.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_InnerClass,
                    AnnotationElementKey.create(Key.DALVIK_accessFlags, PrimitiveKey.of(0)),
                    AnnotationElementKey.create(Key.DALVIK_name, NullValueKey.INSTANCE)
                    )
            );
        }
        return of(programElement);
    }
}
