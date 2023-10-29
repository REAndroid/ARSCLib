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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.BytesOutputStream;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.header.Checksum;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.data.TypeList;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.KeyPool;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.StringValue;
import com.reandroid.utils.collection.*;

import java.io.*;
import java.util.Iterator;

public class DexFileBlock extends FixedBlockContainer {

    private final SectionList sectionList;

    private String mSimpleName;

    private final KeyPool<ClassId> extendingClassMap;
    private final KeyPool<ClassId> interfaceMap;

    public DexFileBlock() {
        super(1);
        this.sectionList = new SectionList();
        addChild(0, sectionList);

        this.extendingClassMap = new KeyPool<>(SectionType.CLASS_ID, 2000);
        this.interfaceMap = new KeyPool<>(SectionType.CLASS_ID, 2000);
    }


    public Iterator<ClassId> getSubTypes(TypeKey typeKey){
        Iterator<ClassId> iterator = CombiningIterator.two(getExtendingClassIds(typeKey),
                getImplementationIds(typeKey));
        return new IterableIterator<ClassId, ClassId>(iterator) {
            @Override
            public Iterator<ClassId> iterator(ClassId element) {
                return CombiningIterator.two(SingleIterator.of(element), getSubTypes(element.getKey()));
            }
        };
    }
    public Iterator<ClassId> getExtendingClassIds(TypeKey typeKey){
        return this.extendingClassMap.getAll(typeKey);
    }
    public Iterator<ClassId> getImplementationIds(TypeKey interfaceClass){
        return this.interfaceMap.getAll(interfaceClass);
    }

    public void clear(){
        extendingClassMap.clear();
        interfaceMap.clear();
        getSectionList().clear();
    }
    public void loadSuperTypesMap(){
        loadExtendingClassMap();
        loadInterfacesMap();
    }
    public void loadExtendingClassMap(){
        KeyPool<ClassId> superClassMap = this.extendingClassMap;
        superClassMap.clear();
        Iterator<ClassId> iterator = getItems(SectionType.CLASS_ID);
        while (iterator.hasNext()){
            ClassId classId = iterator.next();
            String superName = classId.getSuperClassName();
            if(DexUtils.isJavaFramework(superName)){
                continue;
            }
            TypeKey typeKey = TypeKey.create(superName);
            superClassMap.put(typeKey, classId);
        }
        superClassMap.trimToSize();
    }
    public void loadInterfacesMap(){
        KeyPool<ClassId> interfaceMap = this.interfaceMap;
        interfaceMap.clear();
        Iterator<ClassId> iterator = getItems(SectionType.CLASS_ID);
        while (iterator.hasNext()){
            ClassId classId = iterator.next();
            TypeList typeList = classId.getInterfaces();
            if(typeList == null){
                continue;
            }
            Iterator<TypeKey> typeListIterator = typeList.getTypeKeys();
            while (typeListIterator.hasNext()){
                TypeKey typeKey = typeListIterator.next();
                if(DexUtils.isJavaFramework(typeKey.getType())){
                    continue;
                }
                interfaceMap.put(typeKey, classId);
            }
        }
        interfaceMap.trimToSize();
    }
    public Iterator<StringId> getStrings(){
        return getItems(SectionType.STRING_ID);
    }
    public<T1 extends Block> Iterator<T1> getItems(SectionType<T1> sectionType) {
        Section<T1> section = getSectionList().get(sectionType);
        if(section != null){
            return section.iterator();
        }
        return EmptyIterator.of();
    }
    public void refreshFull() throws DexException{
        Checksum checksum = getHeader().checksum;
        int previousSum = checksum.getValue();
        int max_trials = 10;
        int trials;
        for(trials = 0; trials < max_trials; trials++){
            refresh();
            int sum = checksum.getValue();
            if(previousSum == sum){
                return;
            }
            previousSum = sum;
        }
        throw new DexException("Failed to refresh trials = " + trials);
    }
    public void sortSection(SectionType<?>[] order){
        refresh();
        getSectionList().sortSection(order);
        refresh();
    }
    public void clearPools(){
        getSectionList().clearPools();
    }
    public void sortStrings(){
        getSectionList().sortStrings();
    }

    public void linkTypeSignature(){

        Section<AnnotationItem> annotationSection = get(SectionType.ANNOTATION_ITEM);

        if(annotationSection == null){
            return;
        }

        Iterator<AnnotationItem> iterator = annotationSection.iterator(
                item -> TYPE_Signature.equals(item.getTypeKey()) &&
                item.containsName(NAME_value));

        while (iterator.hasNext()){
            AnnotationItem item = iterator.next();
            AnnotationElement element = item.getElement(NAME_value);
            if(element == null){
                continue;
            }
            DexValueBlock<?> dexValue = element.getValue();
            if(!(dexValue instanceof ArrayValue)){
                continue;
            }
            ArrayValue arrayValue = (ArrayValue) dexValue;
            linkTypeSignature(arrayValue);
        }
    }
    private void linkTypeSignature(ArrayValue arrayValue){
        for(DexValueBlock<?> value : arrayValue){
            if(!(value instanceof StringValue)){
                continue;
            }
            StringId stringId = ((StringValue) value).get();
            if(stringId != null){
                stringId.addUsageType(StringId.USAGE_TYPE_NAME);
            }
        }
    }

    public <T1 extends Block> Iterator<T1> getAll(SectionType<T1> sectionType, Key key){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.getAll(key);
        }
        return EmptyIterator.of();
    }
    public <T1 extends Block> T1 get(SectionType<T1> sectionType, Key key){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.get(key);
        }
        return null;
    }
    public<T1 extends Block> Section<T1> get(SectionType<T1> sectionType){
        return getSectionList().get(sectionType);
    }
    public DexHeader getHeader() {
        return getSectionList().getHeader();
    }
    public SectionList getSectionList(){
        return sectionList;
    }
    public MapList getMapList(){
        return getSectionList().getMapList();
    }

    @Override
    protected void onPreRefresh() {
        sectionList.refresh();
    }
    @Override
    protected void onRefreshed() {
        sectionList.updateHeader();
    }
    public boolean isEmpty(){
        Section<ClassId> section = get(SectionType.CLASS_ID);
        return section == null || section.getCount() == 0;
    }
    public boolean merge(DexFileBlock dexFile){
        if(dexFile == this){
            return false;
        }
        return getSectionList().merge(dexFile.getSectionList(), true);
    }
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeader().fileSize.get());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }

    public void read(byte[] dexBytes) throws IOException {
        BlockReader reader = new BlockReader(dexBytes);
        readBytes(reader);
        reader.close();
    }
    public void read(InputStream inputStream) throws IOException {
        BlockReader reader = new BlockReader(inputStream);
        readBytes(reader);
        reader.close();
    }
    public void read(File file) throws IOException {
        BlockReader reader = new BlockReader(file);
        readBytes(reader);
        reader.close();
    }
    public void write(File file) throws IOException {
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        writeBytes(outputStream);
        outputStream.close();
    }

    public String getSimpleName() {
        return mSimpleName;
    }
    public void setSimpleName(String simpleName) {
        this.mSimpleName = simpleName;
    }

    public static boolean isDexFile(File file){
        if(file == null || !file.isFile()){
            return false;
        }
        DexHeader dexHeader = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    public static boolean isDexFile(InputStream inputStream){
        DexHeader dexHeader = null;
        try {
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    private static boolean isDexFile(DexHeader dexHeader){
        if(dexHeader == null){
            return false;
        }
        if(dexHeader.magic.isDefault()){
            return false;
        }
        int version = dexHeader.version.getVersionAsInteger();
        return version > 0 && version < 1000;
    }

    public static final String NAME_value = "value";
    public static final TypeKey TYPE_Signature = TypeKey.create("Ldalvik/annotation/Signature;");
}
