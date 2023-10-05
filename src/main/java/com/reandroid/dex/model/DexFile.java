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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.item.ClassData;
import com.reandroid.dex.item.MethodDef;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.DexFileBlock;
import com.reandroid.dex.sections.Marker;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DexFile implements VisitableInteger {

    private final DexFileBlock dexFileBlock;
    private Object mTag;

    public DexFile(DexFileBlock dexFileBlock){
        this.dexFileBlock = dexFileBlock;
    }

    public DexClass getOrCreateClass(String type){
        return getOrCreateClass(new TypeKey(type));
    }
    public DexClass getOrCreateClass(TypeKey key){
        DexClass dexClass = get(key);
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

    public void replaceRFields(){
        Map<Integer, RField> map = RField.mapRFields(listRFields().iterator());
        IntegerVisitor visitor = new IntegerVisitor() {
            @Override
            public void visit(Object sender, IntegerReference reference) {
                replaceRFields(DexFile.this, map, reference);
            }
        };
        this.visitIntegers(visitor);
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
    public DexClass get(String typeName){
        return get(new TypeKey(typeName));
    }
    public void loadRClass(TableBlock tableBlock){
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            loadRClass(packageBlock);
        }
    }
    public void loadRClass(PackageBlock packageBlock){
        String name = DexUtils.toDalvikName(packageBlock.getName() + ".R");
        RClassParent rClassParent = getOrCreateRParent(name);
        rClassParent.initialize();
        rClassParent.load(packageBlock);
        rClassParent.replaceConstIds();
        System.err.println(name);
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
        DexIdPool<ClassId> pool = section.getPool();
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
        Section<StringData> stringSection = get(SectionType.STRING_DATA);
        StringData stringData = stringSection.getPool().getOrCreate(new StringKey(marker.buildString()));
        marker.setStringData(stringData);
        marker.save();
        sortStrings();
        refresh();
        return marker;
    }
    public Iterator<Marker> getMarkers() {
        return Marker.parse(this);
    }
    public void clearMarkers(){
        List<StringData> removeList = CollectionUtil.toList(
                ComputeIterator.of(getMarkers(), Marker::getStringData));
        for(StringData stringData : removeList){
            stringData.removeSelf();
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
        getDexFileBlock().refreshFull();;
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getDexFileBlock().sortSection(order);
        refresh();
    }
    public void sortStrings(){
        getDexFileBlock().sortStrings();
    }
    public Iterator<StringId> unusedStrings(){
        return getStringsWithUsage(StringId.USAGE_NONE);
    }
    public Iterator<StringData> getStringsContainsUsage(int usage){
        return FilterIterator.of(getStringData(),
                stringData -> stringData.containsUsage(usage));
    }
    public Iterator<StringId> getStringsWithUsage(int usage){
        return FilterIterator.of(getStringIds(),
                stringId -> stringId.getUsageType() == usage);
    }
    public Iterator<String> getClassNames(){
        return ComputeIterator.of(getClassIds(), ClassId::getName);
    }
    Iterator<ClassId> getClassIds(){
        Section<ClassId> section = get(SectionType.CLASS_ID);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<StringId> getStringIds(){
        Section<StringId> section = get(SectionType.STRING_ID);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<StringData> getStringData(){
        Section<StringData> section = get(SectionType.STRING_DATA);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<String> getTypeNames(){
        return ComputeIterator.of(getTypes(), TypeId::getName);
    }
    public Iterator<TypeId> getTypes(){
        Section<TypeId> section = get(SectionType.TYPE_ID);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
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

    public static void replaceRFields(DexFile dexFile, Map<Integer, RField> map, IntegerReference reference){
        if(!(reference instanceof InsConst)){
            return;
        }
        InsConst insConst = (InsConst) reference;
        int id = reference.get();
        RField rField = map.get(id);
        if(rField == null){
            return;
        }
        MethodDef methodDef = insConst.getMethodDef();
        if(methodDef == null){
            return;
        }
        if(rField.getClassName().equals(methodDef.getClassName())){
            return;
        }
        FieldId fieldId = rField.getOrCreate(dexFile);
        if((fieldId.getIndex() & 0xffff0000) != 0){
            return;
        }
        Ins21c ins = Opcode.SGET.newInstance();
        ins.setRegister(0, insConst.getRegister(0));
        insConst.replace(ins);
        ins.setSectionItem(fieldId);
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
