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

import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.dex.common.FullRefresh;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.id.*;
import com.reandroid.dex.sections.Marker;
import com.reandroid.dex.sections.MergeOptions;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class DexDirectory implements Iterable<DexFile>, DexClassRepository, FullRefresh {

    private final DexFileSourceSet dexSourceSet;
    private Object mTag;

    public DexDirectory() {
        this.dexSourceSet = new DexFileSourceSet();
    }

    public Object getTag() {
        return mTag;
    }
    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public int getVersion(){
        DexFile first = getFirst();
        if(first != null){
            return first.getVersion();
        }
        return 0;
    }
    public void setVersion(int version){
        for(DexFile dexFile : this){
            dexFile.setVersion(version);
        }
    }
    public void clearMarkers(){
        for(DexFile dexFile : this){
            dexFile.clearMarkers();
        }
    }
    public List<Marker> getMarkers() {
        ArrayCollection<Marker> results = new ArrayCollection<>();
        for(DexFile dexFile : this){
            results.addAll(dexFile.getMarkers());
        }
        return results;
    }
    public void setClassSourceFileAll(){
        setClassSourceFileAll(SourceFile.SourceFile);
    }
    public void setClassSourceFileAll(String sourceFile){
        Iterator<DexFile> iterator = iterator();
        while (iterator.hasNext()){
            DexFile dexFile = iterator.next();
            dexFile.setClassSourceFileAll(sourceFile);
        }
    }
    public int mergeAll(MergeOptions options, Iterable<DexClass> iterable){
        return mergeAll(options, iterable.iterator());
    }
    public int mergeAll(MergeOptions options, Iterator<DexClass> iterator){
        int result = 0;
        while (iterator.hasNext()){
            boolean merged = merge(options, iterator.next());
            if(merged){
                result ++;
            }
        }
        return result;
    }
    public boolean merge(DexClass dexClass){
        return merge(new DexMergeOptions(true), dexClass);
    }
    public boolean merge(MergeOptions options, DexClass dexClass){
        if(dexClass.isInSameDirectory(this)){
            return false;
        }
        if(containsClass(dexClass.getKey())){
            options.onDuplicate(dexClass.getId());
            return false;
        }
        for(DexFile dexFile : this){
            if(dexFile.merge(options, dexClass)){
                return true;
            }
        }
        return false;
    }
    public void merge(DexDirectory directory){
        merge(new DexMergeOptions(false), directory);
    }
    public void merge(MergeOptions options, DexDirectory directory){
        if(directory == this){
            throw new IllegalArgumentException("Cyclic merge");
        }
        int i = 0;
        while (true){
            DexFile dexFile = this.get(i);
            DexFile last = directory.getLastNonEmpty(options,0);
            if(dexFile == null || last == null){
                break;
            }
            if(!dexFile.merge(options, last)){
                i ++;
            }
        }
        shrink();
        directory.merge(options);
        getDexSourceSet().merge(directory.getDexSourceSet());
    }
    public void merge(){
        merge(new DexMergeOptions(true));
    }
    public void merge(MergeOptions options){
        if(size() < 2){
            return;
        }
        int i = 0;
        while (true){
            DexFile dexFile = get(i);
            DexFile last = getLastNonEmpty(options,i + 1);
            if(dexFile == null || last == null){
                break;
            }
            if(!dexFile.merge(options, last)){
                i ++;
            }
        }
        shrink();
    }
    private DexFile getLastNonEmpty(MergeOptions options, int limit){
        int size = size() - 1;
        for(int i = size; i >= limit; i--){
            DexFile dexFile = get(i);
            if(!options.isEmptyDexFile(dexFile.getDexLayout())){
                return dexFile;
            }
        }
        return null;
    }
    public int shrink(){
        int result = 0;
        for(DexFile dexFile : this){
            result += dexFile.shrink();
        }
        return result;
    }
    public int clearDuplicateData(){
        int result = 0;
        for(DexFile dexFile : this){
            result += dexFile.clearDuplicateData();
        }
        return result;
    }
    public int clearUnused(){
        int result = 0;
        for(DexFile dexFile : this){
            result += dexFile.clearUnused();
        }
        return result;
    }
    public void clearDebug(){
        for(DexFile dexFile : this){
            dexFile.clearDebug();
        }
    }

    public void cleanDuplicateDebugLines(){
        for(DexFile dexFile : this){
            dexFile.fixDebugLineNumbers();
        }
    }
    public Iterator<FieldKey> findEquivalentFields(FieldKey fieldKey){
        DexClass defining = getDexClass(fieldKey.getDeclaring());
        if(defining == null){
            return EmptyIterator.of();
        }
        DexField dexField = defining.getField(fieldKey);
        if(dexField == null){
            return EmptyIterator.of();
        }
        defining = dexField.getDexClass();

        FieldKey definingKey = dexField.getKey();

        Iterator<FieldKey> subKeys = ComputeIterator.of(getSubTypes(defining.getKey()),
                dexClass -> {
                    FieldKey key = definingKey.changeDeclaring(dexClass.getKey());
                    DexField field = dexClass.getField(key);
                    if(definingKey.equals(field.getKey())){
                        return key;
                    }
                    return null;
                }
        );
        return CombiningIterator.two(SingleIterator.of(definingKey), subKeys);
    }
    public Iterator<MethodKey> findEquivalentMethods(MethodKey methodKey){
        DexClass defining = getDexClass(methodKey.getDeclaring());
        if(defining == null){
            return EmptyIterator.of();
        }
        Iterator<DexMethod> iterator = defining.getMethods(methodKey);

        return new IterableIterator<DexMethod, MethodKey>(iterator) {
            @Override
            public Iterator<MethodKey> iterator(DexMethod element) {
                element = element.getDeclared();
                MethodKey definingKey = element.getKey();
                return CombiningIterator.two(SingleIterator.of(definingKey), element.getOverridingKeys());
            }
        };
    }
    public Iterator<DexClass> getSubTypes(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getSubTypes(typeKey);
            }
        };
    }
    public Iterator<DexClass> getImplementClasses(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getImplementClasses(typeKey);
            }
        };
    }
    public void save(File dir) throws IOException {
        dexSourceSet.saveAll(dir);
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
    public Iterator<ClassId> getClassIds() {
        return getItems(SectionType.CLASS_ID);
    }
    public <T1 extends SectionItem> T1 get(SectionType<T1> sectionType, Key key){
        for (DexFile dexFile : this) {
            T1 item = dexFile.getItem(sectionType, key);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    public <T1 extends SectionItem> Iterator<T1> getAll(SectionType<T1> sectionType, Key key){
        return new IterableIterator<DexFile, T1>(iterator()) {
            @Override
            public Iterator<T1> iterator(DexFile element) {
                return element.getItems(sectionType, key);
            }
        };
    }
    public Iterator<RClass> getRClasses() {
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                DexFile::getRClasses));
    }
    public boolean removeDexClass(TypeKey typeKey){
        for(DexFile dexFile : this){
            if(dexFile.removeDexClass(typeKey)){
                return true;
            }
        }
        return false;
    }
    public Iterator<Key> removeDexClasses(Predicate<? super Key> filter){
        return new IterableIterator<DexFile, Key>(iterator()) {
            @Override
            public Iterator<Key> iterator(DexFile element) {
                return element.removeDexClasses(filter);
            }
        };
    }
    public<T1 extends SectionItem> Iterator<T1> getClonedItems(SectionType<T1> sectionType) {
        return new IterableIterator<DexFile, T1>(clonedIterator()) {
            @Override
            public Iterator<T1> iterator(DexFile element) {
                return element.getClonedItems(sectionType);
            }
        };
    }
    @Override
    public int getDexClassesCount() {
        int result = 0;
        for(DexFile dexFile : this){
            result += dexFile.getDexClassesCount();
        }
        return result;
    }
    public DexClass getDexClass(String name) {
        return getDexClass(TypeKey.create(name));
    }
    @Override
    public DexClass getDexClass(TypeKey key){
        for(DexFile dexFile : this){
            DexClass result = dexFile.getDexClass(key);
            if(result != null){
                return result;
            }
        }
        return null;
    }
    @Override
    public Iterator<DexClass> getDexClasses(Predicate<? super TypeKey> filter) {
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile dexFile) {
                return dexFile.getDexClasses(filter);
            }
        };
    }
    @Override
    public<T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType) {
        return new IterableIterator<DexFile, T1>(iterator()) {
            @Override
            public Iterator<T1> iterator(DexFile element) {
                return element.getItems(sectionType);
            }
        };
    }
    @Override
    public <T1 extends SectionItem> Iterator<T1> getItems(SectionType<T1> sectionType, Key key){
        return new IterableIterator<DexFile, T1>(iterator()) {
            @Override
            public Iterator<T1> iterator(DexFile dexFile) {
                return dexFile.getItems(sectionType, key);
            }
        };
    }
    @Override
    public <T1 extends SectionItem> T1 getItem(SectionType<T1> sectionType, Key key){
        for(DexFile dexFile : this){
            T1 item = dexFile.getItem(sectionType, key);
            if(item != null){
                return item;
            }
        }
        return null;
    }
    public Iterator<Ins> getInstructions() {
        return new IterableIterator<DexFile, Ins>(iterator()) {
            @Override
            public Iterator<Ins> iterator(DexFile element) {
                return element.getInstructions();
            }
        };
    }
    @Override
    public Iterator<DexFile> iterator() {
        return dexSourceSet.getDexFiles();
    }
    public Iterator<DexFile> clonedIterator() {
        return dexSourceSet.getClonedDexFiles();
    }

    public void clearPools(){
        for(DexFile dexFile : this){
            dexFile.clearPools();
        }
    }
    public void sortStrings(){
        for(DexFile dexFile : this){
            dexFile.sortStrings();
        }
    }
    @Override
    public void refreshFull(){
        for(DexFile dexFile : this){
            dexFile.setDexDirectory(this);
            dexFile.refreshFull();
        }
    }
    public void refresh(){
        for(DexFile dexFile : this){
            dexFile.setDexDirectory(this);
            dexFile.refresh();
        }
    }
    public void updateDexFileList(){
        for(DexFile dexFile : this){
            dexFile.setDexDirectory(this);
        }
    }
    public void addDirectory(File dir) throws IOException {
        getDexSourceSet().addAll(dir);
        for(DexFile dexFile : this){
            dexFile.setDexDirectory(this);
        }
    }
    public void addApk(ZipEntryMap zipEntryMap) throws IOException {
        addZip(zipEntryMap, "");
    }
    public void addZip(ZipEntryMap zipEntryMap, String root) throws IOException {
        getDexSourceSet().addAll(zipEntryMap, root);
        for(DexFile dexFile : this){
            dexFile.setDexDirectory(this);
        }
    }
    public void addFile(File file) throws IOException {
        DexSource<DexFile> source = getDexSourceSet().add(file);
        if(file.isFile()){
            source.get().setDexDirectory(this);
        }
    }
    public DexFile createDefault(){
        DexSource<DexFile> source = getDexSourceSet().createNext();
        DexFile dexFile = DexFile.createDefault();
        source.set(dexFile);
        dexFile.setDexDirectory(this);
        dexFile.setSimpleName(source.toString());
        int version = getVersion();
        if(version != 0){
            dexFile.setVersion(version);
        }
        return dexFile;
    }
    public DexFileSourceSet getDexSourceSet() {
        return dexSourceSet;
    }

    public int rename(TypeKey search, TypeKey replace){
        if(containsClass(replace)){
            throw new RuntimeException("Duplicate: " + search + " --> " + replace);
        }
        int count = 0;
        Iterator<?> iterator = renameTypes(search, replace, true, true);
        while (iterator.hasNext()){
            iterator.next();
            count++;
        }
        return count;
    }

    public Iterator<StringId> renameTypes(TypeKey search, TypeKey replace){
        return renameTypes(search, replace, true, true);
    }
    public Iterator<StringId> renameTypes(TypeKey search, TypeKey replace, boolean renameInner, boolean renameJava){
        return renameTypes(new KeyPair<>(search, replace), renameInner, renameJava);
    }
    public Iterator<StringId> renameTypes(KeyPair<TypeKey, TypeKey> pair, boolean renameInner, boolean renameJava){
        return FilterIterator.of(getClonedItems(SectionType.STRING_ID),
                stringId -> renameTypes(stringId, pair, renameInner, renameJava));
    }
    public Iterator<StringId> renameTypes(Iterable<KeyPair<TypeKey, TypeKey>> iterable, boolean renameInner, boolean renameJava){
        return FilterIterator.of(getClonedItems(SectionType.STRING_ID),
                stringId -> renameTypes(stringId, iterable, renameInner, renameJava));
    }
    boolean renameTypes(StringId stringId, Iterable<KeyPair<TypeKey, TypeKey>> iterable, boolean renameInner, boolean renameJava){
        for(KeyPair<TypeKey, TypeKey> pair : iterable){
            boolean renamed = renameTypes(stringId, pair, renameInner, renameJava);
            if(renamed){
                return true;
            }
        }
        return false;
    }
    boolean renameTypes(StringId stringId, KeyPair<TypeKey, TypeKey> pair, boolean renameInner, boolean renameJava){
        boolean renamed = renameTypeString(stringId, pair, renameInner, renameJava);
        if(renamed){
            DexClass dexClass = getDexClass(stringId.getString());
            if(dexClass != null){
                dexClass.fixDalvikInnerClassName();
            }
        }
        return renamed;
    }
    private boolean renameTypeString(StringId stringId, KeyPair<TypeKey, TypeKey> pair, boolean renameInner, boolean renameJava){

        String text = stringId.getString();

        TypeKey search = pair.getFirst();
        TypeKey replace = pair.getSecond();
        String type = search.getTypeName();
        String type2 = replace.getTypeName();

        if(type.equals(text)){
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getTypeName();
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getSignatureTypeName();
        if(type.equals(text)){
            type2 = replace.getSignatureTypeName();
            stringId.setString(type2);
            return true;
        }
        type = search.getArrayType(1);
        if(type.equals(text)){
            type2 = replace.getArrayType(1);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(1);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getArrayType(2);
        if(type.equals(text)){
            type2 = replace.getArrayType(2);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(2);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getArrayType(3);
        if(type.equals(text)){
            type2 = replace.getArrayType(3);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(3);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        if(renameJava){
            type = search.getSourceName();
            if(type.equals(text)){
                type2 = replace.getSourceName();
                stringId.setString(type2);
                return true;
            }
            if(renameInner){
                type = type + "$";
                if(text.startsWith(type)){
                    type2 = replace.getSourceName();
                    type2 = type2 + "$";
                    text = text.substring(type.length());
                    stringId.setString(type2 + text);
                    return true;
                }
                type = type + ".";
                if(text.startsWith(type)){
                    type2 = replace.getSourceName();
                    type2 = type2 + ".";
                    text = text.substring(type.length());
                    stringId.setString(type2 + text);
                    return true;
                }
            }
        }
        return false;
    }
    public List<MethodKey> rename(MethodKey methodKey, String name){
        if(containsDeepSearch(methodKey.changeName(name))){
            return EmptyList.of();
        }
        ArrayCollection<MethodId> methodIdList = new ArrayCollection<>();
        methodIdList.addAll(getMethods(methodKey));
        if(methodIdList.size() == 0){
            return EmptyList.of();
        }
        MethodKey renamed = methodKey.changeName(name);
        for(MethodId methodId : methodIdList){
            if(renamed.equals(methodId.getKey())){
                throw new IllegalArgumentException("Duplicate: " + renamed);
            }
        }
        for(MethodId methodId : methodIdList){
            methodId.setName(name);
        }
        return new ComputeList<>(methodIdList, MethodId::getKey);
    }
    public Collection<FieldKey> rename(FieldKey fieldKey, String name){
        ArrayCollection<FieldKey> existingFields = ArrayCollection.of(findEquivalentFields(fieldKey.changeName(name)));
        ArrayCollection<FieldId> fieldIdList = ArrayCollection.of(getFields(fieldKey));
        if(fieldIdList.isEmpty()){
            return EmptyList.of();
        }
        if(!existingFields.isEmpty()){
            throw new IllegalArgumentException("Conflicting fields: " + existingFields.getFirst());
        }
        FieldKey renamed = fieldKey.changeName(name);
        for(FieldId fieldId : fieldIdList){
            if(renamed.equals(fieldId.getKey())){
                throw new IllegalArgumentException("Duplicate: " + renamed);
            }
        }
        for(FieldId fieldId : fieldIdList){
            fieldId.setName(name);
        }
        return new ComputeList<>(fieldIdList, FieldId::getKey);
    }
    public boolean containsDeepSearch(MethodKey methodKey){
        DexClass startClass = getDexClass(methodKey.getDeclaring());
        if(startClass == null){
            return false;
        }
        if(startClass.containsDeclaredMethod(methodKey)){
            return true;
        }
        Iterator<DexClass> iterator = startClass.getOverridingAndSuperTypes();
        while (iterator.hasNext()){
            DexClass dexClass = iterator.next();
            if(dexClass.containsDeclaredMethod(methodKey)){
                return true;
            }
        }
        return false;
    }
    public boolean containsDeepSearch(FieldKey fieldKey){
        DexClass startClass = getDexClass(fieldKey.getDeclaring());
        if(startClass == null){
            return false;
        }
        Iterator<DexClass> iterator = startClass.getOverridingAndSuperTypes();
        while (iterator.hasNext()){
            DexClass dexClass = iterator.next();
            FieldKey key = fieldKey.changeDeclaring(dexClass.getKey());
            if(fieldKey.equals(key)){
                return true;
            }
        }
        return false;
    }
    public Iterator<FieldId> getFields(FieldKey fieldKey){
        return new IterableIterator<FieldKey, FieldId>(findEquivalentFields(fieldKey)) {
            @Override
            public Iterator<FieldId> iterator(FieldKey element) {
                return getAll(SectionType.FIELD_ID, element);
            }
        };
    }
    public Iterator<MethodId> getMethods(MethodKey methodKey){
        return new IterableIterator<MethodKey, MethodId>(findEquivalentMethods(methodKey)) {
            @Override
            public Iterator<MethodId> iterator(MethodKey element) {
                return getAll(SectionType.METHOD_ID, element);
            }
        };
    }
    public Iterator<DexClass> searchExtending(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getExtendingClasses(typeKey);
            }
        };
    }
    public Iterator<DexClass> searchImplementations(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getImplementClasses(typeKey);
            }
        };
    }
    public boolean containsClass(TypeKey key){
        return contains(SectionType.CLASS_ID, key);
    }
    public boolean contains(SectionType<?> sectionType, Key key){
        for(DexFile dexFile : this){
            if(dexFile.contains(sectionType, key)){
                return true;
            }
        }
        return false;
    }
    public boolean contains(Key key){
        for(DexFile dexFile : this){
            if(dexFile.contains(key)){
                return true;
            }
        }
        return false;
    }
    public DexDeclaration getDef(Key key){
        for(DexFile dexFile : this){
            DexDeclaration result = dexFile.getDef(key);
            if(result != null){
                return result;
            }
        }
        return null;
    }
    public DexField getField(FieldKey fieldKey){
        for(DexFile dexFile : this){
            DexField result = dexFile.getDeclaredField(fieldKey);
            if(result != null){
                return result;
            }
        }
        return null;
    }
    public DexFile get(int i){
        return dexSourceSet.getDexFile(i);
    }
    public int size() {
        return dexSourceSet.size();
    }
    public DexFile getFirst(){
        DexSource<DexFile> source = dexSourceSet.getFirst();
        if(source != null){
            return source.get();
        }
        return null;
    }
    public DexFile getLast(){
        DexSource<DexFile> source = dexSourceSet.getLast();
        if(source != null){
            return source.get();
        }
        return null;
    }
    public DexSource<DexFile> getLastSource(){
        return dexSourceSet.getLast();
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


    public void writeSmali(SmaliWriter writer, File root) throws IOException {
        for(DexFile dexFile : this){
            dexFile.writeSmali(writer, root);
        }
    }
    public static DexDirectory fromZip(ZipEntryMap zipEntryMap) throws IOException {
        DexDirectory dexDirectory = new DexDirectory();
        dexDirectory.getDexSourceSet().addAll(zipEntryMap);
        dexDirectory.updateDexFileList();
        return dexDirectory;
    }
    public static DexDirectory fromZip(ZipEntryMap zipEntryMap, String directoryPath) throws IOException {
        DexDirectory dexDirectory = new DexDirectory();
        dexDirectory.getDexSourceSet().addAll(zipEntryMap, directoryPath);
        dexDirectory.updateDexFileList();
        return dexDirectory;
    }
    public static DexDirectory fromDirectory(File dir) throws IOException {
        DexDirectory dexDirectory = new DexDirectory();
        dexDirectory.getDexSourceSet().addAll(dir);
        dexDirectory.updateDexFileList();
        return dexDirectory;
    }
    public static DexDirectory readStrings(ZipEntryMap zipEntryMap) throws IOException {
        return readStrings(zipEntryMap, null);
    }
    public static DexDirectory readStrings(ZipEntryMap zipEntryMap, String directoryPath) throws IOException {
        DexDirectory dexDirectory = new DexDirectory();
        DexFileSourceSet sourceSet = dexDirectory.getDexSourceSet();
        sourceSet.setReadStringsMode(true);
        sourceSet.addAll(zipEntryMap, directoryPath);
        dexDirectory.updateDexFileList();
        return dexDirectory;
    }
}
