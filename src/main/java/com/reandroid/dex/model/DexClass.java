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
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.TypeListReference;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.NullValue;
import com.reandroid.dex.value.StringValue;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.FileUtil;

import java.io.*;
import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class DexClass extends DexDeclaration implements Comparable<DexClass> {
    private final DexFile dexFile;
    private final ClassId classId;

    public DexClass(DexFile dexFile, ClassId classId){
        this.dexFile = dexFile;
        this.classId = classId;
    }

    public void replaceKeys(Key search, Key replace){
        getId().replaceKeys(search, replace);
    }
    public Set<DexClass> getRequired(){
        return getRequired(null);
    }
    public Set<DexClass> getRequired(Predicate<TypeKey> exclude){
        Set<DexClass> results = new HashSet<>();
        results.add(this);
        searchRequired(exclude, results);
        return results;
    }
    private void searchRequired(Predicate<TypeKey> exclude, Set<DexClass> results){
        DexClassRepository dexClassRepository = getClassRepository();
        Iterator<TypeKey> iterator = usedTypes();
        while (iterator.hasNext()){
            TypeKey typeKey = iterator.next();
            if(exclude != null && !exclude.test(typeKey)){
                continue;
            }
            DexClass dexClass = dexClassRepository.getDexClass(typeKey);
            if(dexClass == null || results.contains(dexClass)){
                continue;
            }
            results.add(dexClass);
            dexClass.searchRequired(exclude, results);
        }
    }
    public Iterator<TypeKey> usedTypes(){
        Iterator<Key> iterator = ComputeIterator.of(getId().usedIds(), IdItem::getKey);
        Iterator<Key> mentioned = new IterableIterator<Key, Key>(iterator) {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<Key> iterator(Key element) {
                return (Iterator<Key>) element.mentionedKeys();
            }
        };
        return InstanceIterator.of(mentioned, TypeKey.class);
    }
    public DexMethod getStaticConstructor(){
        MethodKey methodKey = MethodKey.STATIC_CONSTRUCTOR
                .changeDeclaring(getDefining());
        return getDeclaredMethod(methodKey);
    }
    public DexField getField(FieldKey fieldKey) {
        if(!isAccessibleTo(fieldKey.getDeclaring())){
            return null;
        }
        DexField dexField = getDeclaredField(fieldKey);
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
    public DexField getDeclaredField(FieldKey fieldKey) {
        Iterator<DexField> iterator = getDeclaredFields();
        while (iterator.hasNext()){
            DexField dexField = iterator.next();
            if(fieldKey.equals(dexField.getKey(), false, true)){
                return dexField;
            }
        }
        return null;
    }
    public DexMethod getMethod(MethodKey methodKey) {
        DexMethod dexMethod = getDeclaredMethod(methodKey);
        if(dexMethod != null) {
            return dexMethod;
        }
        Iterator<DexClass> iterator = getSuperTypes();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            dexMethod = dexClass.getDeclaredMethod(methodKey);
            if(dexMethod == null){
                continue;
            }
            if(!dexMethod.isAccessibleTo(methodKey.getDeclaring())) {
                // TODO: should not reach here ?
                continue;
            }
            return dexMethod;
        }
        return null;
    }
    public Iterator<DexMethod> getMethods(MethodKey methodKey) {
        return CombiningIterator.two(getDeclaredMethods(methodKey),
                ComputeIterator.of(getSuperTypes(), dexClass -> {
                    DexMethod method = dexClass.getDeclaredMethod(methodKey);
                    if (method != null && method.isAccessibleTo(methodKey.getDeclaring())) {
                        return method;
                    }
                    return null;
                }));
    }
    public Iterator<DexMethod> getExtending(MethodKey methodKey) {
        return CombiningIterator.of(getDeclaredMethod(methodKey),
                ComputeIterator.of(getExtending(),
                        dexClass -> dexClass.getExtending(methodKey)));
    }
    public Iterator<DexMethod> getImplementations(MethodKey methodKey) {
        return CombiningIterator.of(getDeclaredMethod(methodKey),
         ComputeIterator.of(getImplementations(),
                dexClass -> dexClass.getImplementations(methodKey)));
    }
    public Iterator<MethodKey> getOverridingKeys(MethodKey methodKey) {
        MethodKey key = methodKey.changeDeclaring(getKey());
        return CombiningIterator.of(CombiningIterator.singleOne(
                key,
                SingleIterator.of(getBridgedMethod(methodKey))
                ),
                ComputeIterator.of(getOverriding(),
                        dexClass -> dexClass.getOverridingKeys(key)));
    }
    private MethodKey getBridgedMethod(MethodKey methodKey){
        DexMethod dexMethod = getDeclaredMethod(methodKey);
        if(dexMethod == null){
            return null;
        }
        dexMethod = dexMethod.getBridged();
        if(dexMethod == null){
            return null;
        }
        return dexMethod.getKey();
    }
    public boolean containsDeclaredMethod(MethodKey methodKey) {
        Iterator<DexMethod> iterator = getDeclaredMethods();
        while (iterator.hasNext()){
            DexMethod dexMethod = iterator.next();
            MethodKey key = dexMethod.getKey();
            if(methodKey.equals(key, false, false)){
                return true;
            }
        }
        return false;
    }
    public DexMethod getDeclaredMethod(MethodKey methodKey) {
        Iterator<DexMethod> iterator = getDeclaredMethods();
        while (iterator.hasNext()){
            DexMethod dexMethod = iterator.next();
            MethodKey key = dexMethod.getKey();
            if(methodKey.equals(key, false, false)){
                return dexMethod;
            }
        }
        return null;
    }
    public Iterator<DexMethod> getDeclaredMethods(MethodKey methodKey) {
        return FilterIterator.of(getDeclaredMethods(),
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
        return search(getSuperClassKey());
    }
    public Iterator<DexField> getDeclaredFields() {
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
        return ComputeIterator.of(classData.getStaticFields(), this::createField);
    }
    public Iterator<DexField> getInstanceFields() {
        ClassData classData = getClassData();
        if(classData == null){
            return EmptyIterator.of();
        }
        return ComputeIterator.of(classData.getInstanceFields(), this::createField);
    }
    public Iterator<DexMethod> getDeclaredMethods(Predicate<DexMethod> filter) {
        Iterator<DexMethod> iterator = getDeclaredMethods();
        if(filter == null){
            return iterator;
        }
        return FilterIterator.of(iterator, filter);
    }
    public Iterator<DexMethod> getDeclaredMethods() {
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
    public DexMethod getOrCreateDirectMethod(MethodKey methodKey){
        return createMethod(getOrCreateClassData().getOrCreateDirect(methodKey));
    }
    public DexMethod getOrCreateVirtualMethod(MethodKey methodKey){
        return createMethod(getOrCreateClassData().getOrCreateVirtual(methodKey));
    }
    public DexMethod getOrCreateStaticMethod(MethodKey methodKey){
        DexMethod dexMethod = createMethod(getOrCreateClassData().getOrCreateDirect(methodKey));
        MethodDef methodDef = dexMethod.getDefinition();
        methodDef.addAccessFlag(AccessFlag.STATIC);
        return dexMethod;
    }

    DexField createField(FieldDef fieldDef){
        return new DexField(this, fieldDef);
    }
    DexMethod createMethod(MethodDef methodDef){
        return new DexMethod(this, methodDef);
    }

    public boolean isInterface() {
        return AccessFlag.INTERFACE.isSet(getAccessFlagsValue());
    }
    public boolean isEnum() {
        return AccessFlag.ENUM.isSet(getAccessFlagsValue());
    }
    public Iterator<DexInstruction> getDexInstructions(){
        return getDexInstructions(null);
    }
    public Iterator<DexInstruction> getDexInstructions(Predicate<DexMethod> filter){
        return new IterableIterator<DexMethod, DexInstruction>(getDeclaredMethods(filter)) {
            @Override
            public Iterator<DexInstruction> iterator(DexMethod element) {
                return element.getInstructions();
            }
        };
    }
    public void decode(SmaliWriter writer, File outDir) throws IOException {
        File file = new File(outDir, toFilePath());
        File dir = file.getParentFile();
        if(dir != null && !dir.exists() && !dir.mkdirs()){
            throw new IOException("Failed to create dir: " + dir);
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        writer.setWriter(new OutputStreamWriter(outputStream));
        append(writer);
        writer.close();
        outputStream.close();
    }
    private String toFilePath(){
        String name = getDefining().getTypeName();
        name = name.substring(1, name.length()-1);
        name = name.replace('/', File.separatorChar);
        return name + ".smali";
    }

    @Override
    public DexFile getDexFile() {
        return dexFile;
    }
    @Override
    public ClassId getId() {
        return classId;
    }
    @Override
    public DexClass getDexClass(){
        return this;
    }
    @Override
    public TypeKey getKey() {
        return getId().getKey();
    }
    @Override
    public ClassId getDefinition(){
        return getId();
    }

    public TypeKey getSuperClassKey(){
        return getId().getSuperClassKey();
    }
    public void setSuperClass(TypeKey superClass){
        getId().setSuperClass(superClass);
    }
    public String getSourceFileName(){
        return getId().getSourceFileName();
    }
    public void setSourceFile(String sourceFile){
        getId().setSourceFile(sourceFile);
    }

    public Iterator<DexClass> getInterfaceClasses(){
        return ComputeIterator.of(getInterfaces(), this::search);
    }
    DexClass search(TypeKey typeKey){
        return getClassRepository().getDexClass(typeKey);
    }
    public Iterator<TypeKey> getInterfaces(){
        return getId().getInterfaceKeys();
    }
    public void addInterface(String typeName) {
        TypeListReference reference = getId().getInterfacesReference();
        reference.add(typeName);
    }
    public void clearInterfaces() {
        TypeListReference reference = getId().getInterfacesReference();
        reference.setItem((TypeList) null);
    }
    public void removeAnnotations(Predicate<AnnotationItem> filter) {
        ClassId classId = getId();
        AnnotationSet annotationSet = classId.getClassAnnotations();
        if(annotationSet == null) {
            return;
        }
        annotationSet.remove(filter);
        annotationSet.refresh();
        if(annotationSet.size() == 0){
            annotationSet.removeSelf();
            classId.setClassAnnotations(null);
            AnnotationsDirectory directory = getId().getAnnotationsDirectory();
            if(directory != null && directory.isEmpty()){
                directory.removeSelf();
                classId.setAnnotationsDirectory(null);
            }
        }
        getId().refresh();
    }
    public void fixDalvikInnerClassName(){
        AnnotationItem annotationItem = getId().getDalvikInnerClass();
        if(annotationItem == null){
            return;
        }
        AnnotationElement element = annotationItem.getElement("name");
        if(element == null){
            return;
        }
        DexValueBlock<?> valueBlock = element.getValue();
        if(!valueBlock.is(DexValueType.STRING)){
            return;
        }
        TypeKey typeKey = getKey();
        if(!typeKey.isInnerName()){
            element.setValue(new NullValue());
            return;
        }
        StringValue value = (StringValue) valueBlock;
        value.setString(typeKey.getSimpleInnerName());
    }
    public List<Key> fixAccessibility(){
        DexClassRepository repository = getClassRepository();
        if(repository == null){
            return ArrayCollection.empty();
        }
        List<Key> results = new ArrayCollection<>();
        ClassId classId = getId();
        Set<Key> checked = new HashSet<>();
        checked.add(classId.getKey());
        Iterator<IdItem> iterator = classId.usedIds();
        while (iterator.hasNext()){
            Key key = iterator.next().getKey();
            if(checked.add(key)){
                if(fixAccessibility(repository.getDexDeclaration(key))){
                    results.add(key);
                }
            }
        }
        return results;
    }
    private boolean fixAccessibility(DexDeclaration declaration){
        if(declaration != null && !declaration.isAccessibleTo(this)){
            declaration.addAccessFlag(AccessFlag.PUBLIC);
            return true;
        }
        return false;
    }
    public TypeKey getDalvikEnclosingClass(){
        Key key = getId().getDalvikEnclosing();
        if(key != null){
            return key.getDeclaring();
        }
        return null;
    }
    public String getDalvikInnerClassName(){
        DexValue dexValue = getAnnotationValue(TypeKey.DALVIK_InnerClass, "name");
        if(dexValue != null){
            return dexValue.getString();
        }
        return null;
    }
    public void updateDalvikInnerClassName(String name){
        DexValue dexValue = getAnnotationValue(
                TypeKey.DALVIK_InnerClass, "name");
        if(dexValue != null){
            dexValue.setString(name);
        }
    }
    public void createDalvikInnerClassName(String name){
        DexValue dexValue = getOrCreateAnnotationValue(
                TypeKey.DALVIK_InnerClass,
                "name",
                DexValueType.STRING);
        dexValue.setString(name);
    }

    @Override
    public Iterator<DexAnnotation> getAnnotations(){
        AnnotationSet annotationSet = getId().getClassAnnotations();
        if(annotationSet != null){
            return ComputeIterator.of(annotationSet.iterator(), annotationItem ->
                    DexAnnotation.create(DexClass.this, annotationItem));
        }
        return EmptyIterator.of();
    }
    @Override
    public Iterator<DexAnnotation> getAnnotations(TypeKey typeKey){
        AnnotationSet annotationSet = getId().getClassAnnotations();
        if(annotationSet != null){
            return ComputeIterator.of(annotationSet.getAll(typeKey), annotationItem ->
                    DexAnnotation.create(DexClass.this, annotationItem));
        }
        return EmptyIterator.of();
    }
    @Override
    public DexAnnotation getAnnotation(TypeKey typeKey){
        AnnotationSet annotationSet = getId().getClassAnnotations();
        if(annotationSet != null){
            return DexAnnotation.create(this, annotationSet.get(typeKey));
        }
        return null;
    }
    @Override
    public DexAnnotation getOrCreateAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getId().getOrCreateClassAnnotations().getOrCreate(typeKey));
    }
    @Override
    public DexAnnotation newAnnotation(TypeKey typeKey){
        return DexAnnotation.create(this,
                getId().getOrCreateClassAnnotations().addNewItem(typeKey));
    }

    ClassData getOrCreateClassData(){
        return getId().getOrCreateClassData();
    }
    ClassData getClassData(){
        return getId().getClassData();
    }
    @Override
    public void removeSelf(){
        getDefinition().removeSelf();
    }
    public void edit(){
        getId().edit();
    }
    @Override
    public int compareTo(DexClass dexClass) {
        return getKey().compareTo(dexClass.getKey());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getClassData();
        getId().append(writer);
    }
    public void writeSmali(SmaliWriter writer, File dir) throws IOException {
        File file = toSmaliFile(dir);
        FileUtil.writeUtf8(file, toSmali(writer));
    }
    public File toSmaliFile(File dir){
        return new File(dir, buildSmaliPath());
    }
    public String buildSmaliPath(){
        String type = getKey().getTypeName();
        type = type.substring(1, type.length() - 1);
        type = type.replace('/', File.separatorChar);
        type = type + ".smali";
        return type;
    }
    public String toSmali() throws IOException {
        return SmaliWriter.toString(this);
    }
    public String toSmali(SmaliWriter writer) throws IOException {
        return SmaliWriter.toString(writer,this);
    }

    @Override
    public ElementType getElementType(){
        return ElementType.TYPE;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
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
        if(!isInSameFile(dexClass)){
            return false;
        }
        return getKey().equals(dexClass.getKey());
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
