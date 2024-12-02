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

import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ArraySupplierIterator;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Iterator;

public class DexAnnotation extends Dex implements Iterable<DexAnnotationElement>{

    private final Dex declaring;
    private final AnnotatedProgram annotatedProgram;
    private TypeKey typeKey;
    private AnnotationItemKey mRemoved;

    public DexAnnotation(Dex declaring, AnnotatedProgram annotatedProgram, TypeKey typeKey){
        super();
        this.declaring = declaring;
        this.annotatedProgram = annotatedProgram;
        this.typeKey = typeKey;
    }

    public AnnotationItemKey getKey() {
        AnnotationItemKey key = this.mRemoved;
        if (key == null) {
            key = getAnnotationSet()
                    .get(getType());
        }
        return key;
    }
    public void setKey(Key key) {
        AnnotationItemKey itemKey = (AnnotationItemKey) key;
        AnnotationSetKey annotationSetKey = getAnnotationSet()
                .remove(getType())
                .add(itemKey);
        setAnnotation(annotationSetKey);
        this.typeKey = itemKey.getType();
    }

    public TypeKey getType() {
        return typeKey;
    }
    public void setType(TypeKey typeKey) {
        setKey(getKey().changeType(typeKey));
    }

    @Override
    public boolean uses(Key key) {
        return getKey().uses(key);
    }

    public boolean contains(String name) {
        return getKey().containsElement(name);
    }
    public DexAnnotationElement get(String name) {
        return DexAnnotationElement.create(this, name);
    }
    public DexAnnotationElement get(int index) {
        return DexAnnotationElement.create(this, getKey().get(index));
    }
    public int size() {
        return getKey().size();
    }
    public void remove(String name) {
        setKey(getKey().remove(name));
    }
    @Override
    public Iterator<DexAnnotationElement> iterator() {
        return ArraySupplierIterator.of(new ArraySupplier<DexAnnotationElement>() {
            @Override
            public DexAnnotationElement get(int i) {
                return DexAnnotation.this.get(i);
            }
            @Override
            public int getCount() {
                return DexAnnotation.this.size();
            }
        });
    }
    public AnnotationVisibility getVisibility() {
        return getKey().getVisibility();
    }
    public void setVisibility(AnnotationVisibility visibility) {
        setKey(getKey().changeVisibility(visibility));
    }
    public DexAnnotationElement getOrCreate(String name) {
        if (!contains(name)) {
            setKey(getKey().getOrCreate(name));
        }
        return DexAnnotationElement.create(this, name);
    }
    @Override
    public void removeSelf() {
        if (!isRemoved()) {
            applyRemoveSelf();
        }
    }
    public boolean isRemoved() {
        checkRemoved();
        return this.typeKey == null;
    }
    private void checkRemoved() {
        AnnotationItemKey key = this.mRemoved;
        if (key != null) {
            AnnotationSetKey annotationSetKey = getAnnotationSet();
            TypeKey typeKey = key.getType();
            if (annotationSetKey != null) {
                if (annotationSetKey.contains(typeKey)) {
                    this.typeKey = typeKey;
                    this.mRemoved = null;
                } else {
                    applyRemoveSelf();
                }
            }
        }
    }
    private void applyRemoveSelf() {
        AnnotationItemKey key = this.mRemoved;
        if (key != null) {
            this.typeKey = null;
            return;
        }
        AnnotationSetKey annotationSetKey = getAnnotationSet();
        key = getKey();
        TypeKey typeKey = this.typeKey;
        this.typeKey = null;
        if (key != null) {
            this.mRemoved = key;
            typeKey = key.getType();
        }
        setAnnotation(annotationSetKey.remove(typeKey));
    }

    public Dex getDeclaring() {
        return declaring;
    }
    private AnnotatedProgram getAnnotatedItem() {
        return annotatedProgram;
    }
    private AnnotationSetKey getAnnotationSet() {
        return getAnnotatedItem().getAnnotation();
    }
    private void setAnnotation(AnnotationSetKey annotationSetKey) {
        getAnnotatedItem().setAnnotation(annotationSetKey);
    }


    public ElementType getAnnotationType() {
        Dex declaring = getDeclaring();
        if (declaring instanceof DexMethodParameter) {
            return ElementType.PARAMETER;
        }
        if (declaring instanceof DexMethod) {
            return ElementType.METHOD;
        }
        if (declaring instanceof DexField) {
            return ElementType.FIELD;
        }
        return ElementType.TYPE;
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDeclaring().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if (!isRemoved()) {
            getKey().append(writer);
        }
    }
    @Override
    public String toString() {
        AnnotationItemKey key = getKey();
        if (isRemoved()) {
            return "#REMOVED " + key;
        }
        return String.valueOf(key);
    }

    public static DexAnnotation create(Dex declaring, AnnotatedProgram annotatedProgram, TypeKey typeKey) {
        if(declaring != null && annotatedProgram != null && annotatedProgram.hasAnnotation(typeKey)) {
            return new DexAnnotation(declaring, annotatedProgram, typeKey);
        }
        return null;
    }

    public static DexAnnotation create(Dex declaring, AnnotatedProgram annotatedProgram, AnnotationItemKey itemKey) {
        if(declaring != null && annotatedProgram != null && itemKey != null) {
            return create(declaring, annotatedProgram, itemKey.getType());
        }
        return null;
    }
}
