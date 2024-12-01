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

import com.reandroid.dex.common.IdDefinition;
import com.reandroid.dex.data.MethodParameter;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class DexMethodParameter extends Dex implements AnnotatedDex {

    private final DexMethod dexMethod;
    private final MethodParameter parameter;

    public DexMethodParameter(DexMethod dexMethod, MethodParameter parameter) {
        this.dexMethod = dexMethod;
        this.parameter = parameter;
    }

    public String getDebugName(){
        return getParameter().getDebugName();
    }
    public void removeDebugName(){
        getParameter().setDebugName(null);
    }
    public void setDebugName(String name){
        getParameter().setDebugName(name);
    }
    public void clearAnnotations(){
        getParameter().clearAnnotations();
    }
    public DexClass getTypeClass(){
        return getClassRepository().getDexClass(getType());
    }
    public TypeKey getType(){
        return getParameter().getType();
    }
    public int getIndex(){
        return getParameter().getDefinitionIndex();
    }
    public DexMethod getDexMethod() {
        return dexMethod;
    }
    public MethodParameter getParameter() {
        return parameter;
    }

    @Override
    public boolean uses(Key key) {
        if(ObjectsUtil.equals(getType(), key)) {
            return true;
        }
        Iterator<DexAnnotation> iterator = getAnnotations();
        while (iterator.hasNext()){
            DexAnnotation dexAnnotation = iterator.next();
            if(dexAnnotation.uses(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void removeSelf() {
        getDexMethod().removeParameter(getIndex());
    }

    @Override
    public Iterator<DexAnnotation> getAnnotations() {
        AnnotationSetKey annotation = getParameter().getAnnotation();
        return ComputeIterator.of(annotation.iterator(), this::initializeAnnotation);
    }
    @Override
    public DexAnnotation getAnnotation(TypeKey typeKey) {
        return initializeAnnotation(typeKey);
    }
    @Override
    public DexAnnotation getOrCreateAnnotation(TypeKey typeKey) {
        MethodParameter parameter = getParameter();
        AnnotationSetKey annotationSetKey = parameter.getAnnotation();
        if (!annotationSetKey.contains(typeKey)) {
            annotationSetKey = annotationSetKey.getOrCreate(typeKey);
            parameter.setAnnotation(annotationSetKey);
        }
        return initializeAnnotation(typeKey);
    }

    private DexAnnotation initializeAnnotation(TypeKey typeKey) {
        return DexAnnotation.create(this, getParameter(), typeKey);
    }
    DexAnnotation initializeAnnotation(AnnotationItemKey key) {
        if (key != null) {
            return DexAnnotation.create(this, getParameter(), key.getType());
        }
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getParameter().append(writer);
    }

    public static DexMethodParameter create(DexMethod dexMethod, MethodParameter parameter){
        if(dexMethod != null && parameter != null){
            return new DexMethodParameter(dexMethod, parameter);
        }
        return null;
    }
}
