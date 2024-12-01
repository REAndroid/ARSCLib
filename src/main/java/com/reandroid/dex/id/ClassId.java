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
import com.reandroid.dex.common.*;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.reference.TypeListReference;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class ClassId extends IdItem implements IdDefinition<TypeId>, Comparable<ClassId> {

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
        int offset = -4;
        
        this.classTypeId = new ClassTypeId(this, offset += 4);
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
    public void edit(){
        this.editInternal(this);
    }
    @Override
    public void editInternal(Block user) {
        annotationsDirectory.editInternal(this);
        classData.editInternal(this);
        staticValues.editInternal(this);
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
        this.classTypeId.setKey(key);
        keyChanged(old);
    }

    @Override
    public AnnotationSetKey getAnnotation() {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.getClassAnnotation();
        }
        return AnnotationSetKey.EMPTY;
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotationSet) {
        clearAnnotations();
        writeAnnotation(annotationSet);
    }
    @Override
    public void clearAnnotations() {
        writeAnnotation(AnnotationSetKey.EMPTY);
    }
    @Override
    public boolean hasAnnotations() {
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if (directory != null) {
            return directory.hasClassAnnotation();
        }
        return false;
    }
    private void writeAnnotation(AnnotationSetKey key) {
        if (key == null || key.isEmpty()) {
            if (hasAnnotations()) {
                getOrCreateUniqueAnnotationsDirectory().setClassAnnotations((AnnotationSetKey )null);
            }
        } else {
            getOrCreateUniqueAnnotationsDirectory().setClassAnnotations(key);
        }
    }
    public String getName(){
        TypeId typeId = getId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public void setName(String typeName){
        setKey(new TypeKey(typeName));
    }

    public ClassTypeId getClassTypeId(){
        return classTypeId;
    }
    @Override
    public TypeId getId(){
        return getClassTypeId().getItem();
    }
    @Override
    public int getAccessFlagsValue() {
        return accessFlagValue.get();
    }
    @Override
    public Iterator<? extends Modifier> getAccessFlags(){
        return AccessFlag.valuesOfClass(getAccessFlagsValue());
    }
    @Override
    public void setAccessFlagsValue(int value) {
        accessFlagValue.set(value);
    }
    public void setId(TypeId typeId){
        this.classTypeId.setItem(typeId);
    }
    public SuperClassId getSuperClassId(){
        return superClassId;
    }
    public TypeId getSuperClassType(){
        return getSuperClassId().getItem();
    }
    public TypeKey getSuperClassKey(){
        return getSuperClassId().getKey();
    }
    public void setSuperClass(TypeKey typeKey){
        this.superClassId.setKey(typeKey);
    }
    public void setSuperClass(String superClass){
        this.superClassId.setKey(new TypeKey(superClass));
    }
    public SourceFile getSourceFile(){
        return sourceFile;
    }
    public String getSourceFileName(){
        return getSourceFile().getString();
    }
    public void setSourceFile(String sourceFile){
        getSourceFile().setString(sourceFile);
    }

    public Iterator<TypeKey> getInstanceKeys(){
        return CombiningIterator.singleOne(getSuperClassKey(), getInterfaceKeys());
    }
    public Iterator<TypeKey> getInterfaceKeys(){
        return getInterfacesReference().getTypeKeys();
    }
    public TypeList getInterfaceTypeList(){
        return interfaces.getItem();
    }
    public TypeListReference getInterfacesReference() {
        return this.interfaces;
    }
    public void setInterfaces(TypeListKey typeListKey){
        this.interfaces.setKey(typeListKey);
    }

    public Key getDalvikEnclosing(){
        TypeKey typeKey = getDalvikEnclosingClass();
        if(typeKey != null) {
            return typeKey;
        }
        MethodKey enclosingMethod = getDalvikEnclosingMethod();
        if (enclosingMethod != null) {
            return enclosingMethod.getDeclaring();
        }
        return null;
    }
    public Iterator<TypeKey> getDalvikMemberClasses() {
        Key key = getAnnotationValue(TypeKey.DALVIK_MemberClass, Key.DALVIK_value);
        if (key instanceof KeyList<?>) {
            return ((KeyList<?>) key).iterator(TypeKey.class);
        }
        return EmptyIterator.of();
    }
    public MethodKey getDalvikEnclosingMethod() {
        Key key = getAnnotationValue(TypeKey.DALVIK_EnclosingMethod, Key.DALVIK_value);
        if (key instanceof MethodKey) {
            return (MethodKey) key;
        }
        return null;
    }
    public AnnotationItemKey getOrCreateDalvikInnerClass(){
        TypeKey typeKey = getKey();
        if (typeKey == null) {
            return null;
        }
        String inner = typeKey.getSimpleInnerName();
        if(AccessFlag.SYNTHETIC.isSet(getAccessFlagsValue())
                || inner.equals(typeKey.getSimpleName())
                || StringsUtil.isDigits(inner)){
            inner = null;
        }
        return getOrCreateDalvikInnerClass(getAccessFlagsValue(), inner);
    }
    public AnnotationItemKey getOrCreateDalvikInnerClass(int flags, String name) {
        AnnotationItemKey itemKey = AnnotationItemKey.create(AnnotationVisibility.SYSTEM,
                TypeKey.DALVIK_InnerClass)
                .add(Key.DALVIK_accessFlags, PrimitiveKey.of(flags))
                .add(Key.DALVIK_name, name == null ? NullValueKey.INSTANCE : StringKey.create(name));
        AnnotationSetKey annotation = getAnnotation().add(itemKey);
        setAnnotation(annotation);
        return itemKey;
    }
    public TypeKey getOrCreateDalvikEnclosingClass(){
        TypeKey key = getKey();
        if(key != null){
            TypeKey enclosing = key.getEnclosingClass();
            if(!key.equals(enclosing)){
                return getOrCreateDalvikEnclosingClass(enclosing);
            }
        }
        return null;
    }
    public TypeKey getOrCreateDalvikEnclosingClass(TypeKey enclosing) {
        if (enclosing == null) {
            return null;
        }
        AnnotationItemKey itemKey = AnnotationItemKey.create(AnnotationVisibility.SYSTEM,
                TypeKey.DALVIK_EnclosingClass)
                .add(Key.DALVIK_value, enclosing);
        AnnotationSetKey annotation = getAnnotation().add(itemKey);
        setAnnotation(annotation);
        return enclosing;
    }
    public TypeKey getDalvikEnclosingClass() {
        Key key = getAnnotationValue(TypeKey.DALVIK_EnclosingClass, Key.DALVIK_value);
        if (key instanceof TypeKey) {
            return (TypeKey) key;
        }
        return null;
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
        if (classData != null) {
            return classData;
        }
        Section<ClassData> section = getSection(SectionType.CLASS_DATA);
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
    public ClassData getClassData(){
        ClassData data = classData.getItem();
        linkClassData(data);
        return data;
    }
    public void setClassData(ClassData classData){
        this.classData.setItem(classData);
        linkClassData(classData);
    }
    public EncodedArray getStaticValuesEncodedArray(){
        EncodedArray encodedArray = staticValues.getItem();
        if (encodedArray != null) {
            encodedArray.addUniqueUser(this);
        }
        return encodedArray;
    }
    public ArrayValueKey getStaticValues() {
        return (ArrayValueKey) staticValues.getKey();
    }
    public void setStaticValues(ArrayValueKey staticValues){
        this.staticValues.setKey(staticValues);
        this.staticValues.addUniqueUser(this);
    }
    public void setStaticValues(EncodedArray staticValues){
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
    void cacheItems(){

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
    private void linkClassData(ClassData classData){
        if(classData != null) {
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

    public void replaceKeys(Key search, Key replace){
        classTypeId.replaceKeys(search, replace);
        superClassId.replaceKeys(search, replace);
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            directory = getUniqueAnnotationsDirectory();
            directory.replaceKeys(search, replace);
        }
        interfaces.replaceKeys(search, replace);
        ClassData classData = getClassData();
        if(classData != null){
            classData.replaceKeys(search, replace);
        }
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return listUsedIds().iterator();
    }
    public ArrayCollection<IdItem> listUsedIds(){

        ArrayCollection<IdItem> collection = new ArrayCollection<>(200);
        collection.add(classTypeId.getItem());
        collection.add(superClassId.getItem());
        collection.add(sourceFile.getItem());
        collection.addAll(interfaces.iterator());
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            collection.addAll(directory.usedIds());
        }
        ClassData classData = getClassData();
        if(classData != null){
            collection.addAll(classData.usedIds());
        }
        EncodedArray encodedArray = getStaticValuesEncodedArray();
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
        superClassId.setKey(classId.superClassId.getKey());
        sourceFile.setKey(classId.sourceFile.getKey());
        interfaces.setKey(classId.interfaces.getKey());
        annotationsDirectory.setKey(classId.annotationsDirectory.getKey());
        EncodedArray comingArray = classId.getStaticValuesEncodedArray();
        if(comingArray != null){
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
        setSuperClass(smaliClass.getSuperClass());
        setSourceFile(smaliClass.getSourceFileName());
        setInterfaces(smaliClass.getInterfacesKey());

        if(smaliClass.hasClassData()) {
            getOrCreateClassData().fromSmali(smaliClass);
        }
        if(smaliClass.hasAnnotation()) {
            setAnnotation(smaliClass.getAnnotationSetKey());
        }
    }
    public SmaliClass toSmali() {
        SmaliClass smaliClass = new SmaliClass();
        smaliClass.setKey(getKey());
        smaliClass.setAccessFlags(InstanceIterator.of(getAccessFlags(), AccessFlag.class));
        smaliClass.setSuperClass(getSuperClassKey());
        smaliClass.setSourceFile(getSourceFile().getKey());
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
        getClassTypeId().append(writer);
        getSuperClassId().append(writer);
        getSourceFile().append(writer);
        writer.newLine();
        TypeList interfaces = getInterfaceTypeList();
        if(interfaces != null){
            interfaces.appendInterfaces(writer);
        }
        AnnotationSetKey annotation = getAnnotation();
        if(!annotation.isEmpty()){
            writer.newLine();
            writer.newLine();
            writer.appendComment("annotations");
            writer.appendAllWithDoubleNewLine(annotation.iterator());
            writer.newLine();
        }
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
        if(classId == this){
            return 0;
        }
        return SectionTool.compareIdx(getId(), classId.getId());
    }
    @Override
    public String toString(){
        if(isReading()){
            return ".class " + getKey();
        }
        return SmaliWriter.toStringSafe(this);
    }


    private static final int SIZE = 32;
}
