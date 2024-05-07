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
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.*;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.*;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.FileIterator;
import com.reandroid.utils.io.FileUtil;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class DexFile implements DexClassRepository, Closeable,
        Iterable<DexClass>, FullRefresh {

    private final DexLayout dexLayout;
    private DexDirectory dexDirectory;
    private boolean closed;

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
    public int shrink(){
        return getDexLayout().getSectionList().shrink();
    }
    public int clearDuplicateData(){
        return getDexLayout().getSectionList().clearDuplicateData();
    }
    public int clearUnused(){
        return getDexLayout().getSectionList().clearUnused();
    }
    public int clearEmptySections(){
        return getDexLayout().getSectionList().clearEmptySections();
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
        section.clearPoolMap();
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
    public Iterator<Key> removeClassesWithKeys(Predicate<Key> filter){
        return getDexLayout().removeWithKeys(SectionType.CLASS_ID, filter);
    }
    public int removeClasses(Predicate<DexClass> filter){
        Predicate<ClassId> classIdFilter = classId -> filter.test(DexFile.this.create(classId));
        return getDexLayout().removeEntries(SectionType.CLASS_ID, classIdFilter);
    }
    @Override
    public <T1 extends SectionItem> int removeEntries(SectionType<T1> sectionType, Predicate<T1> filter){
        return getDexLayout().removeEntries(sectionType, filter);
    }
    public <T1 extends SectionItem> Iterator<Key> removeWithKeys(SectionType<T1> sectionType, Predicate<Key> filter){
        return getDexLayout().removeWithKeys(sectionType, filter);
    }
    @Override
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
    public Iterator<DexClass> getDexClassesCloned(Predicate<? super TypeKey> filter) {
        return ComputeIterator.of(getClassIdsCloned(filter), this::create);
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

    public ClassId getOrCreateClassId(TypeKey key){
        Section<ClassId> section = getDexLayout().get(SectionType.CLASS_ID);
        DexSectionPool<ClassId> pool = section.getPool();
        ClassId classId = pool.get(key);
        if(classId != null) {
            return classId;
        }
        classId = pool.getOrCreate(key);
        classId.getOrCreateClassData();
        classId.setSuperClass(TypeKey.OBJECT);
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
    public void clearPoolMap(SectionType<?> sectionType){
        getDexLayout().clearPoolMap(sectionType);
    }
    @Override
    public void clearPoolMap(){
        getDexLayout().clearPoolMap();
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

    public Iterator<DexInstruction> getDexInstructions(){
        return new IterableIterator<DexClass, DexInstruction>(getDexClasses()){
            @Override
            public Iterator<DexInstruction> iterator(DexClass element) {
                return element.getDexInstructions();
            }
        };
    }
    public Iterator<DexInstruction> getDexInstructionsCloned(){
        return new IterableIterator<DexClass, DexInstruction>(getDexClassesCloned()){
            @Override
            public Iterator<DexInstruction> iterator(DexClass element) {
                return element.getDexInstructions();
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
    public Iterator<ClassId> getClassIdsCloned(Predicate<? super TypeKey> filter){
        return FilterIterator.of(getClonedItems(SectionType.CLASS_ID),
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
    public int getIndex(){
        DexDirectory directory = getDexDirectory();
        if(directory != null){
            return directory.indexOf(this);
        }
        return -1;
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
    public void parseSmaliDirectory(File dir) throws IOException {
        requireNotClosed();
        if(!dir.isDirectory()){
            throw new FileNotFoundException("No such directory: " + dir);
        }
        FileIterator iterator = new FileIterator(dir, FileIterator.getExtensionFilter(".smali"));
        while (iterator.hasNext()){
            parseSmaliFile(iterator.next());
        }
        shrink();
    }
    public void parseSmaliFile(File file) throws IOException {
        requireNotClosed();
        fromSmali(SmaliReader.of(file));
    }
    public void fromSmaliMultipleClasses(SmaliReader reader) throws IOException {
        requireNotClosed();
        while (SmaliDirective.parse(reader, false) == SmaliDirective.CLASS){
            SmaliClass smaliClass = new SmaliClass();
            smaliClass.parse(reader);
            fromSmali(smaliClass);
            reader.skipWhitespacesOrComment();
        }
    }
    public DexClass fromSmali(SmaliReader reader) throws IOException {
        requireNotClosed();
        SmaliClass smaliClass = new SmaliClass();
        smaliClass.parse(reader);
        DexClass dexClass = fromSmali(smaliClass);
        reader.skipWhitespacesOrComment();
        return dexClass;
    }
    public DexClass fromSmali(SmaliClass smaliClass) throws IOException {
        requireNotClosed();
        ClassId classId = getDexLayout().fromSmali(smaliClass);
        return create(classId);
    }

    public byte[] getBytes() {
        if(isClosed()){
            return null;
        }
        if(isEmpty()){
            return new byte[0];
        }
        return getDexLayout().getBytes();
    }
    public void write(File file) throws IOException {
        requireNotClosed();
        OutputStream outputStream = FileUtil.outputStream(file);;
        write(outputStream);
        outputStream.close();
    }
    public void write(OutputStream outputStream) throws IOException {
        requireNotClosed();
        byte[] bytes = getBytes();
        outputStream.write(bytes, 0, bytes.length);
    }

    public String printSectionInfo(){
        return getDexLayout().getMapList().toString();
    }
    public void writeSmali(SmaliWriter writer, File root) throws IOException {
        requireNotClosed();
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

    private void requireNotClosed() throws IOException {
        if(isClosed()){
            throw new IOException("Closed");
        }
    }
    public boolean isClosed() {
        return closed;
    }
    @Override
    public void close() throws IOException {
        if(!closed){
            closed = true;
            getDexLayout().clear();
        }
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
        return new DexFile(dexLayout);
    }
    public static DexFile readClassIds(BlockReader reader) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readClassIds(reader);
        return new DexFile(dexLayout);
    }
    public static DexFile readSections(BlockReader reader, Predicate<SectionType<?>> filter) throws IOException {
        DexLayout dexLayout = new DexLayout();
        dexLayout.readSections(reader, filter);
        return new DexFile(dexLayout);
    }
    public static DexFile readStrings(InputStream inputStream) throws IOException {
        return readStrings(new BlockReader(inputStream));
    }
    public static DexFile readClassIds(InputStream inputStream) throws IOException {
        return readClassIds(new BlockReader(inputStream));
    }

    public static DexFile createDefault(){
        return new DexFile(DexLayout.createDefault());
    }

    public static DexFile findDexFile(ClassId classId){
        if(classId == null){
            return null;
        }
        return DexFile.findDexFile(classId.getParentInstance(DexLayout.class));
    }
    public static DexFile findDexFile(DexLayout dexLayout){
        if(dexLayout == null){
            return null;
        }
        Object obj = dexLayout.getTag();
        if(!(obj instanceof DexFile)){
            return null;
        }
        return  (DexFile) obj;
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
