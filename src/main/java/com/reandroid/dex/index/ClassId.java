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
package com.reandroid.dex.index;

import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.item.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ClassId extends ItemId {

    private final ItemIndexReference<TypeId> typeIndex;
    private final IndirectInteger accessFlagValue;
    private final IndirectInteger superClassIndex;
    private final IndirectInteger interfacesOffset;
    private final IndirectInteger sourceFileIndex;
    private final IndirectInteger annotationDirectoryOffset;
    private final ItemOffsetReference<ClassData> classDataOffset;
    private final IndirectInteger staticValuesOffset;

    public ClassId() {
        super(SIZE);
        int offset = -4;
        
        this.typeIndex = new ItemIndexReference<>(SectionType.TYPE_ID, this, offset += 4);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClassIndex = new IndirectInteger(this, offset += 4);
        this.interfacesOffset = new IndirectInteger(this, offset += 4);
        this.sourceFileIndex = new IndirectInteger(this, offset += 4);
        this.annotationDirectoryOffset = new IndirectInteger(this, offset += 4);
        this.classDataOffset = new ItemOffsetReference<>(SectionType.CLASS_DATA, this, offset += 4);
        this.staticValuesOffset = new IndirectInteger(this, offset += 4);

    }

    public TypeId getType(){
        return typeIndex.getItem();
    }
    public int getAccessFlagValue() {
        return accessFlagValue.get();
    }
    public AccessFlag[] getAccessFlags(){
        return AccessFlag.getAccessFlagsForClass(getAccessFlagValue());
    }
    public TypeId getSuperClass(){
        return getTypeId(superClassIndex.get());
    }
    public StringData getSourceFile(){
        return getStringData(sourceFileIndex.get());
    }
    public TypeId[] getInterfaceTypeIds(){
        TypeList interfaceList = getInterfaceList();
        if(interfaceList != null){
            return interfaceList.toTypeIds();
        }
        return null;
    }
    public TypeList getInterfaceList(){
        return getAt(SectionType.TYPE_LIST, interfacesOffset.get());
    }
    public AnnotationSet getClassAnnotations(){
        AnnotationsDirectory annotationsDirectory = getAnnotationsDirectory();
        if(annotationsDirectory != null){
            return annotationsDirectory.getClassAnnotations();
        }
        return null;
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        return getAt(SectionType.ANNOTATIONS_DIRECTORY, annotationDirectoryOffset.get());
    }
    public ClassData getClassData(){
        return classDataOffset.getItem();
    }
    public EncodedArray getStaticValues(){
        return getAt(SectionType.ENCODED_ARRAY, staticValuesOffset.get());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".class ");
        AccessFlag[] accessFlags = getAccessFlags();
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        getType().append(writer);
        writer.newLine();
        writer.append(".super ");
        getSuperClass().append(writer);
        writer.newLine();
        StringData sourceFile = getSourceFile();
        if(sourceFile != null){
            writer.append(".source ");
            sourceFile.append(writer);
        }
        writer.newLine();
        TypeId[] interfaces = getInterfaceTypeIds();
        if(interfaces != null){
            writer.newLine();
            writer.append("# interfaces");
            for(TypeId typeId : interfaces){
                writer.newLine();
                writer.append(".implements ");
                typeId.append(writer);
            }
        }
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet != null){
            writer.newLine();
            writer.newLine();
            writer.append("# annotations");
            annotationSet.append(writer);
        }
        writer.newLine();
        ClassData classData = getClassData();
        if(classData != null){
            classData.setClassId(this);
            classData.append(writer);
        }else {
            writer.appendComment("Null class data: " + classDataOffset.get());
        }
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n.class ");
        AccessFlag[] accessFlags = getAccessFlags();
        for(AccessFlag af:accessFlags){
            builder.append(af);
            builder.append(" ");
        }
        builder.append(getType());
        builder.append("\n.super ").append(getSuperClass());
        StringData sourceFile = getSourceFile();
        if(sourceFile != null){
            builder.append("\n.source \"").append(sourceFile.getString()).append("\"");
        }
        builder.append("\n");
        TypeId[] interfaces = getInterfaceTypeIds();
        if(interfaces != null){
            builder.append("\n# interfaces");
            for(TypeId typeId : interfaces){
                builder.append("\n.implements ").append(typeId);
            }
        }
        return builder.toString();
    }


    private static final int SIZE = 32;
}
