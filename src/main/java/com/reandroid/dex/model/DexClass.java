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
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.*;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Objects;

public class DexClass implements Comparable<DexClass>{
    private final ClassId classId;

    private List<DexField> staticFields;
    private List<DexField> instanceFields;

    private List<DexMethod> directMethods;
    private List<DexMethod> virtualMethods;

    public DexClass(ClassId classId){
        this.classId = classId;

        this.staticFields = EmptyList.of();
        this.instanceFields = EmptyList.of();

        this.directMethods = EmptyList.of();
        this.virtualMethods = EmptyList.of();
    }

    public List<DexField> getStaticFields() {
        return staticFields;
    }
    public List<DexField> getInstanceFields() {
        return instanceFields;
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
    public String getSourceFile(){
        StringData stringData = getClassId().getSourceFile();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public TypeId[] getInterfaces(){
        return getClassId().getInterfaceTypeIds();
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
        loadFields();
        loadMethods();
    }
    private void loadFields(){
        ClassData classData = getClassData();
        if(classData == null){
            return;
        }
        this.staticFields = DexField.create(this, classData.getStaticFields());
        this.instanceFields = DexField.create(this, classData.getInstanceFields());
    }
    private void loadMethods(){
        ClassData classData = getClassData();
        if(classData == null){
            return;
        }
        this.directMethods = DexMethod.create(this, classData.getDirectMethods());
        this.virtualMethods = DexMethod.create(this, classData.getVirtualMethods());
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
