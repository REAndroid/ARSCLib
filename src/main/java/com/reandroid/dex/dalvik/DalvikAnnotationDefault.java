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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.program.ClassProgram;

import java.util.Iterator;

public class DalvikAnnotationDefault extends DalvikAnnotation {

    private DalvikAnnotationDefault(ClassProgram classProgram) {
        super(classProgram, TypeKey.DALVIK_AnnotationDefault);
    }

    public Iterator<AnnotationElementKey> values() {
        return getSubAnnotation().iterator();
    }
    public Key getValue(String name) {
        return getSubAnnotation().getValue(name);
    }
    public Key getValue(int index) {
        AnnotationElementKey elementKey = getSubAnnotation().get(index);
        if (elementKey != null) {
            return elementKey.getValue();
        }
        return null;
    }
    public void setValue(String name, Key value) {
        writeValue(Key.DALVIK_value, getSubAnnotation().setValue(name, value));
    }
    public void remove(String name) {
        writeValue(Key.DALVIK_value, getSubAnnotation().remove(name));
    }
    public int size() {
        return getSubAnnotation().size();
    }
    public AnnotationItemKey getSubAnnotation() {
        Key key = readValue(Key.DALVIK_value);
        if (!(key instanceof AnnotationItemKey)) {
            key = AnnotationItemKey.create(null, getAnnotatedProgram().getKey());
            writeValue(Key.DALVIK_value, key);
        }
        return (AnnotationItemKey) key;
    }
    @Override
    public ClassProgram getAnnotatedProgram() {
        return (ClassProgram) super.getAnnotatedProgram();
    }

    @Override
    public String toString() {
        Key key = readValue(Key.DALVIK_value);
        if (key == null) {
            return "NULL subannotation";
        }
        return key.toString();
    }

    public static DalvikAnnotationDefault of(AnnotatedProgram annotatedProgram) {
        if (annotatedProgram instanceof ClassProgram) {
            if (annotatedProgram.hasAnnotation(TypeKey.DALVIK_AnnotationDefault)) {
                return new DalvikAnnotationDefault((ClassProgram) annotatedProgram);
            }
        }
        return null;
    }
    public static DalvikAnnotationDefault getOrCreate(AnnotatedProgram annotatedProgram) {
        if (!annotatedProgram.hasAnnotation(TypeKey.DALVIK_AnnotationDefault)) {
            if (!(annotatedProgram instanceof ClassProgram)) {
                return null;
            }
            ClassProgram classProgram = (ClassProgram) annotatedProgram;
            annotatedProgram.addAnnotation(AnnotationItemKey.create(
                    AnnotationVisibility.SYSTEM,
                    TypeKey.DALVIK_AnnotationDefault,
                    AnnotationElementKey.create(
                            Key.DALVIK_value, AnnotationItemKey.create(null, classProgram.getKey()))
            ));
        }
        return of(annotatedProgram);
    }

}
