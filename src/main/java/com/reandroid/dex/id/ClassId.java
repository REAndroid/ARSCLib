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
package com.reandroid.dex.id;

import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.reference.IndirectStringReference;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.*;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class ClassId extends IdItem implements Comparable<ClassId>, KeyItemCreate {

    private final IdItemIndirectReference<TypeId> classType;
    private final IndirectInteger accessFlagValue;
    private final IdItemIndirectReference<TypeId> superClass;
    private final DataItemIndirectReference<TypeList> interfaces;
    private final IndirectStringReference sourceFile;
    private final DataItemIndirectReference<AnnotationsDirectory> annotationsDirectory;
    private final DataItemIndirectReference<ClassData> classData;
    private final DataItemIndirectReference<EncodedArray> staticValues;

    public ClassId() {
        super(SIZE);
        int offset = -4;
        
        this.classType = new IdItemIndirectReference<>(SectionType.TYPE_ID, this, offset += 4, USAGE_DEFINITION);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClass = new IdItemIndirectReference<>(SectionType.TYPE_ID, this, offset += 4, USAGE_SUPER_CLASS);
        this.interfaces = new DataItemIndirectReference<>(SectionType.TYPE_LIST, this, offset += 4);
        this.sourceFile = new IndirectStringReference(this, offset += 4, USAGE_SOURCE);
        this.annotationsDirectory = new DataItemIndirectReference<>(SectionType.ANNOTATION_DIRECTORY, this, offset += 4, USAGE_DEFINITION);
        this.classData = new DataItemIndirectReference<>(SectionType.CLASS_DATA, this, offset += 4, USAGE_DEFINITION);
        this.staticValues = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY, this, offset += 4, USAGE_DEFINITION);
        addUsageType(UsageMarker.USAGE_DEFINITION);
    }

    @Override
    public void clearUsageType() {
    }

    public void ensureAllUnique(){
        annotationsDirectory.getUniqueItem(this);
        classData.getUniqueItem(this);
        staticValues.getUniqueItem(this);
    }
    @Override
    public SectionType<ClassId> getSectionType(){
        return SectionType.CLASS_ID;
    }
    @Override
    public TypeKey getKey(){
        return checkKey(TypeKey.create(getName()));
    }
    @Override
    public void setKey(Key key){
        TypeKey old = getKey();
        if(Objects.equals(old, key)){
            return;
        }
        this.classType.setItem(key);
        keyChanged(old);
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
        setKey(new TypeKey(typeName));
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
    public TypeId getSuperClassId(){
        return superClass.getItem();
    }
    public String getSuperClassName(){
        TypeId typeId = getSuperClassId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public void setSuperClass(TypeId typeId){
        this.superClass.setItem(typeId);
    }
    public void setSuperClass(String superClass){
        this.superClass.setItem(new TypeKey(superClass));
    }
    public StringData getSourceFile(){
        return sourceFile.getStringData();
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
    public DataItemIndirectReference<TypeList> getInterfacesReference() {
        return this.interfaces;
    }
    public void setInterfaces(TypeList interfaces){
        this.interfaces.setItem(interfaces);
    }

    public AnnotationItem getOrCreateInnerClass(){
        String inner = getName();
        if(inner != null){
            int i = inner.lastIndexOf('$');
            if(i > 0){
                inner = inner.substring(i + 1, inner.length() - 1);
                // TODO: check if it is anonymous instead
                if(inner.length() < 3){
                    inner = null;
                }
            }else {
                inner = null;
            }
        }
        return getOrCreateInnerClass(0x19, inner);
    }
    public AnnotationItem getOrCreateInnerClass(int flags, String name){
        AnnotationSet annotationSet = getOrCreateClassAnnotations();
        AnnotationItem item = annotationSet.getOrCreateByType(key_InnerClass);
        item.setVisibility(AnnotationVisibility.SYSTEM);

        AnnotationElement accessFlags = item.getOrCreateElement("accessFlags");
        IntValue accessFlagsValue = accessFlags.getOrCreateValue(DexValueType.INT);
        accessFlagsValue.set(flags);

        AnnotationElement nameElement = item.getOrCreateElement("name");

        if(name != null){
            StringValue stringValue = nameElement.getOrCreateValue(DexValueType.STRING);
            stringValue.set(new StringKey(name));
        }else {
            nameElement.getOrCreateValue(DexValueType.NULL);
        }
        return item;
    }
    public TypeValue getOrCreateEnclosingClass(){
        String enclosing = getName();
        if(enclosing != null){
            int i = enclosing.lastIndexOf('$');
            if(i > 0){
                enclosing = enclosing.substring(0, i) + ";";
            }else {
                enclosing = null;
            }
        }
        return getOrCreateEnclosingClass(TypeKey.create(enclosing));
    }
    public TypeValue getOrCreateEnclosingClass(TypeKey enclosing){
        if(enclosing == null){
            return null;
        }
        AnnotationSet annotationSet = getOrCreateClassAnnotations();
        AnnotationItem item = annotationSet.getOrCreateByType(key_EnclosingClass);
        item.setVisibility(AnnotationVisibility.SYSTEM);
        AnnotationElement element = item.getOrCreateElement("value");
        TypeValue typeValue = element.getOrCreateValue(DexValueType.TYPE);
        typeValue.setKey(enclosing);
        return typeValue;
    }
    public TypeValue getEnclosingClass(){
        AnnotationItem item = getEnclosingClassAnnotation();
        if(item == null){
            return null;
        }
        AnnotationElement element = item.getElement("value");
        DexValueBlock<?> value = element.getValue();
        if(value instanceof TypeValue){
            return (TypeValue) value;
        }
        return null;
    }
    public AnnotationItem getEnclosingClassAnnotation(){
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet != null){
            return annotationSet.getItemByType(key_EnclosingClass);
        }
        return null;
    }
    public AnnotationSet getOrCreateClassAnnotations(){
        return getOrCreateAnnotationsDirectory().getOrCreateClassAnnotations();
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
    public AnnotationsDirectory getOrCreateAnnotationsDirectory(){
        AnnotationsDirectory directory = annotationsDirectory.getOrCreate();
        directory.addClassUsage(this);
        return directory;
    }
    public AnnotationsDirectory getUniqueAnnotationsDirectory(){
        return annotationsDirectory.getUniqueItem(this);
    }
    public AnnotationsDirectory getOrCreateUniqueAnnotationsDirectory(){
        return annotationsDirectory.getOrCreateUniqueItem(this);
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
        classData = section.createItem();
        setClassData(classData);
        return classData;
    }
    public ClassData getClassData(){
        ClassData data = classData.getItem();
        linkClassData(data);
        return data;
    }
    public void setClassData(ClassData classData){
        this.classData.setItem(classData);
        linkClassData(classData);
    }
    public EncodedArray getStaticValues(){
        return staticValues.getItem();
    }
    public EncodedArray getOrCreateStaticValues(){
        return staticValues.getOrCreate();
    }
    public EncodedArray getUniqueStaticValues(){
        return staticValues.getUniqueItem(this);
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

        this.classType.updateItem();
        this.superClass.updateItem();
        this.interfaces.updateItem();
        this.sourceFile.updateItem();
        this.annotationsDirectory.updateItem();
        this.classData.updateItem();
        this.staticValues.updateItem();

        this.annotationsDirectory.addClassUsage(this);
        this.classData.addClassUsage(this);
        this.staticValues.addClassUsage(this);

        linkClassData(this.classData.getItem());
    }
    private void linkClassData(ClassData classData){
        if(classData != null){
            classData.setClassId(this);
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        this.classType.unlink();
        this.superClass.unlink();
        this.sourceFile.unlink();
        this.classData.unlink();
        this.annotationsDirectory.unlink();
        this.staticValues.unlink();
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return listUsedIds().iterator();
    }
    public ArrayCollection<IdItem> listUsedIds(){

        ArrayCollection<IdItem> collection = new ArrayCollection<>(200);
        collection.add(classType.getItem());
        collection.add(superClass.getItem());
        collection.add(sourceFile.getItem());

        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            collection.addAll(directory.usedIds());
        }
        ClassData classData = getClassData();
        if(classData != null){
            collection.addAll(classData.usedIds());
        }
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray != null){
            collection.addAll(encodedArray.usedIds());
        }
        int size = collection.size();
        for (int i = 0; i < size; i++){
            IdItem idItem = collection.get(i);
            collection.addAll(idItem.usedIds());
        }
        return collection;
    }

    public void merge(ClassId classId){
        if(classId == this){
            return;
        }
        accessFlagValue.set(classId.accessFlagValue.get());
        superClass.setItem(classId.superClass.getKey());
        sourceFile.setItem(classId.sourceFile.getKey());
        interfaces.setItem(classId.interfaces.getKey());
        annotationsDirectory.setItem(classId.annotationsDirectory.getKey());
        ClassData comingData = classId.getClassData();
        if(comingData != null){
            ClassData classData = getOrCreateClassData();
            classData.merge(comingData);
        }
        EncodedArray comingArray = classId.getStaticValues();
        if(comingArray != null){
            EncodedArray encodedArray = staticValues.getOrCreate();
            encodedArray.merge(comingArray);
        }
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
        getSuperClassId().append(writer);
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
        builder.append("\n.super ").append(getSuperClassId());
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

    public static final TypeKey key_EnclosingClass = TypeKey.create("Ldalvik/annotation/EnclosingClass;");

    public static final TypeKey key_InnerClass = TypeKey.create("Ldalvik/annotation/InnerClass;");
}
