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
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.item.*;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.*;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class DexClass extends DexDef implements Comparable<DexClass> {
    private final DexFile dexFile;
    private final ClassId classId;

    public DexClass(DexFile dexFile, ClassId classId){
        this.dexFile = dexFile;
        this.classId = classId;
    }

    public DexField getField(String fieldKey) {
        DexField dexField = getDefinedField(fieldKey);
        if(dexField != null) {
            return dexField;
        }
        Iterator<DexClass> iterator = getSuperClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            dexField = dexClass.getDefinedField(fieldKey);
            if(dexField == null){
                continue;
            }
            if(dexField.isAccessibleTo(this)) {
                return dexField;
            }
        }
        return null;
    }
    public DexField getDefinedField(String fieldKey) {
        Iterator<DexField> iterator = getFields();
        while (iterator.hasNext()){
            DexField dexField = iterator.next();
            if(fieldKey.equals(dexField.getKey())){
                return dexField;
            }
        }
        return null;
    }
    public DexMethod getMethod(String methodKey) {
        DexMethod dexMethod = getDefinedMethod(methodKey);
        if(dexMethod != null) {
            return dexMethod;
        }
        Iterator<DexClass> iterator = getSuperClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            dexMethod = dexClass.getDefinedMethod(methodKey);
            if(dexMethod == null){
                continue;
            }
            if(!dexMethod.isAccessibleTo(this)) {
                // TODO: should not reach here ?
                continue;
            }
            return dexMethod;
        }
        return null;
    }
    public DexMethod getDefinedMethod(String methodKey) {
        Iterator<DexMethod> iterator = getMethods();
        while (iterator.hasNext()){
            DexMethod dexMethod = iterator.next();
            if(methodKey.equals(dexMethod.getKey())){
                return dexMethod;
            }
        }
        return null;
    }
    public Iterator<DexClass> getSuperClasses(){
        return listSuperClasses().iterator();
    }
    public Set<DexClass> listSuperClasses(){
        Set<DexClass> results = new HashSet<>();
        listSuperClasses(results);
        return results;
    }
    private void listSuperClasses(Set<DexClass> results) {
        DexClass dexClass = getSuperClass();
        if(dexClass != null && !results.contains(dexClass)){
            results.add(dexClass);
            dexClass.listSuperClasses(results);
        }
        Iterator<String> interfaceNames = getInterfaces();
        while (interfaceNames.hasNext()){
            dexClass = dexFile.get(interfaceNames.next());
            if(dexClass != null && !results.contains(dexClass)){
                results.add(dexClass);
                dexClass.listSuperClasses(results);
            }
        }
    }

    public DexClass getSuperClass() {
        return dexFile.get(getSuperClassName());
    }
    public Iterator<DexField> getFields() {
        return new CombiningIterator<>(getStaticFields(), getInstanceFields());
    }
    public Iterator<DexField> getStaticFields() {
        ClassData classData = getClassData();
        if(classData != null){
            return ComputeIterator.of(classData
                    .getStaticFields().iterator(), this::createField);
        }
        return EmptyIterator.of();
    }
    public Iterator<DexField> getInstanceFields() {
        return ComputeIterator.of(getClassData()
                .getInstanceFields().iterator(), this::createField);
    }
    public Iterator<DexMethod> getMethods() {
        return new CombiningIterator<>(getDirectMethods(), getVirtualMethods());
    }
    public Iterator<DexMethod> getDirectMethods() {
        return ComputeIterator.of(getClassData()
                .getDirectMethods().iterator(), this::createMethod);
    }
    public Iterator<DexMethod> getVirtualMethods() {
        return ComputeIterator.of(getClassData()
                .getVirtualMethods().iterator(), this::createMethod);
    }

    DexField createField(FieldDef fieldDef){
        fieldDef.setClassId(getClassId());
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
        String name = getClassName();
        name = name.substring(1, name.length()-1);
        name = name.replace('/', File.separatorChar);
        return name + ".smali";
    }

    public DexFile getDexFile() {
        return dexFile;
    }
    public ClassId getClassId() {
        return classId;
    }
    @Override
    public String getKey() {
        return getClassName();
    }
    @Override
    public String getClassName() {
        return getClassId().getName();
    }
    public String getSuperClassName(){
        return getClassId().getSuperClass().getName();
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
        return ComputeIterator.of(getInterfaces(), DexClass.this.dexFile::get);
    }
    public Iterator<String> getInterfaces(){
        TypeList typeList = getClassId().getInterfaces();
        if(typeList != null){
            return typeList.getTypeNames();
        }
        return EmptyIterator.of();
    }
    public void addInterface(String typeName) {
        ItemOffsetReference<TypeList> reference = getClassId().getInterfacesReference();
        TypeList typeList = reference.getOrCreate();
        typeList.add(typeName);
    }
    public void addInterfaces(Iterator<String> iterator){
        ItemOffsetReference<TypeList> reference = getClassId().getInterfacesReference();
        TypeList typeList = reference.getOrCreate();
        typeList.addAll(iterator);
    }
    public void clearInterfaces() {
        ItemOffsetReference<TypeList> reference = getClassId().getInterfacesReference();
        reference.setItem(null);
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

    ClassData getClassData(){
        ClassId classId = getClassId();
        ClassData classData = classId.getClassData();
        classData.setClassId(classId);
        return classData;
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
