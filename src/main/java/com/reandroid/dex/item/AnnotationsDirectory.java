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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationsDirectory extends DataItemEntry {

    private final Header header;

    private final SparseSectionArray<AnnotationSet> fieldsOffset;
    private final SparseSectionArray<AnnotationSet> methodsOffset;
    private final SparseSectionArray<AnnotationGroup> parametersOffset;

    public AnnotationsDirectory() {
        super(4);
        this.header = new Header();

        this.fieldsOffset = new SparseSectionArray<>(SectionType.ANNOTATION_SET, header.fieldCount);
        this.methodsOffset = new SparseSectionArray<>(SectionType.ANNOTATION_SET, header.methodCount);

        this.parametersOffset = new SparseSectionArray<>(SectionType.ANNOTATION_GROUP, header.parameterCount);

        addChild(0, header);

        addChild(1, fieldsOffset);
        addChild(2, methodsOffset);
        addChild(3, parametersOffset);
    }
    public AnnotationSet getClassAnnotations(){
        return header.classAnnotation.getItem();
    }
    public void setClassAnnotations(AnnotationSet annotationSet){
        header.classAnnotation.setItem(annotationSet);
    }
    public boolean isEmpty(){
        return header.isEmpty();
    }

    public Iterator<AnnotationSet> getFieldsAnnotation(int index){
        return fieldsOffset.getItems(index);
    }
    public void addFieldAnnotationSet(int index, AnnotationSet annotationSet){
        fieldsOffset.addItem(index, annotationSet);
    }
    public Iterator<AnnotationSet> getMethodAnnotation(int index){
        return methodsOffset.getItems(index);
    }
    public void addMethodAnnotationSet(int index, AnnotationSet annotationSet){
        methodsOffset.addItem(index, annotationSet);
    }
    public Iterator<AnnotationGroup> getParameterAnnotation(int methodIndex){
        return parametersOffset.getItems(methodIndex);
    }
    public Iterator<AnnotationSet> getParameterAnnotation(int methodIndex, int parameterIndex){
        return ComputeIterator.of(getParameterAnnotation(methodIndex),
                annotationGroup -> annotationGroup.getItem(parameterIndex));
    }
    public void addParameterAnnotation(int methodIndex, AnnotationGroup annotationGroup){
        parametersOffset.addItem(methodIndex, annotationGroup);
    }


    @Override
    protected void onPreRefresh() {
        header.refresh();
        super.onPreRefresh();
    }

    @Override
    public String toString() {
        return  header +
                ", fieldsOffset=" + fieldsOffset +
                ", methodsOffset=" + methodsOffset +
                ", parametersOffset=" + parametersOffset;
    }

    static class SparseSectionArray<T extends DataItemEntry> extends IndexAndOffsetArray implements BlockRefresh {
        private final SectionType<T> sectionType;
        private int[] indexArray;
        private T[] items;

        public SparseSectionArray(SectionType<T> sectionType, IntegerReference itemCount) {
            super(itemCount);
            this.sectionType = sectionType;
        }

        public Iterator<T> getItems(int index){
            int[] indexArray = this.indexArray;
            if(indexArray == null || indexArray.length == 0){
                return EmptyIterator.of();
            }
            T[] annotationSets = this.items;
            return new Iterator<T>() {
                private final int size = SparseSectionArray.this.size();
                private int position;
                private T mCurrent;
                @Override
                public boolean hasNext() {
                    return getCurrent() != null;
                }
                @Override
                public T next() {
                    T current = this.mCurrent;
                    this.mCurrent = null;
                    return current;
                }
                private T getCurrent(){
                    while (mCurrent == null && position < size){
                        if(indexArray[position] == index){
                            mCurrent = annotationSets[position];
                        }
                        position ++;
                    }
                    return mCurrent;
                }
            };
        }

        public void addItem(int index, T item){
            int offset = 0;
            if(item != null){
                offset = item.getOffsetReference().get();
            }
            int position = size();
            setSize(position + 1);
            setIndexEntry(position, index);
            setOffset(position, offset);
            cacheItems();
        }

        @Override
        public void refresh() {
            int[] indexArray = this.indexArray;
            if(indexArray == null || indexArray.length == 0){
                setSize(0);
                return;
            }
            int length = indexArray.length;
            setSize(length);
            T[] items = this.items;
            for(int i = 0; i < length; i++){
                int offset = 0;
                T item = items[i];
                if(item != null){
                    offset = item.getOffsetReference().get();
                }
                setIndexEntry(i, indexArray[i]);
                setOffset(i, offset);
            }
        }
        private void cacheItems(){
            indexArray = getIndexEntries();
            items = getAt(sectionType, getOffsets());
        }

        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            super.onReadBytes(reader);
            cacheItems();
        }
    }

    static class Header extends DexBlockItem implements BlockRefresh {

        final ItemOffsetReference<AnnotationSet> classAnnotation;
        final IndirectInteger fieldCount;
        final IndirectInteger methodCount;
        final IndirectInteger parameterCount;

        public Header() {
            super(16);

            int offset = -4;
            this.classAnnotation = new ItemOffsetReference<>(SectionType.ANNOTATION_SET, this, offset += 4);
            this.fieldCount = new IndirectInteger(this, offset += 4);
            this.methodCount = new IndirectInteger(this, offset += 4);
            this.parameterCount = new IndirectInteger(this, offset += 4);
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
            return  "classAnnotation=" + classAnnotation +
                    ", fieldCount=" + fieldCount +
                    ", methodCount=" + methodCount +
                    ", parameterCount=" + parameterCount;
        }

    }
}
