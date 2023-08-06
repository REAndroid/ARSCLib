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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.dex.base.OffsetIndexArray;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationsDirectoryItem extends FixedBlockContainer implements SmaliFormat {


    private final IntegerItem classOffset;
    private final IntegerItem fieldCount;
    private final IntegerItem methodCount;
    private final IntegerItem parameterCount;
    private final IntegerItem annotationStart;

    private final OffsetIndexArray fieldsOffset;
    private final OffsetIndexArray methodsOffset;
    private final OffsetIndexArray parametersOffset;
    
    private final AnnotationItemList classAnnotations;
    private final AnnotationItemList fieldAnnotations;
    private final AnnotationItemList methodAnnotations;
    private final AnnotationItemList parameterAnnotations;

    public AnnotationsDirectoryItem() {
        super(8);
        int offset = -4;
        this.classOffset = new IntegerItem();
        this.fieldCount = new IntegerItem();
        this.methodCount = new IntegerItem();
        this.parameterCount = new IntegerItem();
        this.annotationStart = new IntegerItem();

        this.fieldsOffset = new OffsetIndexArray(fieldCount);
        this.methodsOffset = new OffsetIndexArray(methodCount);
        this.parametersOffset = new OffsetIndexArray(parameterCount);

        addChild(0, classOffset);
        addChild(1, fieldCount);
        addChild(2, methodCount);
        addChild(3, parameterCount);
        addChild(4, annotationStart);
        addChild(5, fieldsOffset);
        addChild(6, methodsOffset);
        addChild(7, parametersOffset);
        
        this.classAnnotations = new AnnotationItemList();
        this.fieldAnnotations = new AnnotationItemList();
        this.methodAnnotations = new AnnotationItemList();
        this.parameterAnnotations = new AnnotationItemList();
        
        classAnnotations.setParent(this);
        fieldAnnotations.setParent(this);
        methodAnnotations.setParent(this);
        parameterAnnotations.setParent(this);
    }

    public IntegerItem getClassOffset() {
        return classOffset;
    }
    public IntegerItem getFieldCount() {
        return fieldCount;
    }
    public IntegerItem getMethodCount() {
        return methodCount;
    }
    public IntegerItem getParameterCount() {
        return parameterCount;
    }
    public IntegerItem getAnnotationsStart() {
        return annotationStart;
    }
    
    public AnnotationItemList getClassAnnotations() {
        return classAnnotations;
    }
    public AnnotationItemList getFieldAnnotations() {
        return fieldAnnotations;
    }
    public AnnotationItemList getMethodAnnotations() {
        return methodAnnotations;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        super.onReadBytes(reader);
        int offset = getClassOffset().get();
        if(offset > 0){
            reader.seek(offset);
            getClassAnnotations().read(reader);
        }
        OffsetIndexArray offsetIndexArray = this.fieldsOffset;
        int count = offsetIndexArray.size();
        for(int i = 0; i < count; i++){
            offset = offsetIndexArray.getItemOffset(i);
            reader.seek(offset);
            fieldAnnotations.read(reader);
        }
        offsetIndexArray = this.methodsOffset;
        count = offsetIndexArray.size();
        for(int i = 0; i < count; i++){
            offset = offsetIndexArray.getItemOffset(i);
            reader.seek(offset);
            methodAnnotations.read(reader);
        }
        offsetIndexArray = this.parametersOffset;
        count = offsetIndexArray.size();
        for(int i = 0; i < count; i++){
            offset = offsetIndexArray.getItemOffset(i);
            reader.seek(offset);
            parameterAnnotations.read(reader);
        }

        reader.seek(position);
        //TODO: read field, method annotation, data ...
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        AnnotationItemList annotations = getClassAnnotations();
        if(annotations.size() > 0){
            writer.newLine();
            writer.append("# annotations");
            writer.newLine();
            annotations.append(writer);
        }
    }
    @Override
    public String toString() {
        return "class=" + classOffset
                + ", fields={" + fieldsOffset
                + "}, methods={" + methodsOffset + "}";
    }


}
