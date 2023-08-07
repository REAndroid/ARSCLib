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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.IndexAndOffsetArray;
import com.reandroid.dex.index.ClassIndex;
import com.reandroid.dex.index.FieldIndex;
import com.reandroid.dex.index.ItemIndex;
import com.reandroid.dex.sections.DexSection;
import com.reandroid.dex.sections.IndexSections;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationsDirectoryItem extends FixedBlockContainer implements SmaliFormat {


    private final IntegerItem classOffset;
    private final IntegerItem fieldCount;
    private final IntegerItem methodCount;
    private final IntegerItem parameterCount;

    private final IndexAndOffsetArray fieldsOffset;
    private final IndexAndOffsetArray methodsOffset;
    private final IndexAndOffsetArray parametersOffset;
    
    private final AnnotationGroup classAnnotations;
    private final AnnotationGroup parameterAnnotations;


    public AnnotationsDirectoryItem() {
        super(7);

        this.classOffset = new IntegerItem();
        this.fieldCount = new IntegerItem();
        this.methodCount = new IntegerItem();
        this.parameterCount = new IntegerItem();

        this.fieldsOffset = new IndexAndOffsetArray(fieldCount);
        this.methodsOffset = new IndexAndOffsetArray(methodCount);
        this.parametersOffset = new IndexAndOffsetArray(parameterCount);

        addChild(0, classOffset);
        addChild(1, fieldCount);
        addChild(2, methodCount);
        addChild(3, parameterCount);

        addChild(4, fieldsOffset);
        addChild(5, methodsOffset);
        addChild(6, parametersOffset);
        
        this.classAnnotations = new AnnotationGroup();
        this.parameterAnnotations = new AnnotationGroup();
        
        classAnnotations.setParent(this);
        parameterAnnotations.setParent(this);
    }

    public IntegerReference getClassOffset() {
        return classOffset;
    }
    public AnnotationGroup getClassAnnotations() {
        return classAnnotations;
    }
    public AnnotationGroup getParameterAnnotations() {
        return parameterAnnotations;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        super.onReadBytes(reader);

        loadClassAnnotation(reader);

        DexFile dexFile = getParentInstance(DexFile.class);
        IndexSections indexSections = dexFile.getSections();

        load(reader, indexSections.getFieldSection(), fieldsOffset);
        load(reader, indexSections.getMethodSection(), methodsOffset);

        reader.seek(position);
        //TODO: read field, method annotation, data ...
    }
    private void loadClassAnnotation(BlockReader reader) throws IOException {
        int offset = getClassOffset().get();
        if(offset > 0){
            ClassIndex classIndex = getParentInstance(ClassIndex.class);
            AnnotationGroup group = new AnnotationGroup();
            group.setParent(this);
            reader.seek(offset);
            group.read(reader);
            classIndex.addAnnotations(group);
        }
    }
    private void load(BlockReader reader, DexSection<? extends ItemIndex> section, IndexAndOffsetArray offsetArray) throws IOException {
        int count = offsetArray.size();
        for(int i = 0; i < count; i++){
            int offset = offsetArray.getOffset(i);
            if(offset == 0){
                continue;
            }
            int index = offsetArray.getItemIndex(i);
            ItemIndex itemIndex = section.get(index);
            AnnotationGroup group = new AnnotationGroup();
            group.setParent(this);
            reader.seek(offset);
            group.read(reader);
            itemIndex.addAnnotations(group);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        AnnotationGroup annotations = getClassAnnotations();
        if(annotations.size() > 0){
            writer.newLine();
            writer.append("# annotations");
            writer.newLine();
            annotations.append(writer);
        }
    }
    public String toString00() {
        return "classOffset=" + classOffset
                + ", fields={" + fieldsOffset
                + "}, methods={" + methodsOffset
                + "}, parameters={" + parametersOffset + "}";
    }

    @Override
    public String toString() {
        return  "classOffset=" + classOffset +
                ", fieldCount=" + fieldCount +
                ", methodCount=" + methodCount +
                ", parameterCount=" + parameterCount +
                ", fieldsOffset=" + fieldsOffset +
                ", methodsOffset=" + methodsOffset +
                ", parametersOffset=" + parametersOffset ;
    }
}
