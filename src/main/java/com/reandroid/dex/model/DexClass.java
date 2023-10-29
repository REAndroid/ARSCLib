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

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.data.*;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DexClass extends DexDef implements Comparable<DexClass> {
    private final DexFile dexFile;
    private final ClassId classId;

    public DexClass(DexFile dexFile, ClassId classId){
        this.dexFile = dexFile;
        this.classId = classId;
    }

    public void cleanKotlin(){
        getClassData();
        cleanKotlinAnnotation();
        cleanKotlinIntrinsics();
    }
    private void cleanKotlinIntrinsics(){
        ClassData classData = getClassData();
        if(classData == null){
            return;
        }
        MethodDefArray defArray = classData.getDirectMethodsArray();
        for(MethodDef methodDef : defArray.getChildes()){
            cleanKotlinIntrinsics(methodDef);
        }
        defArray = classData.getVirtualMethodsArray();
        for(MethodDef methodDef : defArray.getChildes()){
            cleanKotlinIntrinsics(methodDef);
        }
    }
    private void cleanKotlinIntrinsics(MethodDef methodDef){
        InstructionList instructionList = methodDef.getInstructionList();
        if(instructionList == null || methodDef.getCodeItem().getTryBlock() != null){
            return;
        }
        Ins previous = null;
        List<Ins> insList = CollectionUtil.toList(instructionList.iterator());
        for(Ins ins : insList) {
            if(ins.toString().contains("Lkotlin/jvm/internal/Intrinsics;->f(Ljava/lang/Object;Ljava/lang/String;)V")){
                if(previous != null && previous.toString().contains("const-string")){
                    instructionList.remove(previous);
                    instructionList.remove(ins);
                    previous = null;
                    continue;
                }
            }
            if(!ins.toString().contains("move-")){
                previous = ins;
            }
        }
    }
    private void cleanKotlinAnnotation(){
        ClassId classId = getClassId();
        AnnotationSet annotationSet = classId.getClassAnnotations();
        if(annotationSet == null){
            return;
        }
        List<AnnotationItem> annotationItems = getKotlin();
        if(annotationItems.isEmpty()){
            return;
        }
        AnnotationsDirectory directory = classId.getUniqueAnnotationsDirectory();
        annotationSet = directory.getClassAnnotations();
        for(AnnotationItem annotationItem : annotationItems){
            annotationSet.remove(annotationItem);
        }
        if(annotationSet.size() !=  0){
            return;
        }
        classId.setClassAnnotations(null);
    }
    private List<AnnotationItem> getKotlin(){
        ClassId classId = getClassId();
        AnnotationSet annotationSet = classId.getClassAnnotations();
        if(annotationSet == null){
            return EmptyList.of();
        }
        Iterator<AnnotationItem> iterator = annotationSet.iterator();
        iterator = FilterIterator.of(iterator, new Predicate<AnnotationItem>() {
            @Override
            public boolean test(AnnotationItem item) {
                String type = item.getTypeId().getName();
                return "Lkotlin/Metadata;".equals(type);
            }
        });
        return CollectionUtil.toList(iterator);
    }
    ///////////////////////////////////////////
    public DexField getField(FieldKey fieldKey) {
        if(!isAccessibleTo(fieldKey.getDefiningKey())){
            return null;
        }
        DexField dexField = getDefinedField(fieldKey);
        if(dexField != null) {
            return dexField;
        }
        DexClass superClass = getSuperClass();
        if (superClass != null){
            dexField = superClass.getField(fieldKey);
            if(dexField != null){
                if(dexField.isAccessibleTo(getDefining())){
                    return dexField;
                }else {
                    return null;
                }
            }
        }
        Iterator<DexClass> iterator = getInterfaceClasses();
        while (iterator.hasNext()) {
            dexField = iterator.next().getField(fieldKey);
            if(dexField != null){
                if(dexField.isAccessibleTo(getDefining())){
                    return dexField;
                }
            }
        }
        return null;
    }
    public DexField getDefinedField(FieldKey fieldKey) {
        Iterator<DexField> iterator = getFields();
        while (iterator.hasNext()){
            DexField dexField = iterator.next();
            if(fieldKey.equals(dexField.getFieldKey(), false, true)){
                return dexField;
            }
        }
        return null;
    }
    public DexMethod getMethod(MethodKey methodKey) {
        DexMethod dexMethod = getDefinedMethod(methodKey);
        if(dexMethod != null) {
            return dexMethod;
        }
        Iterator<DexClass> iterator = getSuperTypes();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            dexMethod = dexClass.getDefinedMethod(methodKey);
            if(dexMethod == null){
                continue;
            }
            if(!dexMethod.isAccessibleTo(methodKey.getDefiningKey())) {
                // TODO: should not reach here ?
                continue;
            }
            return dexMethod;
        }
        return null;
    }
    public Iterator<DexMethod> getMethods(MethodKey methodKey) {
        return CombiningIterator.two(getDefinedMethods(methodKey),
                ComputeIterator.of(getSuperTypes(), dexClass -> {
                    DexMethod method = dexClass.getDefinedMethod(methodKey);
                    if (method != null && method.isAccessibleTo(methodKey.getDefiningKey())) {
                        return method;
                    }
                    return null;
                }));
    }
    public Iterator<DexMethod> getExtending(MethodKey methodKey) {
        return CombiningIterator.of(getDefinedMethod(methodKey),
                ComputeIterator.of(getExtending(),
                        dexClass -> dexClass.getExtending(methodKey)));
    }
    public Iterator<DexMethod> getImplementations(MethodKey methodKey) {
        return CombiningIterator.of(getDefinedMethod(methodKey),
         ComputeIterator.of(getImplementations(),
                dexClass -> dexClass.getImplementations(methodKey)));
    }
    public Iterator<MethodKey> getOverridingKeys(MethodKey methodKey) {
        MethodKey key = methodKey.changeDefining(getKey());
        return CombiningIterator.of(CombiningIterator.singleOne(
                key,
                SingleIterator.of(getBridged(methodKey))
                ),
                ComputeIterator.of(getOverriding(),
                        dexClass -> dexClass.getOverridingKeys(key)));
    }
    private MethodKey getBridged(MethodKey methodKey){
        DexMethod dexMethod = getDefinedMethod(methodKey);
        if(dexMethod == null){
            return null;
        }
        dexMethod = dexMethod.getBridged();
        if(dexMethod == null){
            return null;
        }
        return dexMethod.getKey();
    }
    public DexMethod getDefinedMethod(MethodKey methodKey) {
        Iterator<DexMethod> iterator = getMethods();
        while (iterator.hasNext()){
            DexMethod dexMethod = iterator.next();
            MethodKey key = dexMethod.getKey();
            if(methodKey.equals(key, false, false)){
                return dexMethod;
            }
        }
        return null;
    }
    public Iterator<DexMethod> getDefinedMethods(MethodKey methodKey) {
        return FilterIterator.of(getMethods(),
                dexMethod -> methodKey.equals(dexMethod.getKey(), false, false));
    }
    public Iterator<DexClass> getOverridingAndSuperTypes(){
        return CombiningIterator.two(getOverriding(), getSuperTypes());
    }
    public Iterator<DexClass> getSuperTypes(){
        final TypeKey overflow = getDefining();
        Iterator<DexClass> iterator = CombiningIterator.two(SingleIterator.of(getSuperClass()),
                getInterfaceClasses());
        return new IterableIterator<DexClass, DexClass>(iterator) {
            @Override
            public Iterator<DexClass> iterator(DexClass element) {
                if(overflow.equals(element.getDefining())){
                    throw new IllegalArgumentException("Recursive class super: " + overflow);
                }
                return CombiningIterator.two(SingleIterator.of(element), element.getSuperTypes());
            }
        };
    }
    public Iterator<DexClass> getOverriding(){
        return CombiningIterator.two(getExtending(), getImplementations());
    }
    public Iterator<DexClass> getExtending(){
        return getDexFile().searchExtending(getKey());
    }
    public Iterator<DexClass> getImplementations(){
        return getDexFile().searchImplementations(getKey());
    }
    public DexClass getSuperClass() {
        return search(getSuperClassName());
    }
    public Iterator<DexField> getFields() {
        return CombiningIterator.two(getStaticFields(), getInstanceFields());
    }

    public DexField getOrCreateStaticField(FieldKey fieldKey){
        return createField(getOrCreateStatic(fieldKey));
    }
    public FieldDef getOrCreateStatic(FieldKey fieldKey){
        return getOrCreateClassData().getOrCreateStatic(fieldKey);
    }
    public Iterator<? extends DexField> getStaticFields() {
        ClassData classData = getClassData();
        if(classData == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(classData.getStaticFieldsArray().iterator(), this::createField);
    }
    public Iterator<DexField> getInstanceFields() {
        ClassData classData = getClassData();
        if(classData == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(classData.getInstanceFieldsArray().iterator(), this::createField);
    }
    public Iterator<DexMethod> getMethods() {
        return new CombiningIterator<>(getDirectMethods(), getVirtualMethods());
    }
    public Iterator<DexMethod> getDirectMethods() {
        ClassData classData = getClassData();
        if(classData == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(classData.getDirectMethods(), this::createMethod);
    }
    public Iterator<DexMethod> getVirtualMethods() {
        ClassData classData = getClassData();
        if(classData == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(classData.getVirtualMethods(), this::createMethod);
    }

    DexField createField(FieldDef fieldDef){
        return new DexField(this, fieldDef);
    }
    DexMethod createMethod(MethodDef methodDef){
        return new DexMethod(this, methodDef);
    }

    @Override
    public String getAccessFlags() {
        return AccessFlag.formatForClass(getAccessFlagsValue());
    }
    @Override
    int getAccessFlagsValue() {
        return getClassId().getAccessFlagsValue();
    }

    public void decode(File outDir) throws IOException {
        File file = new File(outDir, toFilePath());
        File dir = file.getParentFile();
        if(dir != null && !dir.exists() && !dir.mkdirs()){
            throw new IOException("Failed to create dir: " + dir);
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        SmaliWriter writer = new SmaliWriter(new OutputStreamWriter(outputStream));
        append(writer);
        writer.close();
        outputStream.close();
    }
    private String toFilePath(){
        String name = getDefining().getType();
        name = name.substring(1, name.length()-1);
        name = name.replace('/', File.separatorChar);
        return name + ".smali";
    }

    public DexFile getDexFile() {
        return dexFile;
    }
    public void addAccessFlag(AccessFlag accessFlag){
        getClassId().addAccessFlag(accessFlag);
    }
    public ClassId getClassId() {
        return classId;
    }
    @Override
    public TypeKey getKey() {
        return getClassId().getKey();
    }
    @Override
    public TypeKey getDefining(){
        return getClassId().getKey();
    }
    public String getSuperClassName(){
        return getClassId().getSuperClassId().getName();
    }
    public void setSuperClass(String superClass){
        getClassId().setSuperClass(superClass);
    }
    public String getSourceFile(){
        StringData stringData = getClassId().getSourceFile();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setSourceFile(String sourceFile){
        getClassId().setSourceFile(sourceFile);
    }

    public Iterator<DexClass> getInterfaceClasses(){
        return ComputeIterator.of(getInterfaces(), this::search);
    }
    DexClass search(String typeName){
        return getDexFile().search(typeName);
    }
    public Iterator<String> getInterfaces(){
        TypeList typeList = getClassId().getInterfaces();
        if(typeList != null){
            return typeList.getTypeNames();
        }
        return EmptyIterator.of();
    }
    public void addInterface(String typeName) {
        DataItemIndirectReference<TypeList> reference = getClassId().getInterfacesReference();
        TypeList typeList = reference.getOrCreate();
        typeList.add(typeName);
    }
    public void addInterfaces(Iterator<String> iterator){
        DataItemIndirectReference<TypeList> reference = getClassId().getInterfacesReference();
        TypeList typeList = reference.getOrCreate();
        typeList.addAll(iterator);
    }
    public void clearInterfaces() {
        DataItemIndirectReference<TypeList> reference = getClassId().getInterfacesReference();
        reference.setItem((TypeList) null);
    }

    public void removeAnnotations(Predicate<AnnotationItem> filter) {
        ClassId classId = getClassId();
        AnnotationSet annotationSet = classId.getClassAnnotations();
        if(annotationSet == null) {
            return;
        }
        annotationSet.remove(filter);
        annotationSet.refresh();
        if(annotationSet.size() == 0){
            annotationSet.removeSelf();
            classId.setClassAnnotations(null);
            AnnotationsDirectory directory = getClassId().getAnnotationsDirectory();
            if(directory != null && directory.isEmpty()){
                directory.removeSelf();
                classId.setAnnotationsDirectory(null);
            }
        }
        getClassId().refresh();
    }
    public AnnotationSet getAnnotations(){
        return getClassId().getClassAnnotations();
    }

    ClassData getOrCreateClassData(){
        return getClassId().getOrCreateClassData();
    }
    ClassData getClassData(){
        return getClassId().getClassData();
    }
    EncodedArray getStaticValues(){
        return getClassId().getStaticValues();
    }
    AnnotationsDirectory getAnnotationsDirectory(){
        return getClassId().getAnnotationsDirectory();
    }



    public void refresh() {
    }

    @Override
    public int compareTo(DexClass dexClass) {
        String name1 = getClassName();
        if(name1 == null){
            name1 = "null";
        }
        String name2 = dexClass.getClassName();
        if(name2 == null){
            name2 = "null";
        }
        return name1.compareTo(name2);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DexClass dexClass = (DexClass) obj;
        return Objects.equals(getClassName(), dexClass.getClassName());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassData();
        getClassId().append(writer);
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        try {
            append(smaliWriter);
            smaliWriter.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
