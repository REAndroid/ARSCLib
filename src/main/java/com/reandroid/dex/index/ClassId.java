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

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.item.*;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class ClassId extends IndexItemEntry implements Comparable<ClassId>{

    private final ItemIndexReference<TypeId> classType;
    private final IndirectInteger accessFlagValue;
    private final ItemIndexReference<TypeId> superClass;
    private final ItemOffsetReference<TypeList> interfaces;
    private final StringReference sourceFile;
    private final ItemOffsetReference<AnnotationsDirectory> annotationsDirectory;
    private final ItemOffsetReference<ClassData> classData;
    private final ItemOffsetReference<EncodedArray> staticValues;

    public ClassId() {
        super(SIZE);
        int offset = -4;
        
        this.classType = new ItemIndexReference<>(SectionType.TYPE_ID, this, offset += 4);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClass = new ItemIndexReference<>(SectionType.TYPE_ID, this, offset += 4);
        this.interfaces = new ItemOffsetReference<>(SectionType.TYPE_LIST, this, offset += 4);
        this.sourceFile = new StringReference(this, offset += 4, StringData.USAGE_SOURCE);
        this.annotationsDirectory = new ItemOffsetReference<>(SectionType.ANNOTATIONS_DIRECTORY, this, offset += 4);
        this.classData = new ItemOffsetReference<>(SectionType.CLASS_DATA, this, offset += 4);
        this.staticValues = new ItemOffsetReference<>(SectionType.ENCODED_ARRAY, this, offset += 4);
    }

    @Override
    public TypeKey getKey(){
        String name = getName();
        if(name != null){
            return new TypeKey(name);
        }
        return null;
    }
    public String getName(){
        TypeId typeId = getClassType();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }

    public TypeId getClassType(){
        return classType.getItem();
    }
    public void setClassType(String typeName){
        setClassType(new TypeKey(typeName));
    }
    public void setClassType(TypeKey typeKey){
        this.classType.setItem(typeKey);
    }
    public void setClassType(TypeId typeId){
        this.classType.setItem(typeId);
    }
    public int getAccessFlagsValue() {
        return accessFlagValue.get();
    }
    public void setAccessFlagsValue(int value) {
        accessFlagValue.set(value);
    }
    public void addAccessFlag(AccessFlag flag) {
        setAccessFlagsValue(getAccessFlagsValue() | flag.getValue());
    }
    public AccessFlag[] getAccessFlags(){
        return AccessFlag.getAccessFlagsForClass(getAccessFlagsValue());
    }
    public TypeId getSuperClass(){
        return superClass.getItem();
    }
    public void setSuperClass(TypeId typeId){
        this.superClass.setItem(typeId);
    }
    public void setSuperClass(String superClass){
        this.superClass.setItem(new TypeKey(superClass));
    }
    public StringData getSourceFile(){
        return sourceFile.getItem();
    }
    public void setSourceFile(StringData stringData){
        this.sourceFile.setItem(stringData);
    }
    public void setSourceFile(String sourceFile){
        this.sourceFile.setString(sourceFile);
    }
    public TypeId[] getInterfaceTypeIds(){
        TypeList interfaceList = getInterfaces();
        if(interfaceList != null){
            return interfaceList.getTypeIds();
        }
        return null;
    }
    public TypeList getInterfaces(){
        return interfaces.getItem();
    }
    public ItemOffsetReference<TypeList> getInterfacesReference() {
        return this.interfaces;
    }
    public void setInterfaces(TypeList interfaces){
        this.interfaces.setItem(interfaces);
    }
    public AnnotationSet getClassAnnotations(){
        AnnotationsDirectory annotationsDirectory = getAnnotationsDirectory();
        if(annotationsDirectory != null){
            return annotationsDirectory.getClassAnnotations();
        }
        return null;
    }
    public void setClassAnnotations(AnnotationSet annotationSet){
        AnnotationsDirectory annotationsDirectory = getAnnotationsDirectory();
        if(annotationsDirectory != null){
            annotationsDirectory.setClassAnnotations(annotationSet);
        }
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        return annotationsDirectory.getItem();
    }
    public void setAnnotationsDirectory(AnnotationsDirectory directory){
        this.annotationsDirectory.setItem(directory);
    }
    public ClassData getOrCreateClassData(){
        ClassData classData = getClassData();
        if(classData != null){
            return classData;
        }
        Section<ClassData> section = getSection(SectionType.CLASS_DATA);
        classData = section.createOffsetItem();
        setClassData(classData);
        return classData;
    }
    public ClassData getClassData(){
        return classData.getItem();
    }
    public void setClassData(ClassData classData){
        this.classData.setItem(classData);
    }
    public EncodedArray getStaticValues(){
        return staticValues.getItem();
    }
    public EncodedArray getOrCreateStaticValues(){
        return staticValues.getOrCreate();
    }
    public DexValueBlock<?> getStaticValue(int i){
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray != null){
            return encodedArray.get(i);
        }
        return null;
    }
    public<T1 extends DexValueBlock<?>> T1 getOrCreateStaticValue(DexValueType<T1> valueType, int i){
        return getOrCreateStaticValues().getOrCreate(valueType, i);
    }
    public void setStaticValues(EncodedArray staticValues){
        this.staticValues.setItem(staticValues);
    }

    @Override
    public void refresh() {
        this.classType.refresh();
        this.superClass.refresh();
        this.interfaces.refresh();
        this.sourceFile.refresh();
        this.annotationsDirectory.refresh();
        this.classData.refresh();
        this.staticValues.refresh();
    }
    @Override
    void cacheItems(){
        this.classType.getItem();
        this.superClass.getItem();
        this.interfaces.getItem();
        this.sourceFile.getItem();
        this.annotationsDirectory.getItem();
        this.classData.getItem();
        this.staticValues.getItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(".class ");
        AccessFlag[] accessFlags = getAccessFlags();
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        getClassType().append(writer);
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
        TypeList interfaces = getInterfaces();
        if(interfaces != null && interfaces.size() > 0){
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
            writer.appendComment("Null class data: " + this.classData.get());
        }
    }

    @Override
    public int compareTo(ClassId classId) {
        if(classId == null){
            return -1;
        }
        return CompareUtil.compare(getClassType(), classId.getClassType());
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
        builder.append(getClassType());
        builder.append("\n.super ").append(getSuperClass());
        StringData sourceFile = getSourceFile();
        if(sourceFile != null){
            builder.append("\n.source \"").append(sourceFile.getString()).append("\"");
        }
        builder.append("\n");
        TypeList interfaces = getInterfaces();
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
