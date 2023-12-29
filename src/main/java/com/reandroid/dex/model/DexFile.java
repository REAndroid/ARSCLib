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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.sections.MergeOptions;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.SourceFile;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.*;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.SmaliWriterSetting;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.FileUtil;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class DexFile implements DexClassRepository, Iterable<DexClass>, FullRefresh {

    private final DexLayout dexLayout;
    private DexDirectory dexDirectory;

    public DexFile(DexLayout dexLayout){
        this.dexLayout = dexLayout;
        dexLayout.setTag(this);
    }

    public int getVersion(){
        return getDexLayout().getVersion();
    }
    public void setVersion(int version){
        getDexLayout().setVersion(version);
    }
    public void setClassSourceFileAll(){
        setClassSourceFileAll(SourceFile.SourceFile);
    }
    public void setClassSourceFileAll(String sourceFile){
        Iterator<ClassId> iterator = getItems(SectionType.CLASS_ID);
        while (iterator.hasNext()){
            ClassId classId = iterator.next();
            classId.setSourceFile(sourceFile);
        }
    }
    public void shrink(){
        getDexLayout().getSectionList().shrink();
    }
    public void clearDuplicateData(){
        getDexLayout().getSectionList().clearDuplicateData();
    }
    public void clearDebug(){
        Section<DebugInfo> debugInfoSection = getSection(SectionType.DEBUG_INFO);
        if(debugInfoSection != null){
            debugInfoSection.removeSelf();
        }
        Section<CodeItem> section = getSection(SectionType.CODE);
        if(section == null){
            return;
        }
        section.clearPool();
        section.refresh();
    }
    public void fixDebugLineNumbers(){
        Section<CodeItem> section = getSection(SectionType.CODE);
        if(section == null){
            return;
        }
        for(CodeItem codeItem : section){
            DebugInfo debugInfo = codeItem.getDebugInfo();
            if(debugInfo == null){
                continue;
            }
            debugInfo.getDebugSequence().fixDebugLineNumbers();
        }
    }
    public boolean cleanInvalidDebugLineNumbers(){
        Section<CodeItem> section = getSection(SectionType.CODE);
        if(section == null){
            return false;
        }
        boolean result = false;
        for(CodeItem codeItem : section){
            if(codeItem.cleanInvalidDebugLineNumbers()){
                result = true;
            }
        }
        return result;
    }
    public void clearUnused(){
        getDexLayout().getSectionList().clearUnused();
    }


    public DexClassRepository getClassRepository(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory;
        }
        return this;
    }
    public DexDirectory getDexDirectory() {
        return dexDirectory;
    }
    public void setDexDirectory(DexDirectory dexDirectory) {
        this.dexDirectory = dexDirectory;
        DexLayout dexLayout = getDexLayout();
        dexLayout.setTag(this);
        dexLayout.setSimpleName(getSimpleName());
    }

    public Iterator<DexClass> getSubTypes(TypeKey typeKey){
        return ComputeIterator.of(getSubTypeIds(typeKey), this::create);
    }
    public Iterator<DexClass> getExtendingClasses(TypeKey typeKey){
        return ComputeIterator.of(getExtendingClassIds(typeKey), this::create);
    }
    public Iterator<DexClass> getImplementClasses(TypeKey typeKey){
        return ComputeIterator.of(getImplementationIds(typeKey), this::create);
    }
    public Iterator<ClassId> getSubTypeIds(TypeKey superClass){
        return getDexLayout().getSubTypes(superClass);
    }
    public Iterator<ClassId> getExtendingClassIds(TypeKey superClass){
        return getDexLayout().getExtendingClassIds(superClass);
    }
    public Iterator<ClassId> getImplementationIds(TypeKey interfaceClass){
        return getDexLayout().getImplementationIds(interfaceClass);
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
    public DexSource<DexFile> getSource(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.getDexSourceSet().getSource(this);
        }
        return null;
    }
    public String getSimpleName() {
        return getDexLayout().getSimpleName();
    }
    public void setSimpleName(String simpleName){
        getDexLayout().setSimpleName(simpleName);
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

    @Override
    public Iterator<DexClass> iterator() {
        return getDexClasses();
    }

    public boolean removeDexClass(TypeKey typeKey){
        Section<ClassId> section = getSection(SectionType.CLASS_ID);
        if(section != null){
            return section.remove(typeKey);
        }
        return false;
    }
    public Iterator<Key> removeDexClasses(Predicate<? super Key> filter){
        Section<ClassId> section = getSection(SectionType.CLASS_ID);
        if(section != null){
            return section.removeAll(filter);
        }
        return EmptyIterator.of();
    }
    public<T1 extends SectionItem> Iterator<T1> getClonedItems(SectionType<T1> sectionType) {
        return getDexLayout().getClonedItems(sectionType);
    }
    @Override
    public int getDexClassesCount() {
        Section<ClassId> section = getSection(SectionType.CLASS_ID);
        if(section != null){
            return section.getCount();
        }
        return 0;
    }
    public DexClass getDexClass(String typeName){
        return getDexClass(TypeKey.create(typeName));
    }
    @Override
    public DexClass getDexClass(TypeKey key){
        ClassId classId = getItem(SectionType.CLASS_ID, key);
        if(classId == null) {
            return null;
        }
        return create(classId);
    }
    @Override
    public Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter) {
        return ComputeIterator.of(getClassIds(filter), this::create);
    }
    @Override
    public<T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType) {
        return getDexLayout().getItems(sectionType);
    }
    @Override
    public <T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType, Key key){
        return getDexLayout().getAll(sectionType, key);
    }
    @Override
    public <T1 extends SectionItem> T1 getItem(SectionType<T1> sectionType, Key key){
        return getDexLayout().get(sectionType, key);
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
    public DexClass search(TypeKey typeKey){
        return getClassRepository().getDexClass(typeKey);
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
        name = DexUtils.toBinaryName(name + ".R");
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
        Section<ClassId> section = getDexLayout().get(SectionType.CLASS_ID);
        ClassId classId = section.getPool().get(key);
        if(classId == null) {
            return null;
        }
        return createRClass(classId);
    }
    public boolean containsClass(TypeKey key){
        return contains(SectionType.CLASS_ID, key);
    }
    public boolean contains(SectionType<?> sectionType, Key key){
        Section<?> section = getSection(sectionType);
        if(section != null){
            return section.contains(key);
        }
        return false;
    }
    public boolean contains(Key key){
        return getDexLayout().getSectionList().contains(key);
    }

    public DexDeclaration getDef(Key key){
        if(key instanceof TypeKey){
            return getDexClass((TypeKey) key);
        }
        if(key instanceof MethodKey){
            return getDeclaredMethod((MethodKey) key);
        }
        if(key instanceof FieldKey){
            return getDeclaredField((FieldKey) key);
        }
        return null;
    }
    public ClassId getOrCreateClassId(TypeKey key){
        Section<ClassId> section = getDexLayout().get(SectionType.CLASS_ID);
        DexSectionPool<ClassId> pool = section.getPool();
        ClassId classId = pool.get(key);
        if(classId != null) {
            return classId;
        }
        classId = pool.getOrCreate(key);
        classId.getOrCreateClassData();
        classId.setSuperClass("Ljava/lang/Object;");
        classId.setSourceFile(DexUtils.toSourceFileName(key.getTypeName()));
        classId.addAccessFlag(AccessFlag.PUBLIC);
        return classId;
    }
    public ClassId getClassId(Key key){
        return getItem(SectionType.CLASS_ID, key);
    }
    private DexClass create(ClassId classId) {
        return new DexClass(this, classId);
    }
    public Marker getOrCreateMarker() {
        Marker marker = CollectionUtil.getFirst(getMarkers().iterator());
        if(marker != null){
            return marker;
        }
        marker = Marker.createR8();
        Section<StringId> stringSection = getSection(SectionType.STRING_ID);

        StringId stringId = stringSection.createItem();
        marker.setStringId(stringId);

        marker.save();

        return marker;
    }
    public void addMarker(Marker marker) {
        StringId stringId = marker.getStringId();
        if(stringId == null){
            Section<StringId> stringSection = getSection(SectionType.STRING_ID);
            stringId = stringSection.createItem();
            marker.setStringId(stringId);
        }
        marker.save();
    }
    public List<Marker> getMarkers() {
        return CollectionUtil.toList(getDexLayout().getMarkers());
    }
    public void clearMarkers(){
        List<Marker> markerList = getMarkers();
        for(Marker marker : markerList){
            marker.removeSelf();
        }
    }
    @Override
    public void refreshFull() throws DexException {
        getDexLayout().refreshFull();
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getDexLayout().sortSection(order);
        refresh();
    }
    public void clearPools(){
        getDexLayout().clearPools();
    }
    public void sortStrings(){
        getDexLayout().sortStrings();
    }
    public Iterator<StringId> unusedStrings(){
        return unused(SectionType.STRING_ID);
    }
    public<T1 extends SectionItem> Iterator<T1> unused(SectionType<T1> sectionType){
        return getWithUsage(sectionType, UsageMarker.USAGE_NONE);
    }
    public<T1 extends SectionItem> Iterator<T1> getWithUsage(SectionType<T1> sectionType, int usage){
        return FilterIterator.of(getSection(sectionType).iterator(),
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
    public Iterator<ClassId> getClassIds(Predicate<? super TypeKey> filter){
        return FilterIterator.of(getItems(SectionType.CLASS_ID),
                classId -> filter == null || filter.test(classId.getKey()));
    }
    public Iterator<StringId> getStringIds(){
        return getItems(SectionType.STRING_ID);
    }
    public Iterator<StringData> getStringData(){
        return getItems(SectionType.STRING_DATA);
    }
    public Iterator<TypeId> getTypes(){
        return getItems(SectionType.TYPE_ID);
    }
    public <T1 extends SectionItem> Section<T1> getSection(SectionType<T1> sectionType){
        return getDexLayout().get(sectionType);
    }
    public void refresh() {
        getDexLayout().refresh();
    }
    public DexLayout getDexLayout() {
        return dexLayout;
    }

    public boolean isEmpty(){
        return getDexLayout().isEmpty();
    }
    public boolean merge(DexClass dexClass){
        return merge(new DexMergeOptions(true), dexClass);
    }
    public boolean merge(MergeOptions options, DexClass dexClass){
        return this.merge(options, dexClass.getId());
    }
    public boolean merge(ClassId classId){
        return merge(new DexMergeOptions(true), classId);
    }
    public boolean merge(MergeOptions options, ClassId classId){
        return getDexLayout().merge(options, classId);
    }
    public boolean merge(MergeOptions options, DexFile dexFile){
        if(dexFile == null || dexFile.isEmpty()){
            return false;
        }
        return getDexLayout().merge(options, dexFile.getDexLayout());
    }

    public byte[] getBytes() {
        if(isEmpty()){
            return new byte[0];
        }
        return getDexLayout().getBytes();
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

    public String printSectionInfo(){
        return getDexLayout().getMapList().toString();
    }
    public void writeSmali(SmaliWriter writer, File root) throws IOException {
        File dir = new File(root, buildSmaliDirectoryName());
        for(DexClass dexClass : this){
            dexClass.writeSmali(writer, dir);
        }
    }
    public String buildSmaliDirectoryName(){
        int i = 0;
        DexDirectory dexDirectory = getDexDirectory();
        if(dexDirectory != null){
            for(DexFile dexFile : dexDirectory){
                if(dexFile == this){
                    break;
                }
                i++;
            }
        }
        if(i == 0){
            return "classes";
        }
        i++;
        return "classes" + i;
    }
    public String getFileName(){
        String simpleName = getSimpleName();
        if(simpleName == null){
            return buildSmaliDirectoryName() + ".dex";
        }
        return FileUtil.getFileName(simpleName);
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getSimpleName());
        builder.append(", version = ");
        builder.append(getVersion());
        builder.append(", classes = ");
        builder.append(getDexClassesCount());
        List<Marker> markers = getMarkers();
        int size = markers.size();
        if(size != 0){
            builder.append(", markers = ");
            builder.append(size);
            if(size > 10){
                size = 10;
            }
            for(int i = 0; i < size; i++){
                builder.append('\n');
                builder.append(markers.get(i));
            }
        }
        return builder.toString();
    }

    public static boolean replaceRFields(RField rField, SizeXIns insConst){
        if(insConst == null || rField == null){
            return false;
        }
        MethodDef methodDef = insConst.getMethodDef();
        if(methodDef == null){
            return false;
        }
        if(rField.getDefining().equals(methodDef.getKey().getDeclaring())){
            return false;
        }
        Ins21c ins = Opcode.SGET.newInstance();
        ins.setRegister(0, ((RegistersSet)insConst).getRegister(0));
        insConst.replace(ins);
        ins.setSectionIdKey(rField.getKey());
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
        DexLayout dexLayout = new DexLayout();
        dexLayout.readBytes(reader);
        reader.close();
        return new DexFile(dexLayout);
    }
    public static DexFile readStrings(BlockReader reader) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readStrings(reader);
        reader.close();
        return new DexFile(dexLayout);
    }
    public static DexFile readStrings(InputStream inputStream) throws IOException {
        return readStrings(new BlockReader(inputStream));
    }

    public static DexFile createDefault(){
        return new DexFile(DexLayout.createDefault());
    }

    public static int getDexFileNumber(String name){
        int i = name.lastIndexOf('/');
        if(i < 0){
            i = name.lastIndexOf('\\');
        }
        if(i >= 0){
            name = name.substring(i + 1);
        }
        if(name.equals("classes.dex")){
            return 0;
        }
        String prefix = "classes";
        String ext = ".dex";
        if(!name.startsWith(prefix) || !name.endsWith(ext)){
            return -1;
        }
        String num = name.substring(prefix.length(), name.length() - ext.length());
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException ignored){
            return -1;
        }
    }

    public static String getDexName(int i) {
        if (i == 0) {
            return "classes.dex";
        }
        if(i == 1){
            i = 2;
        }
        return "classes" + i + ".dex";
    }
}
