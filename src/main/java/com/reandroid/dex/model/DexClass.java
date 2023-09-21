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

import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.ItemOffsetReference;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.*;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.EmptyList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DexClass implements Comparable<DexClass>{
    private final ClassId classId;

    public DexClass(ClassId classId){
        this.classId = classId;
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


    public void decode(File outDir) throws IOException {
        ClassId classId = getClassId();
        File file = new File(outDir, toFilePath());
        File dir = file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        SmaliWriter writer = new SmaliWriter(new OutputStreamWriter(outputStream));
        classId.append(writer);
        writer.close();
        outputStream.close();
    }
    private String toFilePath(){
        String name = getName();
        name = name.substring(1, name.length()-1);
        name = name.replace('/', File.separatorChar);
        return name + ".smali";
    }

    public ClassId getClassId() {
        return classId;
    }
    public String getName(){
        return getClassId().getName();
    }
    public String getSuperClass(){
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
        return getClassId().getClassData();
    }
    EncodedArray getStaticValues(){
        return getClassId().getStaticValues();
    }
    AnnotationsDirectory getAnnotationsDirectory(){
        return getClassId().getAnnotationsDirectory();
    }



    public void refresh(){
    }

    @Override
    public int compareTo(DexClass dexClass) {
        String name1 = getName();
        if(name1 == null){
            name1 = "null";
        }
        String name2 = dexClass.getName();
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
        return Objects.equals(getName(), dexClass.getName());
    }
    @Override
    public int hashCode() {
        String name = getName();
        if(name != null){
            return name.hashCode();
        }
        return 0;
    }
    @Override
    public String toString() {
        String name = getName();
        if(name != null){
            return name;
        }
        return "null";
    }

    public static DexClass create(ClassId classId){
        DexClass dexClass = new DexClass(classId);
        dexClass.refresh();
        return dexClass;
    }
}
