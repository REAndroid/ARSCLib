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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.ClassData;
import com.reandroid.dex.item.StringData;
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

public class DexFile implements VisitableInteger {

    private final DexFileBlock dexFileBlock;

    public DexFile(DexFileBlock dexFileBlock){
        this.dexFileBlock = dexFileBlock;
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
        Section<ClassId> section = getDexFileBlock().get(SectionType.CLASS_ID);
        ClassId classId = section.getPool().get(typeName);
        if(classId == null) {
            return null;
        }
        return create(classId);
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
        StringData stringData = stringSection.getPool().getOrCreate(marker.buildString());
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
    public void sortSection(SectionType<?>[] order){
        refresh();
        getDexFileBlock().sortSection(order);
        refresh();
    }
    public void sortStrings(){
        getDexFileBlock().sortStrings();
    }
    public Iterator<StringData> unusedStrings(){
        return getStringsWithUsage(StringData.USAGE_NONE);
    }
    public Iterator<StringData> getStringsContainsUsage(int usage){
        return FilterIterator.of(getStringData(),
                stringData -> stringData.containsUsage(usage));
    }
    public Iterator<StringData> getStringsWithUsage(int usage){
        return FilterIterator.of(getStringData(),
                stringData -> stringData.getStringUsage() == usage);
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
