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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.*;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;
import java.util.List;

public class DexFile implements VisitableInteger {

    private final DexFileBlock dexFileBlock;
    private DexDirectory dexDirectory;
    private Object mTag;

    public DexFile(DexFileBlock dexFileBlock){
        this.dexFileBlock = dexFileBlock;
    }

    public void cleanDuplicateDebugLines(){
        Section<CodeItem> section = get(SectionType.CODE);
        if(section == null){
            return;
        }
        for(CodeItem codeItem : section){
            DebugInfo debugInfo = codeItem.getDebugInfo();
            if(debugInfo == null){
                continue;
            }
            debugInfo.getDebugSequence().cleanDuplicates();
        }
    }

    public DexDirectory getDexDirectory() {
        return dexDirectory;
    }
    public void setDexDirectory(DexDirectory dexDirectory) {
        this.dexDirectory = dexDirectory;
    }

    public Iterator<DexClass> getSubTypeClasses(TypeKey typeKey){
        return ComputeIterator.of(getSubTypeClassIds(typeKey), this::create);
    }
    public Iterator<DexClass> getExtendingClasses(TypeKey typeKey){
        return ComputeIterator.of(getExtendingClassIds(typeKey), this::create);
    }
    public Iterator<DexClass> getImplementClasses(TypeKey typeKey){
        return ComputeIterator.of(getImplementationIds(typeKey), this::create);
    }
    public Iterator<ClassId> getSubTypeClassIds(TypeKey superClass){
        return getDexFileBlock().getSubTypes(superClass);
    }
    public Iterator<ClassId> getExtendingClassIds(TypeKey superClass){
        return getDexFileBlock().getExtendingClassIds(superClass);
    }
    public Iterator<ClassId> getImplementationIds(TypeKey interfaceClass){
        return getDexFileBlock().getImplementationIds(interfaceClass);
    }
    public void loadSuperTypesMap(){
        getDexFileBlock().loadSuperTypesMap();
    }
    public void loadExtendingClassMap(){
        getDexFileBlock().loadExtendingClassMap();
    }
    public void loadInterfacesMap(){
        getDexFileBlock().loadInterfacesMap();
    }
    public DexClass getOrCreateClass(String type){
        return getOrCreateClass(new TypeKey(type));
    }
    public DexClass getOrCreateClass(TypeKey key){
        DexClass dexClass = search(key);
        if(dexClass != null){
            return dexClass;
        }
        ClassId classId = getOrCreateClassId(key);
        return create(classId);
    }
    public Object getTag() {
        return mTag;
    }
    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public String getSimpleName() {
        return getDexFileBlock().getSimpleName();
    }
    public void setSimpleName(String simpleName){
        getDexFileBlock().setSimpleName(simpleName);
    }

    public List<RField> listRFields() {
        List<RField> fieldList = CollectionUtil.toUniqueList(getRFields());
        fieldList.sort(CompareUtil.getComparableComparator());
        return fieldList;
    }
    public Iterator<RField> getRFields() {
        return new MergingIterator<>(ComputeIterator.of(getRClasses(),
                RClass::getStaticFields));
    }
    public Iterator<RClass> getRClasses() {
        return ComputeIterator.of(getClassIds(), this::createRClass);
    }
    private RClass createRClass(ClassId classId) {
        if(RClass.isRClassName(classId)){
            return new RClass(this, classId);
        }
        return null;
    }
    public Iterator<DexClass> getDexClasses() {
        return ComputeIterator.of(getClassIds(), this::create);
    }

    public Iterator<DexClass> searchExtending(TypeKey typeKey){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.searchExtending(typeKey);
        }
        return getExtendingClasses(typeKey);
    }
    public Iterator<DexClass> searchImplementations(TypeKey typeKey){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.searchImplementations(typeKey);
        }
        return getImplementClasses(typeKey);
    }
    public DexClass search(String typeName){
        return search(TypeKey.create(typeName));
    }
    public DexClass search(TypeKey typeKey){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.getDexClass(typeKey);
        }
        return get(typeKey);
    }
    public DexClass get(String typeName){
        return get(new TypeKey(typeName));
    }
    public void loadRClass(TableBlock tableBlock){
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            loadRClass(packageBlock);
        }
    }
    public RClassParent loadRClass(PackageBlock packageBlock){
        String name = packageBlock.getName();
        if("android".equals(name)){
            name = "android_res";
        }
        name = DexUtils.toDalvikName(name + ".R");
        RClassParent rClassParent = getOrCreateRParent(name);
        rClassParent.initialize();
        rClassParent.load(packageBlock);
        return rClassParent;
    }
    public RClassParent getOrCreateRParent(String type){
        ClassId classId = getOrCreateClassId(new TypeKey(type));
        RClassParent rClassParent = new RClassParent(this, classId);
        rClassParent.initialize();
        return rClassParent;
    }
    public RClass getOrCreateRClass(TypeKey typeKey){
        ClassId classId = getOrCreateClassId(typeKey);
        return createRClass(classId);
    }
    public RClass getRClass(TypeKey key){
        Section<ClassId> section = getDexFileBlock().get(SectionType.CLASS_ID);
        ClassId classId = section.getPool().get(key);
        if(classId == null) {
            return null;
        }
        return createRClass(classId);
    }
    public boolean contains(Key key){
        return getDexFileBlock().getSectionList().contains(key);
    }
    public DexClass get(TypeKey key){
        Section<ClassId> section = getDexFileBlock().get(SectionType.CLASS_ID);
        ClassId classId = section.get(key);
        if(classId == null) {
            return null;
        }
        return create(classId);
    }
    public ClassId getOrCreateClassId(TypeKey key){
        Section<ClassId> section = getDexFileBlock().get(SectionType.CLASS_ID);
        DexSectionPool<ClassId> pool = section.getPool();
        ClassId classId = pool.get(key);
        if(classId != null) {
            return classId;
        }
        classId = pool.getOrCreate(key);
        classId.getOrCreateClassData();
        classId.setSuperClass("Ljava/lang/Object;");
        classId.setSourceFile(DexUtils.toSourceName(key.getType()));
        classId.addAccessFlag(AccessFlag.PUBLIC);
        System.err.println("Created: " + key);
        return classId;
    }
    public ClassId getClassId(Key key){
        Section<ClassId> section = getDexFileBlock().get(SectionType.CLASS_ID);
        if(section != null){
            return section.get(key);
        }
        return null;
    }
    private DexClass create(ClassId classId) {
        return new DexClass(this, classId);
    }
    public Marker getOrCreateMarker() {
        Marker marker = CollectionUtil.getFirst(getMarkers());
        if(marker != null){
            return marker;
        }
        marker = Marker.createR8();
        Section<StringId> stringSection = get(SectionType.STRING_ID);

        StringId stringId = stringSection.getPool().getOrCreate(
                new StringKey(marker.buildString()));
        marker.setStringId(stringId);

        marker.save();

        return marker;
    }
    public Iterator<Marker> getMarkers() {
        return Marker.parse(this);
    }
    public void clearMarkers(){
        List<StringId> removeList = CollectionUtil.toList(
                ComputeIterator.of(getMarkers(), Marker::getStringId));
        for(StringId stringId : removeList){
            stringId.removeSelf();
        }
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        Section<ClassData> section = get(SectionType.CLASS_DATA);
        for(ClassData classData : section){
            classData.visitIntegers(visitor);
        }
    }
    public void refreshFull() throws DexException {
        getDexFileBlock().refreshFull();
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getDexFileBlock().sortSection(order);
        refresh();
    }
    public void clearPools(){
        getDexFileBlock().clearPools();
    }
    public void sortStrings(){
        getDexFileBlock().sortStrings();
    }
    public Iterator<StringId> unusedStrings(){
        return unused(SectionType.STRING_ID);
    }
    public<T1 extends Block> Iterator<T1> unused(SectionType<T1> sectionType){
        return getWithUsage(sectionType, UsageMarker.USAGE_NONE);
    }
    public<T1 extends Block> Iterator<T1> getWithUsage(SectionType<T1> sectionType, int usage){
        return FilterIterator.of(get(sectionType).iterator(),
                item -> ((UsageMarker)item).containsUsage(usage));
    }
    public Iterator<StringId> getStringsWithUsage(int usage){
        return FilterIterator.of(getStringIds(),
                stringId -> stringId.containsUsage(usage));
    }
    public Iterator<String> getClassNames(){
        return ComputeIterator.of(getClassIds(), ClassId::getName);
    }

    public Iterator<Ins> getInstructions(){
        return new IterableIterator<ClassData, Ins>(getClassData()){
            @Override
            public Iterator<Ins> iterator(ClassData element) {
                return element.getInstructions();
            }
        };
    }
    public Iterator<MethodDef> getMethods(){
        return new IterableIterator<ClassData, MethodDef>(getClassData()){
            @Override
            public Iterator<MethodDef> iterator(ClassData element) {
                return element.getMethods();
            }
        };
    }
    public Iterator<FieldDef> getFields(){
        return new IterableIterator<ClassData, FieldDef>(getClassData()){
            @Override
            public Iterator<FieldDef> iterator(ClassData element) {
                return element.getFields();
            }
        };
    }
    public Iterator<FieldDef> getStaticFields(){
        return new IterableIterator<ClassData, FieldDef>(getClassData()){
            @Override
            public Iterator<FieldDef> iterator(ClassData element) {
                return element.getStaticFields();
            }
        };
    }
    public Iterator<FieldDef> getInstanceFields(){
        return new IterableIterator<ClassData, FieldDef>(getClassData()){
            @Override
            public Iterator<FieldDef> iterator(ClassData element) {
                return element.getInstanceFields();
            }
        };
    }
    public Iterator<ClassData> getClassData(){
        return ComputeIterator.of(getClassIds(), ClassId::getClassData);
    }
    public Iterator<ClassId> getClassIds(){
        return getItems(SectionType.CLASS_ID);
    }
    public Iterator<StringId> getStringIds(){
        return getItems(SectionType.STRING_ID);
    }
    public Iterator<StringData> getStringData(){
        return getItems(SectionType.STRING_DATA);
    }
    public<T1 extends Block> Iterator<T1> getItems(SectionType<T1> sectionType) {
        return getDexFileBlock().getItems(sectionType);
    }
    public Iterator<TypeId> getTypes(){
        return getItems(SectionType.TYPE_ID);
    }
    public <T1 extends Block> Iterator<T1> getAll(SectionType<T1> sectionType, Key key){
        return getDexFileBlock().getAll(sectionType, key);
    }
    public <T1 extends Block> T1 get(SectionType<T1> sectionType, Key key){
        return getDexFileBlock().get(sectionType, key);
    }
    public <T1 extends Block> Section<T1> get(SectionType<T1> sectionType){
        return getDexFileBlock().get(sectionType);
    }
    public void refresh() {
        getDexFileBlock().refresh();
    }
    public DexFileBlock getDexFileBlock() {
        return dexFileBlock;
    }
    public byte[] getBytes() {
        return getDexFileBlock().getBytes();
    }
    public void write(File file) throws IOException {
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        write(outputStream);
        outputStream.close();
    }
    public void write(OutputStream outputStream) throws IOException {
        byte[] bytes = getBytes();
        outputStream.write(bytes, 0, bytes.length);
    }

    public void serializePublicXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, PackageBlock.TAG_resources);

        List<RField> fieldList = listRFields();
        for(RField rField : fieldList) {
            rField.serializePublicXml(serializer);
        }

        serializer.text("\n");
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
    }

    @Override
    public String toString() {
        return getDexFileBlock().getMapList().toString();
    }

    public static boolean replaceRFields(RField rField, SizeXIns insConst){
        if(insConst == null || rField == null){
            return false;
        }
        MethodDef methodDef = insConst.getMethodDef();
        if(methodDef == null){
            return false;
        }
        if(rField.getClassName().equals(methodDef.getClassName())){
            return false;
        }
        Ins21c ins = Opcode.SGET.newInstance();
        ins.setRegister(0, ((RegistersSet)insConst).getRegister(0));
        insConst.replace(ins);
        ins.setSectionItem(rField.getFieldKey());
        return true;
    }

    public static DexFile read(byte[] dexBytes) throws IOException {
        return read(new BlockReader(dexBytes));
    }
    public static DexFile read(InputStream inputStream) throws IOException {
        return read(new BlockReader(inputStream));
    }
    public static DexFile read(File file) throws IOException {
        return read(new BlockReader(file));
    }
    public static DexFile read(BlockReader reader) throws IOException {
        DexFileBlock dexFileBlock = new DexFileBlock();
        dexFileBlock.readBytes(reader);
        reader.close();
        return new DexFile(dexFileBlock);
    }
}
