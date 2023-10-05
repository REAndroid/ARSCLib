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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationsDirectory extends DataSectionEntry {

    private final Header header;


    private final DirectoryMap<FieldDef, AnnotationSet> fieldsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationSet> methodsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationGroup> parametersAnnotationMap;

    public AnnotationsDirectory() {
        super(4);
        this.header = new Header();

        this.fieldsAnnotationMap = new DirectoryMap<>(header.fieldCount, CREATOR_FIELDS);
        this.methodsAnnotationMap = new DirectoryMap<>(header.methodCount, CREATOR_METHODS);
        this.parametersAnnotationMap = new DirectoryMap<>(header.parameterCount, CREATOR_PARAMS);

        addChild(0, header);

        addChild(1, fieldsAnnotationMap);
        addChild(2, methodsAnnotationMap);
        addChild(3, parametersAnnotationMap);
    }

    public AnnotationSet getOrCreateClassAnnotations(){
        return header.classAnnotation.getOrCreate();
    }
    public AnnotationSet getClassAnnotations(){
        return header.classAnnotation.getItem();
    }
    public void setClassAnnotations(AnnotationSet annotationSet){
        header.classAnnotation.setItem(annotationSet);
    }

    public boolean isEmpty(){
        return getClassAnnotations() == null ||
                fieldsAnnotationMap.isEmpty() ||
                methodsAnnotationMap.isEmpty() ||
                parametersAnnotationMap.isEmpty();
    }
    public int countField(){
        return  fieldsAnnotationMap.getCount() ;
    }
    public int countMethod(){
        return  methodsAnnotationMap.getCount() +
                parametersAnnotationMap.getCount();
    }


    public void sortFields() {
        fieldsAnnotationMap.sort();
    }
    public void sortMethods() {
        methodsAnnotationMap.sort();
        parametersAnnotationMap.sort();
    }
    public void link(Def<?> def) {
        if(def instanceof FieldDef){
            linkField((FieldDef) def);
        }else if(def instanceof MethodDef){
            linkMethod((MethodDef) def);
        }
    }
    public void linkField(FieldDef def) {
        fieldsAnnotationMap.link(def);
    }
    public void linkMethod(MethodDef def) {
        methodsAnnotationMap.link(def);
        parametersAnnotationMap.link(def);
    }
    public void remove(Def<?> def) {
        if(def instanceof FieldDef){
            removeField((FieldDef) def);
        }else if(def instanceof MethodDef){
            removeMethod((MethodDef) def);
        }
    }
    public void removeField(FieldDef def) {
        fieldsAnnotationMap.remove(def);
    }
    public void removeMethod(MethodDef def) {
        methodsAnnotationMap.remove(def);
        parametersAnnotationMap.remove(def);
    }
    public Iterator<AnnotationSet> getAnnotations(Def<?> def){
        if(def.getClass() == FieldDef.class){
            return getFieldsAnnotation((FieldDef) def);
        }
        if(def.getClass() == MethodDef.class){
            return getMethodAnnotation((MethodDef) def);
        }
        throw new IllegalArgumentException("Unknown class type: " + def.getClass());
    }
    public Iterator<AnnotationSet> getFieldsAnnotation(FieldDef fieldDef){
        return fieldsAnnotationMap.getValues(fieldDef);
    }
    public Iterator<AnnotationSet> getFieldsAnnotation(int index){
        return fieldsAnnotationMap.getValues(index);
    }
    public Iterator<AnnotationSet> getMethodAnnotation(int index){
        return methodsAnnotationMap.getValues(index);
    }
    public Iterator<AnnotationSet> getMethodAnnotation(MethodDef methodDef){
        return methodsAnnotationMap.getValues(methodDef);
    }
    public Iterator<AnnotationGroup> getParameterAnnotation(MethodDef methodDef){
        return parametersAnnotationMap.getValues(methodDef);
    }
    public Iterator<DirectoryEntry<MethodDef, AnnotationGroup>> getParameterEntries(MethodDef methodDef){
        return parametersAnnotationMap.getEntries(methodDef);
    }
    public Iterator<AnnotationGroup> getParameterAnnotation(int methodIndex){
        return parametersAnnotationMap.getValues(methodIndex);
    }
    public Iterator<AnnotationSet> getParameterAnnotation(MethodDef methodDef, int parameterIndex){
        return ComputeIterator.of(getParameterAnnotation(methodDef),
                annotationGroup -> annotationGroup.getItem(parameterIndex));
    }
    public Iterator<AnnotationSet> getParameterAnnotation(int methodIndex, int parameterIndex){
        return ComputeIterator.of(getParameterAnnotation(methodIndex),
                annotationGroup -> annotationGroup.getItem(parameterIndex));
    }
    public void editParameterAnnotations(MethodDef methodDef){
        Iterator<AnnotationGroup> iterator = getParameterAnnotation(methodDef);
        while (iterator.hasNext()){
            AnnotationGroup group = iterator.next();

        }
    }


    @Override
    protected void onPreRefresh() {
        header.refresh();
        super.onPreRefresh();
    }

    @Override
    public String toString() {
        return  header +
                ", fields=" + fieldsAnnotationMap +
                ", methods=" + methodsAnnotationMap +
                ", parameters=" + parametersAnnotationMap;
    }

    static class Header extends DexBlockItem implements BlockRefresh {

        final ItemOffsetReference<AnnotationSet> classAnnotation;
        final IndirectInteger fieldCount;
        final IndirectInteger methodCount;
        final IndirectInteger parameterCount;

        public Header() {
            super(16);

            this.classAnnotation = new ItemOffsetReference<>(SectionType.ANNOTATION_SET, this, 0);
            this.fieldCount = new IndirectInteger(this, 4);
            this.methodCount = new IndirectInteger(this, 8);
            this.parameterCount = new IndirectInteger(this, 12);
        }

        public boolean isEmpty(){
            return classAnnotation.get() == 0
                    && fieldCount.get() == 0
                    && methodCount.get() == 0
                    && parameterCount.get() == 0;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            super.onReadBytes(reader);
            cacheItems();
        }
        private void cacheItems(){
            this.classAnnotation.getItem();
        }

        @Override
        public void refresh() {
            this.classAnnotation.refresh();
        }

        @Override
        public String toString() {
            return  "class=" + classAnnotation +
                    ", fields=" + fieldCount +
                    ", methods=" + methodCount +
                    ", parameters=" + parameterCount;
        }

    }
    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<FieldDef, AnnotationSet>> CREATOR_FIELDS = new Creator<DirectoryEntry<FieldDef, AnnotationSet>>() {
        @Override
        public DirectoryEntry<FieldDef, AnnotationSet>[] newInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<FieldDef, AnnotationSet> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_SET);
        }
    };

    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<MethodDef, AnnotationSet>> CREATOR_METHODS = new Creator<DirectoryEntry<MethodDef, AnnotationSet>>() {
        @Override
        public DirectoryEntry<MethodDef, AnnotationSet>[] newInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<MethodDef, AnnotationSet> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_SET);
        }
    };

    @SuppressWarnings("unchecked")
    private static final Creator<DirectoryEntry<MethodDef, AnnotationGroup>> CREATOR_PARAMS = new Creator<DirectoryEntry<MethodDef, AnnotationGroup>>() {
        @Override
        public DirectoryEntry<MethodDef, AnnotationGroup>[] newInstance(int length) {
            return new DirectoryEntry[length];
        }
        @Override
        public DirectoryEntry<MethodDef, AnnotationGroup> newInstance() {
            return new DirectoryEntry<>(SectionType.ANNOTATION_GROUP);
        }
    };
}
