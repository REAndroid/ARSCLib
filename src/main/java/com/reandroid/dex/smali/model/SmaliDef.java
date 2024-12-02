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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.*;
import com.reandroid.dex.program.AccessibleProgram;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.utils.collection.ArrayIterator;

import java.util.Iterator;

public abstract class SmaliDef extends Smali implements AccessibleProgram, SmaliRegion {

    private StringKey name;
    private int accessFlagsValue;

    private SmaliAnnotationSet annotation;

    private HiddenApiFlag[] hiddenApiFlags;

    private TypeKey defining;

    public SmaliDef(){
        super();
    }

    @Override
    public abstract ProgramKey getKey();

    @Override
    public int getAccessFlagsValue() {
        return accessFlagsValue;
    }
    @Override
    public void setAccessFlagsValue(int accessFlagsValue) {
        this.accessFlagsValue = accessFlagsValue;
    }
    @Override
    public AnnotationSetKey getAnnotation() {
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        if (annotationSet != null) {
            return annotationSet.getKey();
        }
        return AnnotationSetKey.EMPTY;
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotation) {
        if (annotation == null || annotation.isEmpty()) {
            setSmaliAnnotationSet(null);
        } else {
            getOrCreateSmaliAnnotationSet().setKey(annotation);
        }
    }
    @Override
    public boolean hasAnnotations() {
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        return annotationSet != null && !annotationSet.isEmpty();
    }
    @Override
    public void clearAnnotations() {
        setSmaliAnnotationSet(null);
    }

    public void setAccessFlags(AccessFlag[] accessFlags) {
        setAccessFlagsValue(Modifier.combineValues(accessFlags));
    }
    public void setAccessFlags(Iterator<AccessFlag> iterator) {
        setAccessFlagsValue(Modifier.combineValues(iterator));
    }
    public Iterator<HiddenApiFlag> getHiddenApiFlags() {
        return ArrayIterator.of(hiddenApiFlags());
    }
    public HiddenApiFlag[] hiddenApiFlags() {
        return this.hiddenApiFlags;
    }
    public void setHiddenApiFlags(HiddenApiFlag[] hiddenApiFlags) {
        this.hiddenApiFlags = hiddenApiFlags;
    }

    public StringKey getNameKey() {
        return name;
    }
    public String getName() {
        StringKey name = getNameKey();
        if (name != null) {
            return name.getString();
        }
        return null;
    }
    public void setName(StringKey name) {
        this.name = name;
    }
    public void setName(String name) {
        setName(StringKey.create(name));
    }
    public TypeKey getDefining() {
        TypeKey typeKey = null;
        SmaliDefSet<?> defSet = getDefSet();
        if(defSet != null) {
            typeKey = defSet.getDefining();
        }
        if(typeKey == null) {
            typeKey = this.defining;
        }
        return typeKey;
    }
    public void setDefining(TypeKey defining) {
        this.defining = defining;
    }

    public AnnotationSetKey getAnnotationSetKey() {
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        if (annotationSet != null) {
            return annotationSet.getKey();
        }
        return null;
    }
    public void setAnnotation(Iterator<AnnotationItemKey> iterator) {
        if (iterator.hasNext()) {
            getOrCreateSmaliAnnotationSet().addAllKeys(iterator);
        } else {
            setSmaliAnnotationSet(null);
        }
    }
    public void addAnnotation(AnnotationItemKey annotation) {
        SmaliAnnotationSet annotationSet = getOrCreateSmaliAnnotationSet();
        annotationSet.addKey(annotation);
    }
    public SmaliAnnotationSet getAnnotationSet() {
        return annotation;
    }
    public SmaliAnnotationSet getOrCreateSmaliAnnotationSet() {
        SmaliAnnotationSet directory = getAnnotationSet();
        if(directory == null){
            directory = new SmaliAnnotationSet();
            setSmaliAnnotationSet(directory);
        }
        return directory;
    }
    public void setSmaliAnnotationSet(SmaliAnnotationSet annotation) {
        SmaliAnnotationSet old = this.annotation;
        this.annotation = annotation;
        if (annotation != null) {
            annotation.setParent(this);
        }
        if (old != null && old != annotation) {
            old.setParent(null);
        }
    }
    public boolean hasAnnotation(){
        SmaliAnnotationSet annotation = getAnnotationSet();
        return annotation != null && !annotation.isEmpty();
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return null;
    }

    private SmaliDefSet<?> getDefSet(){
        return getParentInstance(SmaliDefSet.class);
    }
    public SmaliClass getSmaliClass(){
        if(getClass().isInstance(SmaliClass.class)) {
            return (SmaliClass) this;
        }
        return getParentInstance(SmaliClass.class);
    }
}
