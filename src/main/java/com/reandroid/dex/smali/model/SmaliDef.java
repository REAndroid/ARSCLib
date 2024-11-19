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
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.utils.collection.ArrayIterator;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public abstract class SmaliDef extends Smali implements SmaliRegion {

    private String name;
    private AccessFlag[] accessFlags;
    private SmaliAnnotationSet annotation;

    private HiddenApiFlag[] hiddenApiFlags;

    private TypeKey defining;

    public SmaliDef(){
        super();
    }

    public abstract Key getKey();
    public AccessFlag[] getAccessFlags() {
        return accessFlags;
    }
    public void setAccessFlags(AccessFlag[] accessFlags) {
        this.accessFlags = accessFlags;
    }
    public void setAccessFlags(Iterator<AccessFlag> iterator) {
        AccessFlag[] accessFlags;
        if (!iterator.hasNext()) {
            accessFlags = null;
        } else {
            List<AccessFlag> list = CollectionUtil.toList(iterator);
            accessFlags = list.toArray(new AccessFlag[list.size()]);
        }
        setAccessFlags(accessFlags);
    }
    public int getAccessFlagsValue(){
        return Modifier.combineValues(getAccessFlags());
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public void setAnnotation(AnnotationSetKey annotation) {
        if (annotation != null) {
            setAnnotation(annotation.iterator());
        } else {
            setAnnotation((SmaliAnnotationSet) null);
        }
    }
    public void setAnnotation(Iterator<AnnotationItemKey> iterator) {
        if (iterator.hasNext()) {
            getOrCreateAnnotation().addAllKeys(iterator);
        } else {
            setAnnotation((SmaliAnnotationSet) null);
        }
    }
    public void addAnnotation(AnnotationSetKey annotation) {
        SmaliAnnotationSet annotationSet = getOrCreateAnnotation();
        annotationSet.addAllKeys(annotation);
    }

    public void addAnnotations(Iterator<AnnotationItemKey> iterator) {
        if (iterator.hasNext()) {
            getOrCreateAnnotation().addAllKeys(iterator);
        }
    }
    public void addAnnotation(AnnotationItemKey annotation) {
        SmaliAnnotationSet annotationSet = getOrCreateAnnotation();
        annotationSet.addKey(annotation);
    }
    public SmaliAnnotationSet getAnnotationSet() {
        return annotation;
    }
    public SmaliAnnotationSet getOrCreateAnnotation() {
        SmaliAnnotationSet directory = getAnnotationSet();
        if(directory == null){
            directory = new SmaliAnnotationSet();
            setAnnotation(directory);
        }
        return directory;
    }
    public void setAnnotation(SmaliAnnotationSet annotation) {
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

    public boolean isStatic(){
        return Modifier.contains(getAccessFlags(), AccessFlag.STATIC);
    }
    public boolean isFinal(){
        return Modifier.contains(getAccessFlags(), AccessFlag.FINAL);
    }
    public boolean isPrivate(){
        return Modifier.contains(getAccessFlags(), AccessFlag.PRIVATE);
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

    public SmaliAnnotationItem parseAnnotation(SmaliReader reader) throws IOException {
        return getOrCreateAnnotation().parseNext(reader);
    }
}
