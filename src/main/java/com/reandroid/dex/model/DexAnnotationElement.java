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

import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class DexAnnotationElement extends Dex {

    private final DexAnnotation dexAnnotation;
    private String name;
    private AnnotationElementKey mRemoved;

    public DexAnnotationElement(DexAnnotation dexAnnotation, String name) {
        super();
        this.dexAnnotation = dexAnnotation;
        this.name = name;
    }

    public String getName(){
        return name;
    }
    public void setName(String name) {
        setKey(getKey().changeName(name));
    }
    public AnnotationElementKey getKey() {
        AnnotationElementKey key = this.mRemoved;
        if (key == null) {
            key = getDexAnnotation()
                    .getKey()
                    .get(getName());
        }
        return key;
    }
    public void setKey(Key key) {
        AnnotationElementKey elementKey = (AnnotationElementKey) key;
        DexAnnotation annotation = getDexAnnotation();
        AnnotationItemKey itemKey = annotation.getKey()
                .remove(getName())
                .add(elementKey);
        annotation.setKey(itemKey);
        this.name = elementKey.getName();
    }

    public Key getValue() {
        return getKey().getValue();
    }
    public void setValue(Key value) {
        setKey(getKey().changeValue(value));
    }

    @Override
    public void removeSelf() {
        if (!isRemoved()) {
            applyRemoveSelf();
        }
    }
    @Override
    public boolean isRemoved() {
        checkRemoved();
        return this.name == null;
    }
    private void checkRemoved() {
        DexAnnotation annotation = getDexAnnotation();
        if (annotation.isRemoved()) {
            applyRemoveSelf();
            return;
        }
        AnnotationElementKey key = this.mRemoved;
        if (key != null) {
            if (!annotation.isRemoved()) {
                String name = key.getName();
                if (annotation.contains(key.getName())) {
                    this.name = name;
                    this.mRemoved = null;
                }
            }
        }
    }
    private void applyRemoveSelf() {
        AnnotationElementKey key = this.mRemoved;
        if (key != null) {
            this.name = null;
            return;
        }
        key = getKey();
        this.name = null;
        if (key != null) {
            this.mRemoved = key;
            getDexAnnotation().remove(key.getName());
        }
    }

    public TypeKey getType() {
        return getDexAnnotation().getType();
    }
    public DexAnnotation getDexAnnotation() {
        return dexAnnotation;
    }

    @Override
    public boolean uses(Key key) {
        return getKey().uses(key);
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexAnnotation().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if (!isRemoved()) {
            getKey().append(writer);
        }
    }

    @Override
    public String toString() {
        AnnotationElementKey key = getKey();
        if (isRemoved()) {
            return "#REMOVED " + key;
        }
        return String.valueOf(key);
    }

    public static DexAnnotationElement create(DexAnnotation dexAnnotation, String name) {
        if(dexAnnotation != null && name != null) {
            return new DexAnnotationElement(dexAnnotation, name);
        }
        return null;
    }
    public static DexAnnotationElement create(DexAnnotation dexAnnotation, AnnotationElementKey elementKey) {
        if (dexAnnotation != null && elementKey != null) {
            return create(dexAnnotation, elementKey.getName());
        }
        return null;
    }
}
