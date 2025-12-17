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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DefIndex;
import com.reandroid.dex.common.IdDefinition;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.dalvik.DalvikEnclosing;
import com.reandroid.dex.dalvik.DalvikMemberClass;
import com.reandroid.dex.data.AnnotationsDirectory;
import com.reandroid.dex.data.ClassData;
import com.reandroid.dex.data.Def;
import com.reandroid.dex.data.EncodedArray;
import com.reandroid.dex.data.FieldDef;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.ArrayValueKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeListKey;
import com.reandroid.dex.program.ClassProgram;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.reference.TypeListReference;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class ClassId extends IdItem implements ClassProgram, 
        IdDefinition<TypeId>, DefIndex, Comparable<ClassId> {

    private final ClassTypeId classTypeId;
    private final IndirectInteger accessFlagValue;
    private final SuperClassId superClassId;
    private final TypeListReference interfaces;
    private final SourceFile sourceFile;
    private final DataItemIndirectReference<AnnotationsDirectory> annotationsDirectory;
    private final DataItemIndirectReference<ClassData> classData;
    private final DataItemIndirectReference<EncodedArray> staticValues;

    public ClassId() {
        super(SIZE);
        int offset = 0;
        
        this.classTypeId = new ClassTypeId(this, offset);
        this.accessFlagValue = new IndirectInteger(this, offset += 4);
        this.superClassId = new SuperClassId(this, offset += 4);
        this.interfaces = new TypeListReference(this, offset += 4, USAGE_INTERFACE);
        this.sourceFile = new SourceFile(this, offset += 4);
        this.annotationsDirectory = new DataItemIndirectReference<>(SectionType.ANNOTATION_DIRECTORY, this, offset += 4, UsageMarker.USAGE_DEFINITION);
        this.classData = new DataItemIndirectReference<>(SectionType.CLASS_DATA, this, offset += 4, UsageMarker.USAGE_DEFINITION);
        this.staticValues = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY, this, offset += 4, UsageMarker.USAGE_STATIC_VALUES);
        addUsageType(UsageMarker.USAGE_DEFINITION);
    }

    @Override
    public void clearUsageType() {
    }

    @Override
    public void edit() {
        this.editInternal(this);
    }
    @Override
    public void editInternal(Block user) {
        annotationsDirectory.editInternal(this);
        classData.editInternal(this);
        staticValues.editInternal(this);
    }

    @Override
    public SectionType<ClassId> getSectionType() {
        return SectionType.CLASS_ID;
    }
    @Override
    public TypeKey getKey() {
        TypeId typeId = getId();
        TypeKey typeKey = null;
        if (typeId != null) {
            typeKey = typeId.getKey();
        }
        return checkKey(typeKey);
    }
    @Override
    public void setKey(Key key) {
        TypeKey old = getKey();
        if (ObjectsUtil.equals(old, key)) {
            return;
        }
        this.classTypeId.setKey(key);
        keyChanged(old);
    }
    @Override
    public int getDefinitionIndex() {
        return getIndex();
    }

    @Override
    public AnnotationSetKey getAnnotation() {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.get(this);
        }
        return AnnotationSetKey.empty();
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotationSet) {
        writeAnnotation(annotationSet);
    }
    @Override
    public void clearAnnotations() {
        writeAnnotation(AnnotationSetKey.empty());
    }
    @Override
    public boolean hasAnnotations() {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.contains(this);
        }
        return false;
    }
    @Override
    public AnnotationItemKey getAnnotation(TypeKey typeKey) {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.getAnnotation(this, typeKey);
        }
        return null;
    }
    @Override
    public Key getAnnotationValue(TypeKey typeKey, String name) {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.getAnnotationValue(this, typeKey, name);
        }
        return null;
    }

    private void writeAnnotation(AnnotationSetKey key) {
        if (key == null || key.isEmpty()) {
            if (hasAnnotations()) {
                getOrCreateUniqueAnnotationsDirectory().remove(this);
            }
        } else {
            getOrCreateUniqueAnnotationsDirectory().put(this, key);
        }
    }
    @Deprecated
    public String getName() {
        TypeId typeId = getId();
        if (typeId != null) {
            return typeId.getName();
        }
        return null;
    }
    @Deprecated
    public void setName(String typeName) {
        setKey(new TypeKey(typeName));
    }

    public ClassTypeId getClassTypeId() {
        return classTypeId;
    }
    @Override
    public TypeId getId() {
        return getClassTypeId().getItem();
    }
    @Override
    public int getAccessFlagsValue() {
        return accessFlagValue.get();
    }
    @Override
    public void setAccessFlagsValue(int value) {
        accessFlagValue.set(value);
    }
    public void setId(TypeId typeId) {
        this.classTypeId.setItem(typeId);
    }
    public SuperClassId getSuperClassId() {
        return superClassId;
    }
    public TypeId getSuperClassType() {
        return getSuperClassId().getItem();
    }
    @Override
    public TypeKey getSuperClassKey() {
        return getSuperClassId().getKey();
    }
    public void setSuperClass(TypeKey typeKey) {
        this.superClassId.setKey(typeKey);
    }
    public SourceFile getSourceFileReference() {
        return sourceFile;
    }
    @Override
    public String getSourceFileName() {
        return getSourceFileReference().getString();
    }
    public void setSourceFile(String sourceFile) {
        getSourceFileReference().setString(sourceFile);
    }

    public Iterator<TypeKey> getInstanceKeys() {
        return CombiningIterator.singleOne(getSuperClassKey(), getInterfacesKey().iterator());
    }
    @Override
    public TypeListKey getInterfacesKey() {
        TypeListKey typeListKey = interfaces.getKey();
        if (typeListKey == null) {
            typeListKey = TypeListKey.empty();
        }
        return typeListKey;
    }
    public TypeList getInterfaceTypeList() {
        return interfaces.getItem();
    }
    public TypeListReference getInterfacesReference() {
        return this.interfaces;
    }
    public void setInterfaces(TypeListKey typeListKey) {
        this.interfaces.setKey(typeListKey);
    }

    @Override
    public Iterator<FieldDef> getStaticFields() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getStaticFields();
        }
        return EmptyIterator.of();
    }
    @Override
    public Iterator<FieldDef> getInstanceFields() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getInstanceFields();
        }
        return EmptyIterator.of();
    }

    @Override
    public int getInstanceFieldsCount() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getInstanceFieldsCount();
        }
        return 0;
    }

    @Override
    public int getStaticFieldsCount() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getStaticFieldsCount();
        }
        return 0;
    }
    @Override
    public boolean hasStaticFields() {
        return getStaticFieldsCount() != 0;
    }
    @Override
    public boolean hasInstanceFields() {
        return getInstanceFieldsCount() != 0;
    }

    @Override
    public Iterator<MethodDef> getDirectMethods() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getDirectMethods();
        }
        return EmptyIterator.of();
    }
    @Override
    public Iterator<MethodDef> getVirtualMethods() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getVirtualMethods();
        }
        return EmptyIterator.of();
    }
    @Override
    public Iterator<MethodDef> declaredMethods() {
        return CombiningIterator.two(getDirectMethods(), getVirtualMethods());
    }
    @Override
    public int getDirectMethodsCount() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getDirectMethodsCount();
        }
        return 0;
    }
    @Override
    public int getVirtualMethodsCount() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.getVirtualMethodsCount();
        }
        return 0;
    }
    @Override
    public boolean hasDirectMethods() {
        return getDirectMethodsCount() != 0;
    }
    @Override
    public boolean hasVirtualMethods() {
        return getVirtualMethodsCount() != 0;
    }

    public TypeKey getDalvikEnclosing() {
        DalvikEnclosing<?> enclosing = DalvikEnclosing.of(this);
        if (enclosing != null) {
            return enclosing.getEnclosingClass();
        }
        return null;
    }
    public Iterator<TypeKey> getDalvikMemberClasses() {
        DalvikMemberClass dalvikMemberClass = DalvikMemberClass.of(this);
        if (dalvikMemberClass != null) {
            return dalvikMemberClass.getMembers();
        }
        return EmptyIterator.of();
    }
    public AnnotationsDirectory getUniqueAnnotationsDirectory() {
        return annotationsDirectory.getUniqueItem(this);
    }
    public AnnotationsDirectory getOrCreateUniqueAnnotationsDirectory() {
        return annotationsDirectory.getOrCreateUniqueItem(this);
    }
    public AnnotationsDirectory getAnnotationsDirectory() {
        return annotationsDirectory.getItem();
    }
    public void setAnnotationsDirectory(AnnotationsDirectory directory) {
        this.annotationsDirectory.setItem(directory);
    }
    public ClassData getOrCreateClassData() {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData;
        }
        Section<ClassData> section = getOrCreateSection(SectionType.CLASS_DATA);
        classData = section.createItem();
        setClassData(classData);
        return classData;
    }
    public Def<?> getDef(Key key) {
        ClassData classData = getClassData();
        if (classData != null) {
            return classData.get(key);
        }
        return null;
    }
    public ClassData getClassData() {
        ClassData data = classData.getItem();
        linkClassData(data);
        return data;
    }
    public void setClassData(ClassData classData) {
        this.classData.setItem(classData);
        linkClassData(classData);
    }
    public EncodedArray getStaticValuesEncodedArray() {
        EncodedArray encodedArray = staticValues.getItem();
        if (encodedArray != null) {
            encodedArray.addUniqueUser(this);
        }
        return encodedArray;
    }
    public ArrayValueKey getStaticValues() {
        return (ArrayValueKey) staticValues.getKey();
    }
    public void setStaticValues(ArrayValueKey staticValues) {
        this.staticValues.setKey(staticValues);
        this.staticValues.addUniqueUser(this);
    }
    public void setStaticValues(EncodedArray staticValues) {
        this.staticValues.setItem(staticValues);
    }

    @Override
    public void refresh() {

        this.annotationsDirectory.addUniqueUser(this);
        this.classData.addUniqueUser(this);
        this.staticValues.addUniqueUser(this);

        this.classTypeId.refresh();
        this.superClassId.refresh();
        this.interfaces.refresh();
        this.sourceFile.refresh();
        this.annotationsDirectory.refresh();
        this.classData.refresh();
        this.staticValues.refresh();
    }
    @Override
    void cacheItems() {

        this.classTypeId.pullItem();
        this.superClassId.pullItem();
        this.interfaces.pullItem();
        this.sourceFile.pullItem();
        this.annotationsDirectory.pullItem();
        this.classData.pullItem();
        this.staticValues.pullItem();

        this.annotationsDirectory.addUniqueUser(this);
        this.classData.addUniqueUser(this);
        this.staticValues.addUniqueUser(this);

        linkClassData(this.classData.getItem());
    }
    private void linkClassData(ClassData classData) {
        if (classData != null) {
            classData.setClassId(this);
        }
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        this.classTypeId.unlink();
        this.superClassId.unlink();
        this.sourceFile.unlink();
        this.classData.unlink();
        this.annotationsDirectory.unlink();
        this.staticValues.unlink();
    }
    @Override
    public boolean isRemoved() {
        return super.isRemoved();
    }

    public void replaceKeys(Key search, Key replace) {
        classTypeId.replaceKeys(search, replace);
        superClassId.replaceKeys(search, replace);
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            directory = getUniqueAnnotationsDirectory();
            directory.replaceKeys(search, replace);
        }
        interfaces.replaceKeys(search, replace);
        ClassData classData = getClassData();
        if (classData != null) {
            classData.replaceKeys(search, replace);
        }
    }
    @Override
    public Iterator<IdItem> usedIds() {
        return listUsedIds().iterator();
    }
    public ArrayCollection<IdItem> listUsedIds() {

        ArrayCollection<IdItem> collection = new ArrayCollection<>(200);
        collection.add(classTypeId.getItem());
        collection.add(superClassId.getItem());
        collection.add(sourceFile.getItem());
        collection.addAll(interfaces.iterator());
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            collection.addAll(directory.usedIds());
        }
        ClassData classData = getClassData();
        if (classData != null) {
            collection.addAll(classData.usedIds());
        }
        EncodedArray encodedArray = getStaticValuesEncodedArray();
        if (encodedArray != null) {
            collection.addAll(encodedArray.usedIds());
        }
        int size = collection.size();
        for (int i = 0; i < size; i++) {
            IdItem idItem = collection.get(i);
            collection.addAll(idItem.usedIds());
        }
        return collection;
    }

    public void merge(ClassId classId) {
        if (classId == this) {
            return;
        }
        accessFlagValue.set(classId.accessFlagValue.get());
        superClassId.setKey(classId.superClassId.getKey());
        sourceFile.setKey(classId.sourceFile.getKey());
        interfaces.setKey(classId.interfaces.getKey());
        annotationsDirectory.setKey(classId.annotationsDirectory.getKey());
        EncodedArray comingArray = classId.getStaticValuesEncodedArray();
        if (comingArray != null) {
            EncodedArray encodedArray = staticValues.getOrCreate();
            encodedArray.merge(comingArray);
        }
        ClassData comingData = classId.getClassData();
        if (comingData != null) {
            ClassData classData = getOrCreateClassData();
            classData.merge(comingData);
        }
    }
    public void fromSmali(SmaliClass smaliClass) throws IOException {

        setKey(smaliClass.getKey());
        setAccessFlagsValue(smaliClass.getAccessFlagsValue());
        setSuperClass(smaliClass.getSuperClassKey());
        setSourceFile(smaliClass.getSourceFileName());
        setInterfaces(smaliClass.getInterfacesKey());

        if (smaliClass.hasClassData()) {
            getOrCreateClassData().fromSmali(smaliClass);
        }
        if (smaliClass.hasAnnotation()) {
            setAnnotation(smaliClass.getAnnotationSetKey());
        }
    }
    public SmaliClass toSmali() {
        SmaliClass smaliClass = new SmaliClass();
        smaliClass.setKey(getKey());
        smaliClass.setAccessFlags(InstanceIterator.of(getAccessFlags(), AccessFlag.class));
        smaliClass.setSuperClass(getSuperClassKey());
        smaliClass.setSourceFile(getSourceFileReference().getKey());
        smaliClass.setInterfaces(getInterfacesReference().getKey());
        smaliClass.setAnnotation(getAnnotation());
        ClassData classData = getClassData();
        if (classData != null) {
            classData.toSmali(smaliClass);
        }
        return smaliClass;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        ClassData classData = getClassData();
        getClassTypeId().append(writer);
        getSuperClassId().append(writer);
        getSourceFileReference().append(writer);
        getInterfacesKey().appendInterfaces(writer);
        getAnnotation().appendClass(writer);
        if (classData != null) {
            writer.newLine();
            classData.append(writer);
        }
    }
    @Override
    public int compareTo(ClassId classId) {
        if (classId == null) {
            return -1;
        }
        if (classId == this) {
            return 0;
        }
        return SectionTool.compareIdx(getId(), classId.getId());
    }
    @Override
    public String toString() {
        if (isReading()) {
            return ".class " + getKey();
        }
        return SmaliWriter.toStringSafe(this);
    }


    private static final int SIZE = 32;
}
